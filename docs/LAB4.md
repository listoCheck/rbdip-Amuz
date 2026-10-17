# Lab 4

OrderRepository loads customer, items and products with one entity graph.
GET /orders no longer queries positions separately for each order. The reference
Hibernate query-count test verifies the bound of three statements for five orders.

Reviews own the public PurchaseHistory port. OrderPurchaseHistory is the adapter
that queries order internals. ReviewService imports only its port, never order
entities or repositories. The formerly unused verification now records whether
that author purchased that specific product; review creation remains available
for unverified authors. V3.1 adds the verification flag without breaking old readers.
The reference ArchUnit rule verifies the dependency boundary.

The additive review column uses a reserved pre-contract version slot so the
application can also run at V4 during the customer migration load test.
