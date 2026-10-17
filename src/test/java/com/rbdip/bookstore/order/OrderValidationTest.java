package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class OrderValidationTest {
    private final OrderValidator validator = new OrderValidator();
    private final ProductRepository products = mock(ProductRepository.class);
    private final OrderPreparation preparation = new OrderPreparation(products);

    static Stream<CreateOrderRequest> invalidOrders() {
        List<CreateOrderRequest.Item> items = List.of(new CreateOrderRequest.Item(1L, 1));
        return Stream.of(null, request(null, "Address", items), request(" ", "Address", items),
                request("Name", null, items), request("Name", " ", items),
                request("Name", "Address", null), request("Name", "Address", List.of()));
    }

    @ParameterizedTest
    @MethodSource("invalidOrders")
    void rejectsMissingRequiredOrderData(CreateOrderRequest request) {
        assertThatThrownBy(() -> validator.validate(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void defaultsQuantityToOneAndLoadsTheProduct() {
        Product product = new Product("Book", new BigDecimal("12.50"), null);
        when(products.findById(1L)).thenReturn(Optional.of(product));
        CreateOrderRequest request = request("Name", "Address", List.of(new CreateOrderRequest.Item(1L, null)));
        validator.validate(request);
        List<OrderPreparation.PreparedItem> items = preparation.prepare(request);
        assertThat(items).hasSize(1);
        assertThat(items.getFirst().product()).isSameAs(product);
        assertThat(items.getFirst().pricingItem()).isEqualTo(
                new PricingCalculator.LineItem(new BigDecimal("12.50"), 1));
    }

    @Test
    void rejectsMissingProductsAndInvalidQuantities() {
        when(products.findById(1L)).thenReturn(Optional.empty());
        for (CreateOrderRequest.Item item : List.of(new CreateOrderRequest.Item(1L, 1),
                new CreateOrderRequest.Item(null, 1), new CreateOrderRequest.Item(1L, 0),
                new CreateOrderRequest.Item(1L, -1))) {
            assertThatThrownBy(() -> preparation.prepare(request("Name", "Address", List.of(item))))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> preparation.prepare(request("Name", "Address", Collections.singletonList(null))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static CreateOrderRequest request(String name, String address, List<CreateOrderRequest.Item> items) {
        return new CreateOrderRequest(name, address, null, null, null, items);
    }
}
