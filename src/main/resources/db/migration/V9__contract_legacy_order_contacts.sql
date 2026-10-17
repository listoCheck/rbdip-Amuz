-- Deploy only after all application instances use customers.
DROP TRIGGER orders_customer_bridge ON orders;
DROP FUNCTION sync_order_customer();
ALTER TABLE orders DROP COLUMN customer_full_name,
                   DROP COLUMN customer_address,
                   DROP COLUMN customer_phone;
