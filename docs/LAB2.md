# Lab 2

Customers are stored separately; order items reference products via foreign keys.
V2 backfills existing customers and products (including legacy items missing from
the catalog) and installs a compatibility bridge for legacy order writers.
V9 removes legacy order contacts. V3-V8 are reserved for intermediate lab 3 steps.
V1 is unchanged. Characterization tests cover discount boundaries, rounding,
operation order, unknown codes, empty orders and negative values.

On an existing database, deploy the expansion first, switch all application
instances to normalized relations, then apply the contract. Product names/prices
now come from the catalog, as required by the assignment; historical price
snapshots would require a separate business model.
