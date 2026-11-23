# Forge ERP

API REST de um ERP para cadastro de peças e materiais: categorias, unidades de medida, conversões entre unidades, produtos e as unidades específicas de cada produto (por exemplo, 1 caixa = 100 unidades).

A autenticação é feita pelo Keycloak. O ERP guarda o perfil local de cada usuário e decide as permissões pelo próprio banco.

## Tecnologias

- Java 21
- Spring Boot 4.1 (Web MVC, Data JPA, Validation, Actuator)
- PostgreSQL com migrations Flyway (o Hibernate só valida o schema, `ddl-auto: validate`)
- Spring Security como OAuth2 Resource Server (JWT) e Keycloak 26.7 para autenticar
- springdoc-openapi (Swagger UI)
- Lombok
- Testes: JUnit 5, Mockito e MockMvc

## Estrutura

O código principal fica em `erp/erp`. Cada recurso tem seu pacote em `forge.erp`, com `controller`, `dto`, `mapper`, `model`, `repository` e `service`.

```
erp/erp
├── docker-compose.yaml        PostgreSQL e Keycloak para desenvolvimento
├── keycloak/                  realm "forge" importado pelo compose (veja o README da pasta)
└── src/main
    ├── java/forge/erp
    │   ├── category/          categorias
    │   ├── unit/              unidades de medida
    │   ├── unitconversion/    conversões entre unidades
    │   ├── product/           produtos
    │   ├── productunit/       unidades de cada produto
    │   ├── user/ role/ permission/   perfil local e permissões
    │   ├── config/            segurança e OpenAPI
    │   └── common/            geração de códigos e tratamento de erros
    └── resources/db/migration migrations V1 a V6
```

## Como executar

Pré-requisitos: JDK 21 e Docker.

1. Suba o PostgreSQL e o Keycloak:

   ```
   cd erp/erp
   docker compose up -d
   ```

   - PostgreSQL em `localhost:5433` (banco, usuário e senha `forge`)
   - Keycloak em `http://localhost:8180` (console: `admin` / `admin`)

2. Inicie a aplicação:

   ```
   ./mvnw spring-boot:run
   ```

   O `JAVA_HOME` precisa apontar para um JDK 21 ou superior. A porta padrão é a 8080 (`server.port`). Para usar outra, defina a variável `SERVER_PORT`.

3. Abra o Swagger UI em `http://localhost:8080/swagger-ui/index.html`.

   O endereço precisa terminar em `/swagger-ui/index.html`. Endereços como `/swagger-ui` ou `/swagger` não existem e a API responde 401 para eles.

As migrations rodam sozinhas na inicialização.

### Configuração

| Variável | Padrão | Uso |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/forge` | banco de dados |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | `forge` / `forge` | credenciais do banco |
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8180/realms/forge` | emissor dos tokens |
| `SERVER_PORT` | `8080` | porta da aplicação |

A API sobe mesmo sem o Keycloak. Ele só é consultado quando chega a primeira requisição autenticada.

## Segurança

- O Keycloak autentica o usuário e emite o JWT. O identificador do usuário é o claim `sub`. Nenhuma senha é guardada no ERP.
- O ERP mantém `users`, `roles` e `permissions` no banco. A cada requisição, as permissões do usuário são lidas do banco, e não do token. Por isso uma mudança de papel vale na hora, sem novo login.
- No primeiro acesso, o usuário é criado automaticamente com o papel `VIEWER`. Nome, e-mail e username são atualizados a cada acesso.
- Usuário com `active = false` no banco recebe 403 em tudo.
- Não há endpoints para gerenciar usuários ou papéis.

| Papel | Acesso |
|---|---|
| `VIEWER` | somente leitura (`*_READ`) |
| `ANALISTA` | leitura e escrita em todos os recursos (`*_READ` e `*_WRITE`) |

Permissões: `CATEGORY`, `UNIT`, `UNIT_CONVERSION` e `PRODUCT`, cada uma com `_READ` e `_WRITE`. As unidades de um produto usam `PRODUCT_*`. Requisições `GET` pedem `_READ`, e todas as outras pedem `_WRITE`.

Rotas sem token: Swagger UI, `/v3/api-docs` e `/actuator/health`.

### Obter um token

Usuários de teste do realm importado: `analista` / `analista123` e `viewer` / `viewer123`.

```
curl -s -X POST http://localhost:8180/realms/forge/protocol/openid-connect/token \
  -d grant_type=password -d client_id=forge-erp \
  -d username=analista -d password=analista123
```

Use o `access_token` no cabeçalho `Authorization: Bearer <token>` ou no botão **Authorize** do Swagger UI.

### Promover um usuário a ANALISTA

Todo usuário novo começa como `VIEWER`. Depois que a pessoa acessar a API uma vez, promova pelo banco:

```sql
UPDATE user_roles
SET role_id = (SELECT id FROM roles WHERE name = 'ANALISTA')
WHERE user_id = (SELECT id FROM users WHERE keycloak_id = '<sub do usuário>');
```

Mais detalhes em [`erp/erp/keycloak/README.md`](erp/erp/keycloak/README.md).

