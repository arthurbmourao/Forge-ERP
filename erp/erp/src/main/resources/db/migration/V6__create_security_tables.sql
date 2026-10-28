-- Perfil local dos usuarios. A autenticacao (senha) fica no Keycloak; o ERP guarda apenas
-- o identificador (sub do token) e decide as permissoes por Role/Permission.
CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),

    CONSTRAINT uk_permissions_name UNIQUE (name)
);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),

    CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    keycloak_id VARCHAR(64) NOT NULL,
    username VARCHAR(150),
    email VARCHAR(255),
    name VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uk_users_keycloak_id UNIQUE (keycloak_id)
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,

    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,

    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id),

    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id)
        REFERENCES permissions(id)
);

CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions (permission_id);

-- Permissoes: uma de leitura e uma de escrita por recurso.
-- PRODUCT_* tambem cobre as unidades do produto (/products/{id}/units).
INSERT INTO permissions (id, name, description)
SELECT gen_random_uuid(),
       r.resource || '_' || a.action,
       CASE a.action WHEN 'READ' THEN 'Consultar ' ELSE 'Criar, alterar e remover ' END || r.label
FROM (VALUES ('CATEGORY', 'categorias'),
             ('UNIT', 'unidades'),
             ('UNIT_CONVERSION', 'conversoes de unidade'),
             ('PRODUCT', 'produtos e suas unidades')) AS r(resource, label)
CROSS JOIN (VALUES ('READ'), ('WRITE')) AS a(action);

-- Papeis: ANALISTA (acesso maximo) e VIEWER (acesso minimo, somente leitura).
INSERT INTO roles (id, name, description) VALUES
    (gen_random_uuid(), 'ANALISTA', 'Acesso total: leitura e escrita em todos os recursos'),
    (gen_random_uuid(), 'VIEWER', 'Acesso minimo: somente leitura');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON r.name = 'ANALISTA'
                   OR (r.name = 'VIEWER' AND RIGHT(p.name, 5) = '_READ');
