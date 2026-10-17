package com.rbdip.bookstore.review.api;

/** Public port owned by reviews; implementations hide order internals. */
public interface PurchaseHistory {
    boolean hasPurchased(Long productId, String customerName);
}
