# TICKET-ADV145 — Kafka Consumer Configuration Review

## Review Decision Matrix

| Area | Finding | Recommendation | Decision | Rationale |
| :--- | :--- | :--- | :--- | :--- |
| **Backpressure & Poll** | `max.poll.records` is left at default (500) | Set `max.poll.records: 50` | **ACCEPT** | Reduces risk of consumer rebalance timeouts during heavy DB insertion loads. |
| **Idempotence** | Producer `enable.idempotence` should be explicitly set to `true` | Set `spring.kafka.producer.properties.enable.idempotence: true` | **REJECT** | Spring Boot 3 / Kafka Client 3.x defaults `enable.idempotence: true` automatically when ACKS=all; redundant explicit setting. |
| **Security & Transport** | Bootstrap server is running on unencrypted PLAINTEXT in dev | Upgrade to `SSL` / `SASL_SSL` for production deployment | **DEFER** | Dev and local Docker environment use PLAINTEXT for training simplicity; TLS/SASL migration deferred to Day 10 deployment. |
| **Error Handling** | DLQ recoverer uses exponential backoff | Add random jitter to backoff | **ACCEPT** | Prevents thundering herd retries on downstream database reconnection. |
| **Observability** | Prometheus metrics binder | Enable `KafkaClientMetrics` binder | **ACCEPT** | Scrapes consumer lag and produce/consume throughput into Grafana dashboards. |

## AI Prompt Used
```text
Review the following Spring Kafka consumer configuration for production readiness:
1. backpressure & poll tuning
2. error handling, retry & DLQ
3. idempotence and exactly-once semantics
4. observability — metrics, logging, traces
5. security — TLS, SASL, ACLs
```
