# Purchase Ledger & Reconciliation Service

A Spring Boot service that records purchase events exactly once, applies country-specific tax rules, and reconciles its ledger against an independent event history every day.

It models a core problem in commerce systems: every purchase has to be accounted for correctly across multiple stores, even when events are delivered more than once, arrive out of order, or fail partway through.

## What it does

- **Consumes purchase events from Kafka** and records each one in a PostgreSQL ledger.
- **Idempotent processing.** Each event carries an `eventId`. The ledger insert uses `ON CONFLICT (event_id) DO NOTHING`, so retries, consumer rebalances, and concurrent deliveries never double-count a transaction.
- **Country tax rules.** Prices are split into net, tax, and gross using per-country rules, covering both tax-inclusive (VAT-style) and tax-exclusive pricing. Rules are data, so they can change at runtime when local laws change.
- **Exact money math.** All amounts are `BigDecimal` at scale 2 with banker's rounding. A database `CHECK` constraint enforces `net + tax = gross`.
- **Raw event history in Cassandra**, partitioned by purchase date and clustered by `eventId`, so duplicate writes are upserts and a full day reads from a single partition.
- **Daily reconciliation.** A scheduled job compares the ledger against the event history and reports events missing from either store and any per-country totals that disagree.
- **Failure handling.** Transient errors are retried with exponential backoff. After retries run out, the record goes to a dead-letter topic (`purchase-events.DLT`). Errors that retrying can't fix, such as malformed payloads or an unknown country, skip straight to the dead-letter topic.

## Architecture

```
Kafka (purchase-events) ──► PurchaseEventListener ──► LedgerService ──► PostgreSQL ledger (system of record)
            │                                              │
            └── on repeated failure ──► purchase-events.DLT └────────► Cassandra event history
                                                                              │
                         DailyReconciliationJob / GET /api/v1/reconciliation ◄┘  compares both stores
```

The domain logic (`domain` package) is plain Java with no framework dependencies. Spring, Kafka, JPA, and Cassandra live in `adapter` classes behind two ports, `LedgerStore` and `EventHistoryStore`. The core logic is unit-tested without any infrastructure.

## Run it locally

Requirements: Java 21, Maven, Docker.

```bash
docker compose up -d          # PostgreSQL, Cassandra (and keyspace), Kafka
mvn spring-boot:run
```

Send a purchase event:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 --topic purchase-events
{"eventId":"e-1001","purchaseId":"p-1","accountId":"acct-1","productId":"app-42","countryCode":"DE","currency":"EUR","listPrice":9.99,"purchasedAt":"2026-10-14T12:00:00Z"}
```

Query it:

```bash
curl localhost:8080/api/v1/ledger/e-1001
curl "localhost:8080/api/v1/reconciliation?date=2026-10-14"
```

Send the same event again and the ledger still holds one entry.

## Tests

```bash
mvn test
```

Unit tests cover tax calculation for inclusive and exclusive countries, balancing across many prices, runtime rule changes, duplicate and concurrent delivery (50 concurrent deliveries of one event record exactly one entry), and reconciliation catching missing events and altered amounts.

## Roadmap

- Integration tests with Testcontainers for PostgreSQL, Cassandra, and Kafka
- Refunds and partial refunds as offsetting ledger entries
- JMeter load tests and Grafana dashboards for throughput, consumer lag, and reconciliation results
- Multi-currency reporting totals

The tax rates in `TaxRules.withSampleDefaults()` are sample values for development only, not tax guidance.