## Modelo de dados

Todos os IDs são UUID.

```
Category 1 ─── N Product N ─── 1 Unit (unidade de estoque)
                  │
                  1
                  │
                  N
              ProductUnit N ─── 1 Unit

UnitConversion: fromUnit ─── factor ───> toUnit   (ambas são Unit)
User N ─── N Role N ─── N Permission
```

- **Category**: categoria de uma peça (por exemplo, Motores, Parafusos).
- **Unit**: unidade de medida (UN, KG, M, L...), com tipo `QUANTITY`, `MASS`, `LENGTH`, `VOLUME`, `AREA` ou `TIME` e o número de casas decimais permitidas.
- **UnitConversion**: conversão genérica entre duas unidades compatíveis (1 KG = 1000 G).
- **Product**: peça ou material, com categoria, unidade de estoque, peso e estoque mínimo e máximo.
- **ProductUnit**: unidade específica de um produto e seu fator de conversão para a unidade de estoque (1 CAIXA = 100 UN), com indicação de uso na compra e na saída.

### Códigos gerados pelo servidor

O campo `code` não é informado no cadastro. O servidor gera `PREFIXO-XXXX`, com 4 dígitos aleatórios e único: `CAT-` (categoria), `UNI-` (unidade) e `PRD-` (produto). O código não muda em atualizações.

## Regras de negócio

- **Product**: a categoria e a unidade de estoque precisam existir e estar ativas. O estoque máximo não pode ser menor que o mínimo. A unidade de estoque não pode ser trocada enquanto o produto tiver unidades vinculadas.
- **ProductUnit**: o produto precisa estar ativo. A unidade deve ser do mesmo tipo da unidade de estoque, não pode ser a própria unidade de estoque e não pode se repetir no mesmo produto. O fator de conversão deve ser maior que zero.
- **UnitConversion**: as unidades devem ser diferentes, do mesmo tipo (KG → M não é permitido), ativas, e o par origem/destino não pode se repetir. O fator deve ser maior que zero.

## API

Documentação interativa em `/swagger-ui/index.html` e especificação em `/v3/api-docs`.

| Recurso | Rota base | Operações |
|---|---|---|
| Categorias | `/category` | `GET`, `GET /{id}`, `POST`, `PUT ?id={id}`, `DELETE /{id}`, `PUT /{id}/activate`, `PUT /{id}/desactivate` |
| Unidades | `/unit` | `GET`, `GET /{id}`, `POST`, `DELETE /{id}`, `PUT /{id}/activate`, `PUT /{id}/desactivate` |
| Conversões | `/unit-conversion` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `PUT /{id}/activate`, `PUT /{id}/desactivate` |
| Produtos | `/products` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `PATCH /{id}/activate`, `PATCH /{id}/deactivate` |
| Unidades do produto | `/products/{productId}/units` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `PATCH /{id}/activate`, `PATCH /{id}/deactivate` |

Atenção à grafia: a rota é `desactivate` em categorias, unidades e conversões, e `deactivate` em produtos e unidades de produto.

### Exemplo: produto com unidade de compra

```
POST /category        {"name": "Fixadores", "description": "Parafusos e porcas"}
POST /unit            {"name": "Unidade", "symbol": "UN", "type": "QUANTITY", "decimalPlaces": 0, "active": true}
POST /unit            {"name": "Caixa",   "symbol": "CX", "type": "QUANTITY", "decimalPlaces": 0, "active": true}

POST /products
{
  "name": "Parafuso Sextavado M8",
  "description": "Parafuso de aço",
  "categoryId": "<id da categoria>",
  "stockUnitId": "<id de UN>",
  "weight": 0.015,
  "minimumStock": 100,
  "maximumStock": 1000,
  "active": true
}

POST /products/{productId}/units          # 1 CX = 100 UN
{
  "unitId": "<id de CX>",
  "conversionFactor": 100,
  "purchaseUnit": true,
  "issueUnit": false,
  "active": true
}
```

### Erros

As falhas seguem o formato `ProblemDetail` (RFC 7807).

| Status | Quando |
|---|---|
| 400 | dados inválidos; o corpo traz `errors` com a mensagem de cada campo |
| 401 | sem token ou token inválido |
| 403 | usuário sem a permissão exigida ou inativo |
| 404 | registro inexistente |
| 409 | duplicidade ou registro em uso (por exemplo, apagar um produto que tem unidades) |
| 422 | regra de negócio violada |

## Testes

```
cd erp/erp
./mvnw test
```

Os testes unitários (serviços, mappers, controllers, conversão do JWT e uma matriz de autorização que cobre cada endpoint) não precisam de banco. O `ErpApplicationTests` sobe o contexto completo e precisa de um PostgreSQL acessível. Para apontar para outro banco, use `SPRING_DATASOURCE_URL`.

## Integração contínua

O workflow `.github/workflows/maven.yml` roda `./mvnw clean verify` com JDK 21 e um PostgreSQL de serviço. Ele é executado em pushes para `main`, `develop` e `feature/**`, e em pull requests para `main` e `develop`.


