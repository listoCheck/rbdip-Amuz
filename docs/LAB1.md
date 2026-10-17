# Lab 1

Identified smells: OrderService combines validation, product loading, persistence
and notifications; PricingCalculator contains magic factors; quantity handling is
duplicated; orders/order_items duplicate customer and catalog data; order listing
has N+1 queries; review depends on internal order repositories.

Extracted OrderValidator, OrderPreparation, OrderPersistence and OrderConfirmation.
Named pricing rules without changing their behavior. Database and module smells
are addressed in subsequent labs. The four baseline pricing/API tests pass.
