CREATE TABLE unit_conversions (
    id UUID PRIMARY KEY,

    from_unit_id UUID NOT NULL,
    to_unit_id UUID NOT NULL,

    factor NUMERIC(19, 6) NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_unit_conversion_from_unit
        FOREIGN KEY (from_unit_id)
        REFERENCES units(id),

    CONSTRAINT fk_unit_conversion_to_unit
        FOREIGN KEY (to_unit_id)
        REFERENCES units(id),

    CONSTRAINT chk_unit_conversion_factor_positive
        CHECK (factor > 0),

    CONSTRAINT chk_unit_conversion_different_units
        CHECK (from_unit_id <> to_unit_id),

    CONSTRAINT uk_unit_conversion_from_to
        UNIQUE (from_unit_id, to_unit_id)
);