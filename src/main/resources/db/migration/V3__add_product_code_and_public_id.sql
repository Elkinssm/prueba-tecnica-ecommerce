-- Add product_code column to inventory table
ALTER TABLE inventory ADD COLUMN product_code VARCHAR(20);

-- Update existing records with generated product codes
UPDATE inventory SET product_code = CONCAT('PROD-', UPPER(LEFT(CAST(product_id AS VARCHAR), 8)));

-- Make product_code NOT NULL and add unique constraint
ALTER TABLE inventory ALTER COLUMN product_code SET NOT NULL;
ALTER TABLE inventory ADD CONSTRAINT uk_inventory_product_code UNIQUE (product_code);

-- Create index for better performance
CREATE INDEX idx_inventory_product_code ON inventory (product_code);

-- Add public_id column to orders table
ALTER TABLE orders ADD COLUMN public_id VARCHAR(20);

-- Create unique index for orders public_id
CREATE UNIQUE INDEX idx_orders_public_id ON orders(public_id);

-- Update existing orders with generated public IDs
UPDATE orders SET public_id = 'ORD-' || FORMATDATETIME(created_at, 'yyyyMMdd') || '-' || SUBSTRING(REPLACE(id, '-', ''), 1, 4)
WHERE public_id IS NULL;
