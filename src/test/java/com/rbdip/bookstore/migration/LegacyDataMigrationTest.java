package com.rbdip.bookstore.migration;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class LegacyDataMigrationTest {
    @Container
    private static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void preservesLegacyDataAndReusesCustomerContacts() {
        DriverManagerDataSource source = new DriverManagerDataSource(
                DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
        JdbcTemplate jdbc = new JdbcTemplate(source);
        Flyway.configure().dataSource(source).target("1").load().migrate();
        jdbc.update("INSERT INTO products(name, price) VALUES ('Book', 25.00)");
        jdbc.update("INSERT INTO orders(customer_full_name, customer_address) VALUES ('Anna Smith', 'Address')");
        jdbc.update("INSERT INTO orders(customer_full_name, customer_address) VALUES ('Anna Smith', 'Address')");
        jdbc.update("INSERT INTO order_items(order_id, product_name, product_price, quantity) "
                + "VALUES (1, 'Book', 25.00, 2), (2, 'Removed book', 12.00, 3)");
        Flyway.configure().dataSource(source).load().migrate();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM customers", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(DISTINCT customer_id) FROM orders", Integer.class))
                .isEqualTo(1);
        assertThat(jdbc.queryForList("SELECT p.name FROM order_items i JOIN products p ON p.id=i.product_id "
                + "ORDER BY i.id", String.class)).containsExactly("Book", "Removed book");
        assertThat(jdbc.queryForList("SELECT quantity FROM order_items ORDER BY id", Integer.class))
                .containsExactly(2, 3);
    }
}
