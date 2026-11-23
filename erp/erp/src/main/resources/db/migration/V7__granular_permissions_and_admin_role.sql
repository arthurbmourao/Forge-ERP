-- Permissoes granulares (RECURSO_READ / _CREATE / _UPDATE / _DELETE) no lugar de READ/WRITE,
-- permissoes de gestao de usuarios e o papel ADMIN.
-- PRODUCT_* tambem cobre as unidades do produto (/products/{id}/units).

-- 1. Novas permissoes de negocio (as *_READ ja existem desde a V6)
INSERT INTO permissions (id, name, description)
SELECT gen_random_uuid(), r.resource || '_' || a.action, a.label || ' ' || r.label
FROM (VALUES ('CATEGORY', 'categorias'),
             ('UNIT', 'unidades'),
             ('UNIT_CONVERSION', 'conversoes de unidade'),
             ('PRODUCT', 'produtos e suas unidades')) AS r(resource, label)
CROSS JOIN (VALUES ('CREATE', 'Criar'),
                   ('UPDATE', 'Alterar, ativar e desativar'),
                   ('DELETE', 'Remover')) AS a(action, label);

-- 2. Permissoes de gestao de usuarios
INSERT INTO permissions (id, name, description) VALUES
    (gen_random_uuid(), 'USER_READ', 'Consultar usuarios'),
    (gen_random_uuid(), 'USER_UPDATE', 'Alterar o papel, ativar e desativar usuarios');

-- 3. Remove as permissoes *_WRITE, substituidas pelas granulares
DELETE FROM role_permissions
WHERE permission_id IN (SELECT id FROM permissions WHERE RIGHT(name, 6) = '_WRITE');

DELETE FROM permissions WHERE RIGHT(name, 6) = '_WRITE';

-- 4. Papel ADMIN (acesso total, incluindo usuarios)
INSERT INTO roles (id, name, description)
VALUES (gen_random_uuid(), 'ADMIN', 'Acesso total, incluindo a gestao de usuarios');

UPDATE roles
SET description = 'Leitura e escrita em todos os recursos de negocio, sem gestao de usuarios'
WHERE name = 'ANALISTA';

-- 5. Concessoes: ADMIN recebe todas; ANALISTA recebe todas menos as de usuario.
-- VIEWER continua apenas com as *_READ de negocio (nao recebe USER_READ).
-- Os vinculos usuario -> papel (user_roles) nao sao alterados.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p
  ON r.name = 'ADMIN'
  OR (r.name = 'ANALISTA' AND LEFT(p.name, 5) <> 'USER_')
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 6. Um papel por usuario
CREATE UNIQUE INDEX uk_user_roles_user_id ON user_roles (user_id);
