package com.rbdip.bookstore.migration;

import static org.assertj.core.api.Assertions.assertThat;

import com.rbdip.bookstore.order.CreateOrderRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MigrationApiLoadTest {
    private static final int CLIENTS = 4;
    private static final int TIMEOUT_SECONDS = 10;
    private static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>("postgres:16-alpine");
    private static final DriverManagerDataSource SOURCE;

    static {
        DATABASE.start();
        SOURCE = new DriverManagerDataSource(
                DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
        Flyway.configure().dataSource(SOURCE).target("1").load().migrate();
        JdbcTemplate jdbc = new JdbcTemplate(SOURCE);
        jdbc.update("INSERT INTO orders(customer_full_name, customer_address) VALUES ('Legacy Family Name', 'Old')");
        Flyway.configure().dataSource(SOURCE).target("4").load().migrate();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.username", DATABASE::getUsername);
        registry.add("spring.datasource.password", DATABASE::getPassword);
        registry.add("spring.flyway.enabled", () -> false);
    }

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void migratesUnderRealHttpReadAndWriteLoad() throws Exception {
        verifyBackfillAndDualWrites();
        Map<?, ?> product = restTemplate.postForEntity("/products",
                Map.of("name", "Migration book", "price", 10), Map.class).getBody();
        long productId = ((Number) product.get("id")).longValue();
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicInteger requests = new AtomicInteger();
        List<Throwable> errors = new CopyOnWriteArrayList<>();
        CountDownLatch started = new CountDownLatch(CLIENTS);
        try (var pool = Executors.newFixedThreadPool(CLIENTS)) {
            for (int client = 0; client < CLIENTS; client++) {
                int number = client;
                pool.submit(() -> exerciseApi(productId, number, running, started, requests, errors));
            }
            try {
                assertThat(started.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
                Flyway.configure().dataSource(SOURCE).load().migrate();
            } finally {
                running.set(false);
                pool.shutdown();
                assertThat(pool.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            }
        }
        assertThat(errors).isEmpty();
        assertThat(requests.get()).isGreaterThanOrEqualTo(CLIENTS * 2);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM information_schema.columns "
                + "WHERE table_name='customers' AND column_name='full_name'", Integer.class)).isZero();
        assertThat(restTemplate.getForEntity("/orders", List.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void exerciseApi(long productId, int client, AtomicBoolean running, CountDownLatch started,
            AtomicInteger requests, List<Throwable> errors) {
        TestRestTemplate http = new TestRestTemplate(new RestTemplateBuilder()
                .rootUri(restTemplate.getRootUri())
                .setConnectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .setReadTimeout(Duration.ofSeconds(TIMEOUT_SECONDS)));
        int iteration = 0;
        do {
            try {
                CreateOrderRequest order = new CreateOrderRequest("Client " + client + "-" + iteration++,
                        "Address", null, "regular", null, List.of(new CreateOrderRequest.Item(productId, 1)));
                assertThat(http.postForEntity("/orders", order, Map.class).getStatusCode())
                        .isEqualTo(HttpStatus.CREATED);
                requests.incrementAndGet();
                assertThat(http.getForEntity("/orders", List.class).getStatusCode()).isEqualTo(HttpStatus.OK);
                requests.incrementAndGet();
            } catch (Throwable error) {
                errors.add(error);
            } finally {
                started.countDown();
            }
        } while (running.get());
    }

    private void verifyBackfillAndDualWrites() {
        assertThat(jdbc.queryForMap("SELECT first_name, last_name FROM customers WHERE id=1"))
                .containsEntry("first_name", "Legacy").containsEntry("last_name", "Family Name");
        jdbc.update("INSERT INTO customers(full_name, address) VALUES ('Old Writer', 'Address')");
        assertThat(jdbc.queryForObject("SELECT first_name FROM customers WHERE full_name='Old Writer'",
                String.class)).isEqualTo("Old");
        jdbc.update("INSERT INTO customers(first_name, last_name) VALUES ('New', 'Writer')");
        assertThat(jdbc.queryForObject("SELECT full_name FROM customers WHERE first_name='New'", String.class))
                .isEqualTo("New Writer");
        jdbc.update("UPDATE customers SET full_name='Changed Surname' WHERE first_name='Old'");
        assertThat(jdbc.queryForObject("SELECT last_name FROM customers WHERE first_name='Changed'", String.class))
                .isEqualTo("Surname");
        jdbc.update("UPDATE customers SET last_name='Updated' WHERE first_name='New'");
        assertThat(jdbc.queryForObject("SELECT full_name FROM customers WHERE first_name='New'", String.class))
                .isEqualTo("New Updated");
    }
}
