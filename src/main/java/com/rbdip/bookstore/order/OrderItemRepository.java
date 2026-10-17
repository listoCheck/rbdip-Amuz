package com.rbdip.bookstore.order;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @EntityGraph(attributePaths = "product")
    List<OrderItem> findByOrderId(Long orderId);
    @Query("select count(i) > 0 from OrderItem i, Order o where i.orderId = o.id "
            + "and i.product.id = :productId "
            + "and trim(concat(o.customer.firstName, ' ', o.customer.lastName)) = :customerName")
    boolean hasPurchased(@Param("productId") Long productId, @Param("customerName") String customerName);
}
