# Keycloak (desenvolvimento)

O Keycloak autentica o usuario e emite o JWT. O Forge ERP decide as permissoes pelo banco
(`users` -> `roles` -> `permissions`). Nenhuma senha e guardada no ERP.

## Subir

```
docker compose up -d keycloak
```

- Console: http://localhost:8180 (`admin` / `admin`)
- Realm importado: `forge`, cliente publico `forge-erp`
- A aplicacao valida os tokens em `KEYCLOAK_ISSUER_URI`
  (padrao `http://localhost:8180/realms/forge`).

## Usuarios de teste

| Usuario    | Senha         | Papel no ERP                                   |
|------------|---------------|------------------------------------------------|
| `analista` | `analista123` | `VIEWER` ate ser promovido (veja abaixo)       |
| `viewer`   | `viewer123`   | `VIEWER`                                       |

## Papeis

Todo usuario novo e criado no primeiro acesso com o papel `VIEWER` (somente leitura).
Nao ha tela de gestao de papeis; para promover alguem a `ANALISTA` (leitura e escrita em tudo),
a pessoa precisa ter acessado a API uma vez e entao:

```sql
UPDATE user_roles
SET role_id = (SELECT id FROM roles WHERE name = 'ANALISTA')
WHERE user_id = (SELECT id FROM users WHERE keycloak_id = '3f2b1c8e-5a47-4d6e-9b1a-0c7d2e4f6a11');
```

O valor de `keycloak_id` e o `sub` do token (o usuario `analista` acima ja tem um `sub` fixo).
Para desativar um usuario: `UPDATE users SET active = false WHERE keycloak_id = '...';`

## Obter um token

```
curl -s -X POST http://localhost:8180/realms/forge/protocol/openid-connect/token \
  -d grant_type=password -d client_id=forge-erp \
  -d username=analista -d password=analista123
```

Use o `access_token` no cabecalho `Authorization: Bearer <token>` ou no botao **Authorize**
do Swagger UI.
