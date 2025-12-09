# 🏦 Microservices Banking System

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=flat-square)
![Microservices](https://img.shields.io/badge/Architecture-Microservices-purple?style=flat-square)

A comprehensive distributed banking application built using **Spring Boot**, **Spring Cloud**, and **Docker**. This system simulates core banking operations (Accounts, Deposits, Withdrawals, Transfers) using a resilient microservices architecture.

---

## 🏗️ Architecture

The system follows a microservices pattern with a central API Gateway and Service Registry.

```mermaid
graph TD
    User(User / Postman) -->|HTTP Request| Gateway[API Gateway <br> Port: 8080]
    
    subgraph Infrastructure
        Registry[Eureka Server <br> Port: 8761]
        Mongo[(MongoDB <br> Port: 27017)]
    end

    Gateway -->|Routes /api/accounts| AccountService
    Gateway -->|Routes /api/transactions| TransactionService
    
    AccountService[Account Service <br> Port: 8081] -->|Read/Write| Mongo
    
    TransactionService[Transaction Service <br> Port: 8082] -->|Persist Logs| Mongo
    TransactionService -->|Feign Client| AccountService
    TransactionService -->|Feign Client| NotificationService
    
    NotificationService[Notification Service <br> Port: 8083]

    AccountService -.->|Register| Registry
    TransactionService -.->|Register| Registry
    NotificationService -.->|Register| Registry
    Gateway -.->|Register| Registry

Service Name,Port,Description
Service Registry,8761,Eureka Server for service discovery & registration.
API Gateway,8080,Single entry point. Routes requests to microservices.
Account Service,8081,"Manages customer accounts, balances, and status (MongoDB)."
Transaction Service,8082,"Orchestrates Deposits, Withdrawals, and Transfers."
Notification Service,8083,Simulates sending emails for transaction alerts.

Tech Stack
Core: Java 17, Spring Boot 3.x

Database: MongoDB

Service Discovery: Netflix Eureka

Routing: Spring Cloud Gateway

Communication: OpenFeign (REST Client)

Resilience: Resilience4j (Circuit Breaker)

Containerization: Docker & Docker Compose

Observability: Micrometer Tracing (SLF4J + Logback)

🚀 Setup & Run Instructions
Prerequisites
Java 17+

Maven

Docker & Docker Compose

Option 1: Run with Docker (Recommended ⭐️)
This builds the application and starts all services + MongoDB automatically.

Build the JAR files:

Bash

mvn clean package -DskipTests
Start the environment:

Bash

docker-compose up --build
Verify Status:

Wait approx. 60 seconds for all services to register.

Visit Eureka Dashboard: http://localhost:8761

🧪 API Documentation
Base URL: http://localhost:8080 (API Gateway)

1. Account Operations
Create Account

POST /api/accounts

Body:

JSON

{
  "holderName": "John Doe"
}
Get Account Details

GET /api/accounts/{accountNumber}

Activate/Deactivate Account

PUT /api/accounts/{accountNumber}/status

Body: { "status": "INACTIVE" }

2. Transaction Operations
Deposit Money

POST /api/transactions/deposit

Body:

JSON

{
  "accountNumber": "JD1234",
  "amount": 1000.0
}
Withdraw Money

POST /api/transactions/withdraw

Body:

JSON

{
  "accountNumber": "JD1234",
  "amount": 200.0
}
Transfer Money

POST /api/transactions/transfer

Body:

JSON

{
  "sourceAccount": "JD1234",
  "destinationAccount": "AS5678",
  "amount": 300.0
}
Transaction History

GET /api/transactions/account/{accountNumber}

🛡️ Fault Tolerance (Circuit Breaker)
This project uses Resilience4j to handle cascading failures.

How to test:

Stop the Account Service container.

Attempt a Deposit or Transfer via Postman.

Expected Result:

The request will not hang or timeout indefinitely.

The Transaction Service will return a default/fallback response.

The transaction will be logged as "FAILED - SERVICE UNAVAILABLE" in the database.

📂 Project Structure
Plaintext

banking-microservices/
├── service-registry/      # Eureka Server
├── api-gateway/           # Spring Cloud Gateway
├── account-service/       # Account Management (Mongo)
├── transaction-service/   # Logic Orchestrator (Mongo + Feign)
├── notification-service/  # Email Simulator
├── docker-compose.yml     # Container Orchestration
└── README.md              # Documentation
