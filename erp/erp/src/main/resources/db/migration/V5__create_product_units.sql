CREATE TABLE product_units (
    id UUID PRIMARY KEY,

    product_id UUID NOT NULL,
    unit_id UUID NOT NULL,

    conversion_factor NUMERIC(19, 6) NOT NULL,

    purchase_unit BOOLEAN NOT NULL DEFAULT FALSE,
    issue_unit BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_product_units_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT fk_product_units_unit
        FOREIGN KEY (unit_id)
        REFERENCES units(id),

    CONSTRAINT chk_product_units_conversion_factor_positive
        CHECK (conversion_factor > 0),

    -- um produto nao repete a mesma unidade (o indice tambem cobre buscas por product_id)
    CONSTRAINT uk_product_units_product_unit
        UNIQUE (product_id, unit_id)
);

CREATE INDEX idx_product_units_unit_id ON product_units (unit_id);
