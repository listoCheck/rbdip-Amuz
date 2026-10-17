package com.rbdip.bookstore.order;

import java.util.List;
import com.rbdip.bookstore.customer.Customer;
import com.rbdip.bookstore.customer.CustomerRepository;
import org.springframework.stereotype.Component;

@Component
public class OrderPersistence {
    private final CustomerRepository customers;
    private final OrderRepository orders;
    private final OrderItemRepository items;

    public OrderPersistence(OrderRepository orders, OrderItemRepository items, CustomerRepository customers) {
        this.customers = customers;
        this.orders = orders;
        this.items = items;
    }

    public Order save(CreateOrderRequest request, List<OrderPreparation.PreparedItem> prepared) {
        Customer customer = customers.findFirstByFullNameAndAddressAndPhone(
                request.customerFullName(), request.customerAddress(), request.customerPhone())
                .orElseGet(() -> customers.save(new Customer(
                        request.customerFullName(), request.customerAddress(), request.customerPhone())));
        Order order = orders.save(new Order(customer, "new"));
        for (OrderPreparation.PreparedItem item : prepared) {
            items.save(new OrderItem(order.getId(), item.product(), item.quantity()));
        }
        return order;
    }
}
