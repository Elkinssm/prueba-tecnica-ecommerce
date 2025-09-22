-- Fix product_code for existing inventory records
-- Using H2-compatible syntax to generate product codes

-- First, make the column nullable temporarily if it's not
ALTER TABLE inventory ALTER COLUMN product_code DROP NOT NULL;

-- Update all records with explicit product codes based on their IDs
UPDATE inventory SET product_code = 'PROD-11111111' WHERE product_id = '11111111-2222-3333-4444-555555555555';
UPDATE inventory SET product_code = 'PROD-22222222' WHERE product_id = '22222222-3333-4444-5555-666666666666';
UPDATE inventory SET product_code = 'PROD-33333333' WHERE product_id = '33333333-4444-5555-6666-777777777777';
UPDATE inventory SET product_code = 'PROD-44444444' WHERE product_id = '44444444-5555-6666-7777-888888888888';
UPDATE inventory SET product_code = 'PROD-55555555' WHERE product_id = '55555555-6666-7777-8888-999999999999';
UPDATE inventory SET product_code = 'PROD-66666666' WHERE product_id = '66666666-7777-8888-9999-000000000000';
UPDATE inventory SET product_code = 'PROD-AAAAAAAA' WHERE product_id = 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee';

-- Make the column NOT NULL again
ALTER TABLE inventory ALTER COLUMN product_code SET NOT NULL;
