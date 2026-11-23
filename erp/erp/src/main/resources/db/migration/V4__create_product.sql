
CREATE TABLE products (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    category_id UUID NOT NULL,
    stock_unit_id UUID NOT NULL,
    weight NUMERIC(19, 6),
    minimum_stock NUMERIC(19, 6),
    maximum_stock NUMERIC(19, 6),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT uk_products_code
        UNIQUE (code),

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id),

    CONSTRAINT fk_products_stock_unit
        FOREIGN KEY (stock_unit_id)
        REFERENCES units(id),

    CONSTRAINT chk_products_weight
        CHECK (weight IS NULL OR weight >= 0),

    CONSTRAINT chk_products_minimum_stock
        CHECK (minimum_stock IS NULL OR minimum_stock >= 0),

    CONSTRAINT chk_products_maximum_stock
        CHECK (maximum_stock IS NULL OR maximum_stock >= 0),

    CONSTRAINT chk_products_stock_range
        CHECK (
            minimum_stock IS NULL
            OR maximum_stock IS NULL
            OR maximum_stock >= minimum_stock
        )
);
