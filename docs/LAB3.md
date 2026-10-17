# Lab 3: expand / migrate / contract

V3 adds first_name/last_name and a bidirectional write bridge. Old full-name
writers and new split-name writers coexist. V4 backfills existing data. The new
application reads and writes split names while preserving the HTTP full-name API.
V5 drops the old customer column after all instances switch. V9 removes the
legacy order contacts. Single-word names use an empty last name; all text after
the first space is retained, including compound surnames.

Production rollout (each migration/deployment is a separate operation):
1. Apply V2-V3 with the old application still running.
2. Apply V4; verify backfill and deploy the split-name application.
3. Drain old instances, apply V5, then V9.
Use SPRING_FLYWAY_TARGET=3 or 4 during intermediate releases. Do not allow a
legacy application to auto-apply contract migrations at startup.

V3-V5 fill version slots reserved in lab 2. For a database where lab 2 V9 was
already applied, explicitly run Flyway with outOfOrder=true to install these
steps before deploying this application; the order bridge is no longer needed.
The schema supplied by a fresh installation always applies V1,V2,V3,V4,V5,V9.
Migration SQL and V1 from previous labs are unchanged.

MigrationApiLoadTest starts the real HTTP application at V4, then applies V5/V9
while multiple clients create and list orders with bounded timeouts. It also
checks old/new write compatibility and backfilled names before the contract.
The reference migration test additionally keeps legacy SQL readers active.
