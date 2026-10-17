-- Run after readers and writers use first_name/last_name.
-- The order bridge still supports legacy order consumers until V9.
CREATE OR REPLACE FUNCTION sync_order_customer() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
    given_name TEXT;
    family_name TEXT;
BEGIN
    IF NEW.customer_id IS NULL OR
       (TG_OP = 'UPDATE' AND ROW(NEW.customer_full_name, NEW.customer_address, NEW.customer_phone)
          IS DISTINCT FROM ROW(OLD.customer_full_name, OLD.customer_address, OLD.customer_phone)) THEN
        given_name := split_part(NEW.customer_full_name, ' ', 1);
        family_name := substr(NEW.customer_full_name, length(given_name) + 2);
        SELECT id INTO NEW.customer_id FROM customers
        WHERE first_name = given_name AND last_name = family_name
          AND address IS NOT DISTINCT FROM NEW.customer_address
          AND phone IS NOT DISTINCT FROM NEW.customer_phone LIMIT 1;
        IF NEW.customer_id IS NULL THEN
            INSERT INTO customers(first_name, last_name, address, phone)
            VALUES (given_name, family_name, NEW.customer_address, NEW.customer_phone)
            RETURNING id INTO NEW.customer_id;
        END IF;
    ELSE
        SELECT first_name || CASE WHEN last_name = '' THEN '' ELSE ' ' || last_name END, address, phone
        INTO NEW.customer_full_name, NEW.customer_address, NEW.customer_phone
        FROM customers WHERE id = NEW.customer_id;
    END IF;
    RETURN NEW;
END $$;
DROP TRIGGER customer_names_bridge ON customers;
DROP FUNCTION sync_customer_names();
ALTER TABLE customers DROP COLUMN full_name;
