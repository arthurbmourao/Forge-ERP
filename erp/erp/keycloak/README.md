# Keycloak (desenvolvimento)

O Keycloak autentica o usuario e emite o JWT. O Forge ERP decide as permissoes pelo banco
(`users` -> `roles` -> `permissions`). Nenhuma senha e guardada no ERP.

## Subir

O servico do Keycloak nao esta mais no `docker-compose.yaml`. Para subir um Keycloak local com o
realm `forge` (a partir de `erp/erp`):

```
docker run -d --name forge-keycloak -p 8180:8080 \
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
  -v "${PWD}/keycloak/forge-realm.json:/opt/keycloak/data/import/forge-realm.json:ro" \
  quay.io/keycloak/keycloak:26.7.3 start-dev --import-realm
```

- Console: http://localhost:8180 (`admin` / `admin`, realm `master`)
- Realm importado: `forge`, cliente publico `forge-erp`
- A aplicacao valida os tokens em `KEYCLOAK_ISSUER_URI`
  (padrao `http://localhost:8180/realms/forge`).
- O realm so e importado quando ele ainda nao existe. Para receber usuarios novos deste arquivo
  (como o `admin`), recrie o container.

## Usuarios de teste (realm `forge`)

| Usuario    | Senha         | Papel no ERP                                                     |
|------------|---------------|------------------------------------------------------------------|
| `admin`    | `admin123`    | `ADMIN` se for o `FORGE_BOOTSTRAP_ADMIN` (veja abaixo); senao `VIEWER` |
| `analista` | `analista123` | `VIEWER` ate ser promovido por um `ADMIN`                         |
| `viewer`   | `viewer123`   | `VIEWER`                                                         |

## Papeis e permissoes

Cada usuario tem um unico papel. Os papeis e permissoes sao fixos (criados por migration).

| Papel      | O que pode                                                                      |
|------------|---------------------------------------------------------------------------------|
| `VIEWER`   | somente leitura dos recursos de negocio (`*_READ`)                               |
| `ANALISTA` | leitura, criacao, alteracao e remocao dos recursos de negocio, sem gerir usuarios |
| `ADMIN`    | tudo, incluindo a gestao de usuarios                                             |

Permissoes: `CATEGORY`, `UNIT`, `UNIT_CONVERSION` e `PRODUCT` (que cobre tambem as unidades do
produto), cada uma com `_READ`, `_CREATE`, `_UPDATE` e `_DELETE`, mais `USER_READ` e `USER_UPDATE`.
`GET` exige `_READ`, `POST` exige `_CREATE`, `PUT`/`PATCH` (inclusive ativar e desativar) exigem
`_UPDATE` e `DELETE` exige `_DELETE`.

## Primeiro ADMIN

Todo usuario novo e criado no primeiro acesso com o papel `VIEWER`. Para nascer o primeiro
administrador, defina a variavel de ambiente com o **username** do Keycloak:

```
FORGE_BOOTSTRAP_ADMIN=admin
```

Quando esse usuario acessar a API, ele vira `ADMIN`. A variavel so vale **enquanto nao existir
nenhum `ADMIN`**: depois do primeiro, ela e ignorada (ninguem e repromovido por ela). Vazia, ela
fica desabilitada.

Alternativa, direto no banco (a pessoa precisa ter acessado a API uma vez):

```sql
UPDATE user_roles
SET role_id = (SELECT id FROM roles WHERE name = 'ADMIN')
WHERE user_id = (SELECT id FROM users WHERE keycloak_id = '<sub do usuario>');
```

## Gestao de usuarios (somente `ADMIN`)

| Operacao                | Rota                          |
|-------------------------|-------------------------------|
| listar                  | `GET /users`                  |
| consultar               | `GET /users/{id}`             |
| trocar o papel          | `PUT /users/{id}/role` com `{"role": "ANALISTA"}` |
| ativar                  | `PATCH /users/{id}/activate`  |
| desativar               | `PATCH /users/{id}/deactivate`|

- Usuarios nao sao criados pela API: nascem no Keycloak e entram no primeiro acesso.
- Ninguem altera o proprio papel nem desativa a si mesmo (evita ficar sem administrador).
- As permissoes sao lidas do banco a cada requisicao: a mudanca vale na hora, sem novo login.
- Usuario desativado recebe 403 em tudo.

## Obter um token

```
curl -s -X POST http://localhost:8180/realms/forge/protocol/openid-connect/token \
  -d grant_type=password -d client_id=forge-erp \
  -d username=admin -d password=admin123
```

Use o `access_token` no cabecalho `Authorization: Bearer <token>` ou no botao **Authorize**
do Swagger UI.
