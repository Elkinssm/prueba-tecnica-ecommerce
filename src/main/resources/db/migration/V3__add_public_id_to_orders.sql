ALTER TABLE orders ADD COLUMN public_id VARCHAR(20);

CREATE UNIQUE INDEX idx_orders_public_id ON orders(public_id);

UPDATE orders SET public_id = 'ORD-' || FORMATDATETIME(created_at, 'yyyyMMdd') || '-' || SUBSTRING(REPLACE(id, '-', ''), 1, 4)
WHERE public_id IS NULL;