# OrderFlow

Projeto de estudo: **Java + Spring Boot + AWS (SQS) + CI/CD + Terraform + Observability**.

Este repositório contém o serviço **order-api** com:
- API REST para criar pedidos e alterar status
- Publicação de eventos de mudança de status em **SQS**
- Consumer SQS com **idempotência** (tabela `processed_events`)
- Persistência de métricas de consumo (timestamps + payload) em `processed_events`
- Observability local com **Prometheus + Grafana**
- Banco local com **Postgres (Docker)**

> Este README está focado em **rodar local**. Terraform/infra fica para a próxima etapa.

---

## Estrutura

- `services/order-api` — serviço Spring Boot
- `services/order-api/src/main/resources/db/migration` — migrações Flyway
- `services/order-api/docker-compose.yml` — Postgres local (e opcionalmente observability, se você tiver no compose)
- `services/order-api/observability/` — (se existir no repo) Prometheus/Grafana provisionados

---

## Requisitos

- Java 17
- Docker + Docker Compose
- (Opcional) AWS CLI configurado **apenas** se for usar SQS real
  - `aws configure`
  - Região: `sa-east-1`

---

## Variáveis sensíveis (NÃO subir no Git)

Crie um arquivo **local** com variáveis de ambiente e não comite.

### 1) Arquivo `.env` (recomendado)

Crie `services/order-api/.env` usando um template (ex.: `order-api.env` que você gerou).

Exemplo (ajuste conforme seu ambiente):

```dotenv
# App
SPRING_PROFILES_ACTIVE=local
APP_EVENTS_PUBLISHER=log

# Postgres
DB_URL=jdbc:postgresql://localhost:5432/orderflow
DB_USER=orderflow
DB_PASS=orderflow

# AWS (somente se usar SQS real)
AWS_REGION=sa-east-1
AWS_PROFILE=default
SQS_ORDER_EVENTS_QUEUE_URL=
SQS_ORDER_EVENTS_DLQ_URL=
```

### 2) Garanta que não vai para o git

No `.gitignore` do repo (ou do serviço), inclua:

```gitignore
# Local secrets
services/order-api/.env
*.env
.env
```

---

## Rodando Postgres (local)

Seu `docker-compose` atual:

```yaml
services:
  postgres:
    image: postgres:16
    container_name: orderflow-postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: orderflow
      POSTGRES_USER: orderflow
      POSTGRES_PASSWORD: orderflow
    volumes:
      - orderflow_pgdata:/var/lib/postgresql/data

volumes:
  orderflow_pgdata:
```

Subir o Postgres:

```bash
cd services/order-api
docker compose up -d postgres
```

Conectar no banco:

```bash
psql "postgresql://orderflow:orderflow@localhost:5432/orderflow"
```

Ver tabelas:

```sql
\dt
```

Reset total do banco (apaga volume):

```bash
docker compose down -v
docker compose up -d postgres
```

---

## Rodar o `order-api` (fora do Docker)

### 1) Build/Test

```bash
cd services/order-api
./mvnw clean test
```

### 2) Rodar com profile `local`

**Opção A — via variável de ambiente:**

```bash
export SPRING_PROFILES_ACTIVE=local
./mvnw spring-boot:run
```

**Opção B — carregando `.env` no shell:**

```bash
cd services/order-api
set -a; source .env; set +a
./mvnw spring-boot:run
```

---

## Endpoints

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger`
- Actuator: `http://localhost:8080/actuator`
- Métricas Prometheus: `http://localhost:8080/actuator/prometheus`

---

## Profiles e modo de eventos

A aplicação suporta modo de publicação/consumo via property:

- `app.events.publisher=log`
  - **não usa AWS**
  - apenas loga eventos no publisher

- `app.events.publisher=sqs`
  - publica e consome eventos no **SQS real**
  - recomendado só quando você quer validar integração com AWS

> Dica: deixe `log` como default e ative `sqs` só quando precisar.

---

## Teste rápido (API)

### 1) Criar pedido

