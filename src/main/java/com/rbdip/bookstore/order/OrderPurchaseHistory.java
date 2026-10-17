package com.rbdip.bookstore.order;

import com.rbdip.bookstore.review.api.PurchaseHistory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderPurchaseHistory implements PurchaseHistory {
    private final OrderItemRepository items;

    public OrderPurchaseHistory(OrderItemRepository items) {
        this.items = items;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPurchased(Long productId, String customerName) {
        return items.hasPurchased(productId, customerName);
    }
}
