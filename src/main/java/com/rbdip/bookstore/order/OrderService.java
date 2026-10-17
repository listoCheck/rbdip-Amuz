package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final OrderValidator validator;
    private final OrderPreparation preparation;
    private final OrderPersistence persistence;
    private final OrderConfirmation confirmation;
    private final PricingCalculator calculator = new PricingCalculator();

    public OrderService(OrderValidator validator, OrderPreparation preparation,
            OrderPersistence persistence, OrderConfirmation confirmation) {
        this.validator = validator;
        this.preparation = preparation;
        this.persistence = persistence;
        this.confirmation = confirmation;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        validator.validate(request);
        List<OrderPreparation.PreparedItem> items = preparation.prepare(request);
        BigDecimal total = calculator.calculateOrderTotal(
                items.stream().map(OrderPreparation.PreparedItem::pricingItem).toList(),
                request.customerType() == null ? "regular" : request.customerType(), request.couponCode());
        Order order = persistence.save(request, items);
        confirmation.send(request.customerFullName(), order.getId(), total);
        return order;
    }
}