```bash
curl -s -X POST http://localhost:8080/orders   -H "Content-Type: application/json"   -d '{"customerCode":"CUST-001","amount":10}'
```

### 2) Aprovar pedido

Substitua o `<ID>` retornado:

```bash
curl -s -X PATCH "http://localhost:8080/orders/<ID>/status"   -H "Content-Type: application/json"   -d '{"status":"APPROVED"}'
```

---

## Teste rápido (SQS) — somente se `app.events.publisher=sqs`

### Pré-requisito

Você precisa ter:
- AWS CLI configurado (`aws configure`)
- Permissões para **SendMessage** na fila principal
- Permissões para **ReceiveMessage/DeleteMessage** na DLQ (se quiser ler/apagar DLQ)

### Enviar mensagem manual na fila

> Use a URL da fila via variável (recomendado), não hardcode no README.

```bash
aws sqs send-message   --queue-url "$SQS_ORDER_EVENTS_QUEUE_URL"   --message-body '{"orderId":"X","from":"CREATED","to":"APPROVED","occurredAt":"2026-03-05T00:00:00Z"}'   --region "$AWS_REGION"
```

### Ver mensagens na DLQ

```bash
aws sqs receive-message   --queue-url "$SQS_ORDER_EVENTS_DLQ_URL"   --max-number-of-messages 1   --wait-time-seconds 2   --region "$AWS_REGION"
```

### Deletar mensagem da DLQ (jeito seguro)

```bash
RH=$(aws sqs receive-message   --queue-url "$SQS_ORDER_EVENTS_DLQ_URL"   --max-number-of-messages 1   --wait-time-seconds 2   --region "$AWS_REGION"   --query "Messages[0].ReceiptHandle" --output text)

aws sqs delete-message   --queue-url "$SQS_ORDER_EVENTS_DLQ_URL"   --receipt-handle "$RH"   --region "$AWS_REGION"
```

---

## Consultas SQL úteis (métricas e latência)

### Últimos eventos processados

```bash
psql "postgresql://orderflow:orderflow@localhost:5432/orderflow"   -c "select event_id, occurred_at, processed_at from processed_events order by processed_at desc limit 10;"
```

### Latência de consumo (occurred_at -> processed_at)

```bash
psql "postgresql://orderflow:orderflow@localhost:5432/orderflow"   -c "select event_id, occurred_at, processed_at, (processed_at - occurred_at) as consume_latency
      from processed_events
      where occurred_at is not null
      order by processed_at desc
      limit 20;"
```

### P50 / P90 / Máximo (última hora)

- **p50**: mediana (50% dos eventos têm latência <= p50)
- **p90**: 90% dos eventos têm latência <= p90

```bash
psql "postgresql://orderflow:orderflow@localhost:5432/orderflow"   -c "select
        count(*) as total,
        percentile_cont(0.5) within group (order by (processed_at - occurred_at)) as p50,
        percentile_cont(0.9) within group (order by (processed_at - occurred_at)) as p90,
        max(processed_at - occurred_at) as max_latency
      from processed_events
      where occurred_at is not null
        and processed_at > now() - interval '1 hour';"
```

### Latência SQS (se você persistiu `sqs_sent_at` e `sqs_received_at`)

```bash
psql "postgresql://orderflow:orderflow@localhost:5432/orderflow"   -c "select
        event_id,
        sqs_sent_at,
        sqs_received_at,
        (sqs_received_at - sqs_sent_at) as sqs_latency
      from processed_events
      where sqs_sent_at is not null and sqs_received_at is not null
      order by processed_at desc
      limit 20;"
```

### End-to-end (sent -> processed)

```bash
psql "postgresql://orderflow:orderflow@localhost:5432/orderflow"   -c "select
        event_id,
        sqs_sent_at,
        processed_at,
        (processed_at - sqs_sent_at) as end_to_end
      from processed_events
      where sqs_sent_at is not null
      order by processed_at desc
      limit 20;"
```

---

## Observability (Prometheus/Grafana) — local

Se o seu `docker-compose` também inclui observability:

