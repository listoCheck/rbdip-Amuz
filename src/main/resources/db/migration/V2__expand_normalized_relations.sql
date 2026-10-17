-- Expand first: preserve legacy order columns until the final contract.
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50)
);
INSERT INTO customers(full_name, address, phone)
SELECT DISTINCT customer_full_name, customer_address, customer_phone FROM orders;
ALTER TABLE orders ADD COLUMN customer_id BIGINT REFERENCES customers(id);
UPDATE orders o SET customer_id = c.id FROM customers c
WHERE c.full_name = o.customer_full_name
  AND c.address IS NOT DISTINCT FROM o.customer_address
  AND c.phone IS NOT DISTINCT FROM o.customer_phone;
ALTER TABLE orders ALTER COLUMN customer_id SET NOT NULL;
CREATE INDEX orders_customer_idx ON orders(customer_id);

-- Bridge old and new writers during the rollout.
CREATE FUNCTION sync_order_customer() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.customer_id IS NULL OR
       (TG_OP = 'UPDATE' AND ROW(NEW.customer_full_name, NEW.customer_address, NEW.customer_phone)
          IS DISTINCT FROM ROW(OLD.customer_full_name, OLD.customer_address, OLD.customer_phone)) THEN
        SELECT id INTO NEW.customer_id FROM customers
        WHERE full_name = NEW.customer_full_name
          AND address IS NOT DISTINCT FROM NEW.customer_address
          AND phone IS NOT DISTINCT FROM NEW.customer_phone LIMIT 1;
        IF NEW.customer_id IS NULL THEN
            INSERT INTO customers(full_name, address, phone)
            VALUES (NEW.customer_full_name, NEW.customer_address, NEW.customer_phone)
            RETURNING id INTO NEW.customer_id;
        END IF;
    ELSE
        SELECT full_name, address, phone
        INTO NEW.customer_full_name, NEW.customer_address, NEW.customer_phone
        FROM customers WHERE id = NEW.customer_id;
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER orders_customer_bridge BEFORE INSERT OR UPDATE ON orders
FOR EACH ROW EXECUTE FUNCTION sync_order_customer();

-- Preserve legacy item data, including products absent from the catalog.
INSERT INTO products(name, price)
SELECT DISTINCT i.product_name, i.product_price FROM order_items i
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.name = i.product_name AND p.price = i.product_price);
ALTER TABLE order_items ADD COLUMN product_id BIGINT REFERENCES products(id);
UPDATE order_items i SET product_id = (
    SELECT min(p.id) FROM products p WHERE p.name = i.product_name AND p.price = i.product_price
);
ALTER TABLE order_items ALTER COLUMN product_id SET NOT NULL;
ALTER TABLE order_items DROP COLUMN product_name, DROP COLUMN product_price;
CREATE INDEX order_items_order_idx ON order_items(order_id);
CREATE INDEX order_items_product_idx ON order_items(product_id);
