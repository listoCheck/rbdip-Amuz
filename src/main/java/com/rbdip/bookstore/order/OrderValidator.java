package com.rbdip.bookstore.order;

import org.springframework.stereotype.Component;

@Component
public class OrderValidator {
    public void validate(CreateOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("order is required");
        }
        requireText(request.customerFullName(), "customerFullName");
        requireText(request.customerAddress(), "customerAddress");
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one item");
        }
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
}
