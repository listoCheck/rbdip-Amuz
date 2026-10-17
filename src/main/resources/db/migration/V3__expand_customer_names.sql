ALTER TABLE customers ADD COLUMN first_name VARCHAR(255), ADD COLUMN last_name VARCHAR(255);

-- Accept both old full-name writers and new split-name writers.
CREATE FUNCTION sync_customer_names() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF NEW.first_name IS NULL THEN
            NEW.first_name := split_part(NEW.full_name, ' ', 1);
            NEW.last_name := substr(NEW.full_name, length(NEW.first_name) + 2);
        ELSE
            NEW.full_name := NEW.first_name || CASE WHEN coalesce(NEW.last_name, '') = ''
                THEN '' ELSE ' ' || NEW.last_name END;
        END IF;
    ELSIF NEW.full_name IS DISTINCT FROM OLD.full_name THEN
        NEW.first_name := split_part(NEW.full_name, ' ', 1);
        NEW.last_name := substr(NEW.full_name, length(NEW.first_name) + 2);
    ELSE
        NEW.full_name := NEW.first_name || CASE WHEN coalesce(NEW.last_name, '') = ''
            THEN '' ELSE ' ' || NEW.last_name END;
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER customer_names_bridge BEFORE INSERT OR UPDATE ON customers
FOR EACH ROW EXECUTE FUNCTION sync_customer_names();
