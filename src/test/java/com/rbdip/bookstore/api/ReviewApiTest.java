package com.rbdip.bookstore.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.rbdip.bookstore.order.CreateOrderRequest;
import com.rbdip.bookstore.reference.AbstractIntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ReviewApiTest extends AbstractIntegrationTest {
    @Test
    void verifiesOnlyTheAuthorsPurchaseOfThisProduct() {
        long purchased = createProduct("Purchased");
        long other = createProduct("Other");
        CreateOrderRequest order = new CreateOrderRequest("Verified Buyer", "Address", null, null, null,
                List.of(new CreateOrderRequest.Item(purchased, null)));
        assertThat(restTemplate.postForEntity("/orders", order, Map.class).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        addReview(purchased, Map.of("authorName", "Verified Buyer", "rating", 5, "comment", "Good"));
        addReview(purchased, Map.of("rating", 4));
        addReview(other, Map.of("authorName", "Verified Buyer", "rating", 3));
        List<?> reviews = restTemplate.getForObject("/products/" + purchased + "/reviews", List.class);
        assertThat(reviews).hasSize(2);
        Map<?, ?> verified = reviews.stream().map(Map.class::cast)
                .filter(r -> r.get("authorName").equals("Verified Buyer")).findFirst().orElseThrow();
        assertThat(verified.get("verifiedPurchase")).isEqualTo(true);
        Map<?, ?> anonymous = reviews.stream().map(Map.class::cast)
                .filter(r -> r.get("authorName").equals("anonymous")).findFirst().orElseThrow();
        assertThat(anonymous.get("verifiedPurchase")).isEqualTo(false);
        assertThat(anonymous.get("comment")).isEqualTo("");
        List<?> unrelated = restTemplate.getForObject("/products/" + other + "/reviews", List.class);
        assertThat(((Map<?, ?>) unrelated.getFirst()).get("verifiedPurchase")).isEqualTo(false);
        assertThat(restTemplate.getForObject("/products", List.class)).hasSize(2);
    }

    @Test
    void invalidOrdersReturnClientErrorsWithoutPersistingOrders() {
        long productId = createProduct("Book");
        CreateOrderRequest invalid = new CreateOrderRequest("Buyer", "Address", null, null, null,
                List.of(new CreateOrderRequest.Item(productId, 0)));
        assertThat(restTemplate.postForEntity("/orders", invalid, Map.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(restTemplate.getForObject("/orders", List.class)).isEmpty();
    }

    private long createProduct(String name) {
        Map<?, ?> result = restTemplate.postForEntity("/products",
                Map.of("name", name, "price", 25, "description", "Description"), Map.class).getBody();
        return ((Number) result.get("id")).longValue();
    }

    private void addReview(long productId, Map<String, Object> body) {
        assertThat(restTemplate.postForEntity("/products/" + productId + "/reviews", body, Map.class).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
    }
}
