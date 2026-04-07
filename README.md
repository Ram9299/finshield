![CI](https://github.com/Ram9299/finshield/actions/workflows/ci.yml/badge.svg)

# FinShield – Fraud Detection Platform

FinShield is an **event-driven fraud detection platform** designed to simulate and detect suspicious financial transactions using **rule-based scoring + ML probability models**.

The system uses **Kafka-based asynchronous processing**, **microservices architecture**, and **observability features** to replicate how real fintech fraud detection systems operate in production environments.

---

# Architecture Overview

FinShield follows an **event-driven architecture**:

1. Transactions are created via API or Simulator.
2. A `TransactionCreatedEvent` is published to Kafka.
3. A Kafka consumer processes the event asynchronously.
4. Fraud signals are generated using:
   - rule-based detection
   - ML scoring microservice
5. Risk score is calculated and stored.
6. Alerts are generated if suspicious activity is detected.
7. Risk status is available via REST APIs.

---

# Tech Stack

## Backend
- Java 17
- Spring Boot 3
- Spring Data JPA
- Spring Kafka
- Flyway migrations
- Hibernate ORM

## Data & Messaging
- PostgreSQL 16
- Redis 7 (caching / fast lookup)
- Apache Kafka 7.6.1
- Zookeeper

## ML Microservice
- Python
- FastAPI
- Uvicorn

## Infrastructure
- Docker
- Docker Compose

## Observability
- Spring Boot Actuator
- Health checks (readiness/liveness)
- Logs for Kafka producer & consumer

---

# System Design

```
Client / Postman
        │
        ▼
Transaction API
        │
        ▼
PostgreSQL (transactions)
        │
        ▼
Kafka Producer
        │
        ▼
Kafka Topic: txn.created.v1
        │
        ▼
Kafka Consumer
        │
        ├── Rule Engine
        ├── ML Service (FastAPI)
        └── Risk Scoring Service
                │
                ▼
        PostgreSQL (risk_scores)
                │
                ▼
        Alerts Service
```

---

# Project Structure

```
finshield
│
├── src/main/java/com/finshield
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   ├── kafka
│   │   ├── TransactionEventPublisher
│   │   ├── TransactionCreatedConsumer
│   │   └── events
│   │
│   ├── fraud
│   │   ├── FraudDetectionService
│   │   ├── RiskScoringService
│   │   └── AlertService
│   │
│   └── config
│
├── ml-service
│   ├── app.py
│   ├── model logic
│
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

# Key Features

## 1. Event Driven Processing

Transactions are processed asynchronously using Kafka.

```
Transaction Created → Kafka Event → Consumer → Risk Score → Alert
```

Benefits:
- scalable
- resilient
- loosely coupled services
- production-style architecture

---

## 2. Fraud Detection Signals

Examples of fraud signals:

| Signal | Description |
|--------|-------------|
| RAPID_TXN | Multiple transactions in short time |
| GEO_ANOMALY | Transactions from different countries quickly |
| LARGE_AMOUNT | High value transaction |
| ML_SCORE | Probability from ML model |

---

## 3. Risk Scoring Logic

Risk score is computed using:

```
Total Risk Score =
    Rule Score
  + ML Probability Score
```

Decision categories:

| Score | Decision |
|------|----------|
| 0–30 | SAFE |
| 31–60 | REVIEW |
| 61+ | BLOCK |

---

## 4. ML Microservice

ML service predicts fraud probability using FastAPI.

Endpoint:

```
POST /predict
```

Example request:

```json
{
  "amount": 1200,
  "country": "US",
  "txn_type": "ONLINE"
}
```

Example response:

```json
{
  "fraud_probability": 0.72
}
```

---

## 5. Observability

Health endpoint:

```
GET /actuator/health
```

Readiness:

```
GET /actuator/health/readiness
```

Shows status of:
- database
- redis
- kafka
- application

---

# Database Schema

## transactions

| column | type |
|-------|------|
| id | uuid |
| account_id | uuid |
| amount | decimal |
| country | varchar |
| currency | varchar |
| device_id | varchar |
| ip_address | varchar |
| txn_type | varchar |
| created_at | timestamp |

---

## risk_scores

| column | type |
|-------|------|
| id | uuid |
| transaction_id | uuid |
| rules_score | int |
| ml_score | int |
| ml_probability | decimal |
| total_score | int |
| decision | varchar |
| evaluated_at | timestamp |

---

# Running the Project

## 1. Clone repository

```
git clone <repo-url>
cd finshield
```

---

## 2. Start services

```
docker compose up --build
```

Services started:

| service | port |
|--------|------|
| app | 8080 |
| postgres | 5432 |
| kafka | 29092 |
| redis | 6379 |
| ml-service | 8000 |
| zookeeper | 2181 |

---

## 3. Verify containers

```
docker compose ps
```

---

# API Endpoints

## Create User

```
POST /api/users
```

---

## Create Account

```
POST /api/accounts
```

---

## Create Transaction

```
POST /api/transactions
```

Response:

```json
{
  "transactionId": "uuid",
  "status": "PENDING"
}
```

---

## Get Risk Score

```
GET /api/transactions/{txnId}/risk
```

Example response:

```json
{
  "transactionId": "uuid",
  "status": "COMPLETED",
  "totalScore": 55,
  "decision": "REVIEW",
  "signals": [
    "RAPID_TXN",
    "ML_HIGH_PROBABILITY"
  ]
}
```

---

## Alerts

```
GET /api/alerts?status=OPEN
```

---

## Simulator

Generate test transactions automatically:

```
POST /api/sim/start
```

---

# Kafka Topic

```
txn.created.v1
```

Event format:

```json
{
  "transactionId": "uuid"
}
```

---

# Testing Kafka

Produce event:

```
docker exec -it finshield-kafka-1 bash -lc "kafka-console-producer --bootstrap-server kafka:9092 --topic txn.created.v1"
```

Consume:

```
docker exec -it finshield-kafka-1 bash -lc "kafka-console-consumer --bootstrap-server kafka:9092 --topic txn.created.v1 --from-beginning"
```

---

# Example Flow

1. Create user
2. Create account
3. Create multiple transactions quickly
4. Call risk endpoint
5. Observe fraud signals

---

# Future Improvements

- add real ML model training
- add feature store
- add Grafana dashboards
- add Kubernetes deployment
- add retry & dead-letter queue
- add rate limiting
- add authentication
- add streaming analytics

---

# Learning Outcomes

This project demonstrates:

- event driven microservices
- kafka integration
- ml service integration
- docker orchestration
- observability patterns
- async processing
- real-world fintech architecture

---

# Author

**Konda Venkata Rami Reddy**  
Associate Software Engineer  

Tech stack:
Java • Python • Kafka • React • PostgreSQL • LangChain • Docker
