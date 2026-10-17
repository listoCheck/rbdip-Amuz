package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderConfirmation {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConfirmation.class);

    public void send(String customerName, Long orderId, BigDecimal total) {
        LOGGER.info("[email] Dear {}, your order #{} for {} has been placed.", customerName, orderId, total);
    }
}
