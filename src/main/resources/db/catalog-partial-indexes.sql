-- Catálogo: índices parciales para soft-delete (PostgreSQL).
-- Este servicio no usa Flyway/Liquibase; aplicar manualmente tras el ddl de Hibernate
-- o en el script de aprovisionamiento de la base.
-- Objetivo: reutilizar SKU / códigos cuando deleted_at IS NOT NULL.

CREATE UNIQUE INDEX IF NOT EXISTS uq_products_sku_active
    ON products (sku)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_product_variants_sku_active
    ON product_variants (sku)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_uom_abbreviation_active
    ON unit_of_measures (abbreviation)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_brands_code_active
    ON brands (code)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_categories_name_active
    ON categories (name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_price_lists_code_active
    ON price_lists (code)
    WHERE deleted_at IS NULL;

-- Columnas nuevas esperadas por el modelo (si update no las crea):
-- ALTER TABLE products ADD COLUMN IF NOT EXISTS product_type VARCHAR(20) NOT NULL DEFAULT 'STORABLE';
-- ALTER TABLE products ADD COLUMN IF NOT EXISTS barcode VARCHAR(64);
-- ALTER TABLE products ADD COLUMN IF NOT EXISTS price_currency VARCHAR(3) NOT NULL DEFAULT 'PEN';
-- ALTER TABLE products ADD COLUMN IF NOT EXISTS costing_method VARCHAR(20) NOT NULL DEFAULT 'STANDARD';
-- ALTER TABLE products ADD COLUMN IF NOT EXISTS standard_cost NUMERIC(19,2) NOT NULL DEFAULT 0;
-- ALTER TABLE products ADD COLUMN IF NOT EXISTS standard_cost_currency VARCHAR(3) NOT NULL DEFAULT 'PEN';
-- ALTER TABLE products ADD COLUMN IF NOT EXISTS average_cost NUMERIC(19,2) NOT NULL DEFAULT 0;
-- ALTER TABLE products ADD COLUMN IF NOT EXISTS average_cost_currency VARCHAR(3) NOT NULL DEFAULT 'PEN';
-- ALTER TABLE categories ADD COLUMN IF NOT EXISTS parent_id BIGINT;
-- ALTER TABLE unit_of_measures ADD COLUMN IF NOT EXISTS category VARCHAR(20) NOT NULL DEFAULT 'UNIT';
