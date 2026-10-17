UPDATE customers SET first_name = split_part(full_name, ' ', 1),
    last_name = substr(full_name, length(split_part(full_name, ' ', 1)) + 2);
ALTER TABLE customers ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE customers ALTER COLUMN last_name SET NOT NULL;
