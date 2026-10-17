package com.rbdip.bookstore.order;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderPersistence {
    private final OrderRepository orders;
    private final OrderItemRepository items;

    public OrderPersistence(OrderRepository orders, OrderItemRepository items) {
        this.orders = orders;
        this.items = items;
    }

    public Order save(CreateOrderRequest request, List<OrderPreparation.PreparedItem> prepared) {
        Order order = orders.save(new Order(
                request.customerFullName(), request.customerAddress(), request.customerPhone(), "new"));
        for (OrderPreparation.PreparedItem item : prepared) {
            items.save(new OrderItem(order.getId(), item.product().getName(),
                    item.product().getPrice(), item.quantity()));
        }
        return order;
    }
}