- Prometheus: `http://localhost:9090`
  - target esperado: `order-api:8080`
  - métricas: `http://order-api:8080/actuator/prometheus` (dentro da rede docker)
- Grafana: `http://localhost:3000`
  - login padrão: `admin / admin`

---

## Logs

```bash
# (se estiver rodando no Docker)
docker logs -f orderflow-order-api
docker logs -f orderflow-postgres
docker logs -f orderflow-prometheus
docker logs -f orderflow-grafana
```

---

## Troubleshooting rápido

### Porta ocupada (8080/5432/9090/3000)

```bash
lsof -i :8080
lsof -i :5432
```

### Reset do banco

```bash
cd services/order-api
docker compose down -v
docker compose up -d postgres
```

---
## json para usar no grafana

Como importar

Grafana → Dashboards → New → Import → cola o JSON abaixo → Load → em “Prometheus” selecione sua datasource → Import.


{
"annotations": {
"list": [
{
"builtIn": 1,
"datasource": { "type": "grafana", "uid": "-- Grafana --" },
"enable": true,
"hide": true,
"iconColor": "rgba(0, 211, 255, 1)",
"name": "Annotations & Alerts",
"type": "dashboard"
}
]
},
"editable": true,
"fiscalYearStartMonth": 0,
"graphTooltip": 0,
"id": null,
"links": [],
"liveNow": false,
"panels": [
{
"datasource": { "type": "prometheus", "uid": "${DS_PROMETHEUS}" },
"description": "",
"fieldConfig": { "defaults": { "unit": "reqps" }, "overrides": [] },
"gridPos": { "h": 8, "w": 8, "x": 0, "y": 0 },
"id": 1,
"options": {
"legend": { "calcs": [], "displayMode": "list", "placement": "bottom", "showLegend": true },
"tooltip": { "mode": "single", "sort": "none" }
},
"targets": [
{
"editorMode": "code",
"expr": "sum(rate(http_server_requests_seconds_count{job=\"order-api\"}[1m]))",
"legendFormat": "rps",
"range": true,
"refId": "A"
}
],
"title": "RPS",
"type": "timeseries"
},
{
"datasource": { "type": "prometheus", "uid": "${DS_PROMETHEUS}" },
"fieldConfig": { "defaults": { "unit": "percent" }, "overrides": [] },
"gridPos": { "h": 8, "w": 8, "x": 8, "y": 0 },
"id": 2,
"options": {
"legend": { "calcs": [], "displayMode": "list", "placement": "bottom", "showLegend": true },
"tooltip": { "mode": "single", "sort": "none" }
},
"targets": [
{
"editorMode": "code",
"expr": "100 * sum(rate(http_server_requests_seconds_count{job=\"order-api\", status=~\"5..\"}[5m])) / sum(rate(http_server_requests_seconds_count{job=\"order-api\"}[5m]))",
"legendFormat": "5xx %",
"range": true,
"refId": "A"
}
],
"title": "Error rate 5xx (%)",
"type": "timeseries"
},
{
"datasource": { "type": "prometheus", "uid": "${DS_PROMETHEUS}" },
"fieldConfig": { "defaults": { "unit": "ms" }, "overrides": [] },
"gridPos": { "h": 8, "w": 8, "x": 16, "y": 0 },
"id": 3,
"options": {
"legend": { "calcs": [], "displayMode": "list", "placement": "bottom", "showLegend": true },
"tooltip": { "mode": "single", "sort": "none" }
},
"targets": [
{
"editorMode": "code",
"expr": "1000 * histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{job=\"order-api\"}[5m])))",
"legendFormat": "p95 ms",
"range": true,
"refId": "A"
}
],
"title": "Latency p95 (ms)",
"type": "timeseries"
},
{
"datasource": { "type": "prometheus", "uid": "${DS_PROMETHEUS}" },
"fieldConfig": { "defaults": { "unit": "ms" }, "overrides": [] },
"gridPos": { "h": 8, "w": 8, "x": 0, "y": 8 },
"id": 4,
"options": {
"legend": { "calcs": [], "displayMode": "list", "placement": "bottom", "showLegend": true },
"tooltip": { "mode": "single", "sort": "none" }
},
"targets": [
{
"editorMode": "code",
"expr": "1000 * histogram_quantile(0.50, sum by (le) (rate(http_server_requests_seconds_bucket{job=\"order-api\"}[5m])))",
"legendFormat": "p50 ms",
"range": true,
"refId": "A"
}
],
"title": "Latency p50 (ms)",
"type": "timeseries"
},
{
"datasource": { "type": "prometheus", "uid": "${DS_PROMETHEUS}" },
"fieldConfig": { "defaults": { "unit": "mbytes" }, "overrides": [] },
"gridPos": { "h": 8, "w": 8, "x": 8, "y": 8 },
"id": 5,
"options": {
"legend": { "calcs": [], "displayMode": "list", "placement": "bottom", "showLegend": true },
"tooltip": { "mode": "single", "sort": "none" }
},
"targets": [
{
"editorMode": "code",
"expr": "jvm_memory_used_bytes{job=\"order-api\", area=\"heap\"} / 1024 / 1024",
"legendFormat": "heap MB",
"range": true,
"refId": "A"
}
],
"title": "JVM Heap used (MB)",
"type": "timeseries"
},
{
"datasource": { "type": "prometheus", "uid": "${DS_PROMETHEUS}" },
"fieldConfig": { "defaults": { "unit": "percent" }, "overrides": [] },
"gridPos": { "h": 8, "w": 8, "x": 16, "y": 8 },
"id": 6,
"options": {
"legend": { "calcs": [], "displayMode": "list", "placement": "bottom", "showLegend": true },
"tooltip": { "mode": "single", "sort": "none" }
},
"targets": [
{
"editorMode": "code",
"expr": "100 * rate(process_cpu_usage{job=\"order-api\"}[1m])",
"legendFormat": "cpu %",
"range": true,
"refId": "A"
}
],
"title": "Process CPU (%)",
"type": "timeseries"
},
{
"datasource": { "type": "prometheus", "uid": "${DS_PROMETHEUS}" },
"fieldConfig": { "defaults": { "unit": "none" }, "overrides": [] },
"gridPos": { "h": 8, "w": 8, "x": 0, "y": 16 },
"id": 7,
"options": {
"legend": { "calcs": [], "displayMode": "list", "placement": "bottom", "showLegend": true },
"tooltip": { "mode": "single", "sort": "none" }
},
"targets": [
{
"editorMode": "code",
"expr": "jvm_threads_live_threads{job=\"order-api\"}",
"legendFormat": "threads",
"range": true,
"refId": "A"
}
],
"title": "JVM Threads (live)",
"type": "timeseries"
},
{
"datasource": { "type": "prometheus", "uid": "${DS_PROMETHEUS}" },
"fieldConfig": { "defaults": { "unit": "none" }, "overrides": [] },
"gridPos": { "h": 8, "w": 8, "x": 8, "y": 16 },
"id": 8,
"options": {
"legend": { "calcs": [], "displayMode": "list", "placement": "bottom", "showLegend": true },
"tooltip": { "mode": "single", "sort": "none" }
},
"targets": [
{
"editorMode": "code",
"expr": "hikaricp_connections_active{job=\"order-api\"}",
"legendFormat": "active",
"range": true,
"refId": "A"
}
],
"title": "DB Connections (active)",
"type": "timeseries"
}
],
"refresh": "10s",
"schemaVersion": 39,
"style": "dark",
"tags": ["orderflow", "local", "prometheus", "grafana"],
"templating": {
"list": [
{
"current": { "selected": false, "text": "Prometheus", "value": "${DS_PROMETHEUS}" },
"hide": 0,
"label": "Prometheus",
"name": "DS_PROMETHEUS",
"options": [],
"query": "prometheus",
"refresh": 1,
"type": "datasource"
}
]
},
"time": { "from": "now-6h", "to": "now" },
"timepicker": {},
"timezone": "browser",
"title": "OrderFlow - order-api (local)",
"uid": null,
"version": 1
}