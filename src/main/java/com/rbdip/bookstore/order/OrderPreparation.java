package com.rbdip.bookstore.order;

import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderPreparation {
    private final ProductRepository products;

    public OrderPreparation(ProductRepository products) {
        this.products = products;
    }

    public List<PreparedItem> prepare(CreateOrderRequest request) {
        return request.items().stream().map(this::prepareItem).toList();
    }

    private PreparedItem prepareItem(CreateOrderRequest.Item item) {
        if (item == null || item.productId() == null) {
            throw new IllegalArgumentException("productId is required");
        }
        int quantity = item.quantity() == null ? 1 : item.quantity();
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        Product product = products.findById(item.productId())
                .orElseThrow(() -> new IllegalArgumentException("product " + item.productId() + " not found"));
        return new PreparedItem(product, quantity);
    }

    public record PreparedItem(Product product, int quantity) {
        public PricingCalculator.LineItem pricingItem() {
            return new PricingCalculator.LineItem(product.getPrice(), quantity);
        }
    }
}
