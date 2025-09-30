# AudioCloud

A microservices-based music streaming platform built with Spring Boot and Spring Cloud, implementing SAGA pattern for distributed transactions.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Services](#services)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [SAGA Pattern Implementation](#saga-pattern-implementation)
- [Security](#security)
- [Database Schema](#database-schema)
- [Monitoring](#monitoring)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

AudioCloud is a distributed music streaming platform that demonstrates modern microservices architecture patterns. The system handles user registration, authentication, subscription management, payment processing, music content delivery, and notifications through loosely coupled services.

### Key Features

- **User Management**: Registration, authentication, and profile management
- **Subscription System**: Multiple subscription tiers with automatic renewal
- **Payment Processing**: Integration with LiqPay payment gateway
- **Music Streaming**: Audio file storage and streaming capabilities
- **Load Balancing**: Multiple user service instances with custom load balancer
- **SAGA Pattern**: Distributed transaction management with compensation logic
- **Event-Driven Architecture**: Kafka-based asynchronous communication
- **API Gateway**: Centralized entry point with routing and security
- **Service Discovery**: Eureka-based service registration and discovery

## 🏗️ Architecture

AudioCloud follows a microservices architecture with the following patterns:

- **API Gateway Pattern**: Single entry point for all client requests
- **Service Discovery**: Eureka for dynamic service registration
- **Circuit Breaker**: Resilience patterns for fault tolerance
- **Saga Pattern**: Orchestration-based distributed transactions
- **Event Sourcing**: Kafka for event streaming
- **Database per Service**: Each service has its own PostgreSQL schema
- **Centralized Configuration**: Config Server for external configuration

### Service Communication

- **Synchronous**: REST APIs with WebClient and Feign
- **Asynchronous**: Apache Kafka for event streaming
- **Load Balancing**: Spring Cloud LoadBalancer with custom strategy

## 🛠️ Technologies

### Backend

- **Java 17**
- **Spring Boot 3.5.x**
- **Spring Cloud 2025.0.0**
- **Spring Security** with JWT
- **Spring Data JPA**
- **PostgreSQL 16**
- **Apache Kafka 3.x**
- **Redis** for caching
- **Flyway** for database migrations

### Infrastructure

- **Netflix Eureka** - Service Discovery
- **Spring Cloud Gateway** - API Gateway
- **Spring Cloud Config** - Configuration Management
- **Docker & Docker Compose** - Containerization

### Libraries & Tools

- **Lombok** - Boilerplate code reduction
- **MapStruct** - Object mapping
- **SpringDoc OpenAPI** - API documentation
- **JJWT** - JSON Web Token implementation
- **iText PDF** - PDF generation for payment reports

## 📦 Services

### 1. Config Server (Port 8888)

Central configuration management service.

**Responsibilities:**
- Externalized configuration for all services
- Environment-specific properties management
- Configuration refresh without restart

### 2. Discovery Service (Port 8761)

Eureka server for service registration and discovery.

**Responsibilities:**
- Service registration
- Health monitoring
- Service instance load balancing

### 3. API Gateway (Port 8080)

Entry point for all client requests with routing and load balancing.

**Responsibilities:**
- Request routing to appropriate services
- Load balancing between User Service instances
- JWT token validation
- CORS configuration
- Rate limiting

**Endpoints:**
/api/v1/auth/**       → Auth Service
/api/v1/users/**      → User Service (Load Balanced)
/api/v1/subscriptions/** → Subscription Service
/api/v1/payment/**    → Payment Service
/api/v1/music-files/** → Music Content Service
/api/v1/stream/**     → Music Streaming

### 4. Auth Service (Port 8082)

Authentication and authorization service with SAGA orchestration.

**Responsibilities:**
- User registration with SAGA pattern
- User login and JWT token generation
- Password encryption
- SAGA state management
- Compensation logic coordination

**Database Schema:** `auth_service`

**Tables:**
- `auth_credentials` - User authentication data
- `saga_states` - SAGA transaction states

**Key Endpoints:**
- `POST /api/v1/auth/register` - User registration (SAGA orchestrator)
- `POST /api/v1/auth/login` - User authentication

### 5. User Service (Ports 8084, 8081)

User profile and subscription management with dual instances for load balancing.

**Responsibilities:**
- User profile management
- Subscription tracking and expiration
- Automatic subscription renewal
- Payment processing coordination
- Kafka event publishing (user-subscriptions, user-deleted)

**Database Schema:** `user_service`

**Tables:**
- `users` - User profiles and subscription data

**Key Endpoints:**
- `GET /api/v1/users/profile` - Get user profile
- `POST /api/v1/users/saga` - Create user (SAGA step)
- `DELETE /api/v1/users/saga/{userId}` - Delete user (Compensation)
- `PUT /api/v1/users/emails/{email}/subscription/{subscription}` - Update subscription
- `PUT /api/v1/users/emails/{email}/auto-subscription` - Toggle auto-renewal

**Scheduled Tasks:**
- Subscription expiration check (every 1 minute)
- Automatic renewal for expired subscriptions

### 6. Subscription Service (Port 8083)

Subscription plan management.

**Responsibilities:**
- Subscription plan CRUD operations
- Subscription pricing management
- Duration configuration

**Database Schema:** `subscription_service`

**Tables:**
- `subscription_plans` - Available subscription tiers

**Subscription Types:**
- **FREE** - 0 UAH (no expiration)
- **TRIAL** - 0 UAH (100 minutes)
- **OPTIMAL** - 100 UAH (100 minutes)
- **MAXIMUM** - 300 UAH (100 minutes)

**Key Endpoints:**
- `GET /api/v1/subscriptions` - List all plans
- `GET /api/v1/subscriptions/{name}` - Get specific plan
- `POST /api/v1/subscriptions` - Create plan (Admin)
- `PUT /api/v1/subscriptions` - Update plan (Admin)
- `DELETE /api/v1/subscriptions/{name}` - Delete plan (Admin)

### 7. Payment Service (Port 8086)

Payment processing integration with LiqPay.

**Responsibilities:**
- Payment processing via LiqPay API
- Transaction signature generation
- Payment refunds (compensation)
- Kafka notification publishing

**Key Endpoints:**
- `POST /api/v1/payment/{provider}` - Process payment
- `POST /api/v1/payment/refund` - Refund payment
- `POST /api/liqpay/callback` - LiqPay webhook
- `GET /api/liqpay/result` - Payment result page

**Supported Providers:**
- LiqPay

### 8. Music Content Service (Port 8085)

Music file management and streaming with subscription-based access control.

**Responsibilities:**
- Music file metadata management
- Subscription-based access control
- File streaming and download
- Redis caching for user subscriptions
- Kafka event consumption

**Database Schema:** `music_content_service`

**Tables:**
- `music_files` - Music metadata and paths

**Key Endpoints:**
- `GET /api/v1/music-files` - List accessible files (paginated)
- `GET /api/v1/music-files/{name}` - Get file metadata
- `POST /api/v1/music-files` - Upload file metadata (Admin)
- `PUT /api/v1/music-files` - Update file metadata (Admin)
- `DELETE /api/v1/music-files/{name}` - Delete file (Admin)
- `GET /api/v1/stream/{name}` - Stream music file
- `GET /api/v1/stream/download/{name}` - Download music file

**Access Control:**
Configuration from Config Server defines subscription access rules.

### 9. Notification Service (Port 8087)

Email notification service with PDF generation.

**Responsibilities:**
- Email notifications via SMTP
- PDF payment reports generation
- Kafka event consumption
- Template-based email composition

**Notification Types:**
- Registration success with payment receipt
- Payment confirmation
- Subscription renewal confirmation

**Key Endpoints:**
- `POST /api/test/email` - Test email sending
- `GET /api/test/ping` - Health check

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.9+
- PostgreSQL 16
- Apache Kafka 3.x
- Redis
- Docker & Docker Compose (optional)

### Environment Variables

Create a `.env` file in the root directory:
```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=AudioCloud
DB_USER=postgres
DB_PASSWORD=postgres

# JWT Secret (Base64 encoded)
JWT_SECRET=your-base64-encoded-secret-key

# LiqPay Credentials
LIQPAY_PUBLIC_KEY=your-liqpay-public-key
LIQPAY_PRIVATE_KEY=your-liqpay-private-key

# Email Configuration
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password

# SAGA Internal API Key
SAGA_INTERNAL_API_KEY=saga-internal-key-9f2e8d7c6b5a4918273645ebf0c1a2d3e4f5g6h7i8j9k0l1m2n3o4p5q6r7s8t9u0v1w2x3y4z5
Database Setup

Create PostgreSQL database:

sqlCREATE DATABASE AudioCloud;

Flyway migrations will automatically create schemas and tables on first startup:

auth_service
user_service
subscription_service
music_content_service



Running with Docker Compose
Start infrastructure services:
bashdocker-compose up -d
This starts:

PostgreSQL
Zookeeper
Kafka
Kafka UI (http://localhost:8088)
Redis

Running Services Manually
Start services in the following order:

Config Server

bashcd config-server/config-server
./mvnw spring-boot:run

Discovery Service

bashcd discovery-service/discovery-service
./mvnw spring-boot:run

API Gateway

bashcd api-gateway/api-gateway
./mvnw spring-boot:run

Auth Service

bashcd auth-service/auth-service
./mvnw spring-boot:run

Subscription Service

bashcd subscription-service/subscription-service
./mvnw spring-boot:run

Payment Service

bashcd payment-service/payment-service
./mvnw spring-boot:run

User Service (Instance 1)

bashcd user-service/user-service
./mvnw spring-boot:run -Dserver.port=8084

User Service (Instance 2)

bashcd user-service/user-service
./mvnw spring-boot:run -Dserver.port=8081

Music Content Service

bashcd music-content-service/music-content-service
./mvnw spring-boot:run

Notification Service

bashcd notification-service/notification-service
./mvnw spring-boot:run
Verify Services
Check Eureka Dashboard: http://localhost:8761
All services should be registered and UP.
⚙️ Configuration
Config Server Configuration
Update config-server/config-server/src/main/resources/application.yml:
yamlspring:
  cloud:
    config:
      server:
        native:
          search-locations: file:///path/to/config/directory
Subscription Access Rules
Create music-content-service.yml in config directory:
yamlsubscription:
  access-rules:
    FREE:
      - FREE
    TRIAL:
      - FREE
      - TRIAL
    OPTIMAL:
      - FREE
      - TRIAL
      - OPTIMAL
    MAXIMUM:
      - FREE
      - TRIAL
      - OPTIMAL
      - MAXIMUM
📚 API Documentation
Swagger UI is available for each service:

API Gateway: http://localhost:8080/swagger-ui.html
Auth Service: http://localhost:8082/swagger-ui.html
User Service: http://localhost:8084/swagger-ui.html
Subscription Service: http://localhost:8083/swagger-ui.html
Music Content Service: http://localhost:8085/swagger-ui.html

Authentication Flow

Register New User

httpPOST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "userEmail": "user@example.com",
  "password": "securePassword123",
  "bankCardNumber": "4242424242424242",
  "bankCardNumberCVV": "123",
  "bankCardNumberExpired": "12/25"
}
Response:
json{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "subscriptionName": "TRIAL",
  "bankCardNumber": "4242424242424242",
  "bankCardNumberCVV": "123",
  "bankCardNumberExpired": "12/25"
}

Login

httpPOST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "userEmail": "user@example.com",
  "password": "securePassword123"
}
Response:
json"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

Access Protected Resources

httpGET http://localhost:8080/api/v1/users/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Music Streaming

List Available Music

httpGET http://localhost:8080/api/v1/music-files?page=0&size=20
Authorization: Bearer <token>

Stream Music

httpGET http://localhost:8080/api/v1/stream/{musicFileName}
Authorization: Bearer <token>

Download Music

httpGET http://localhost:8080/api/v1/stream/download/{musicFileName}
Authorization: Bearer <token>
🔄 SAGA Pattern Implementation
The user registration process implements the Orchestration-based SAGA pattern with compensation logic.
SAGA Flow
Orchestrator: Auth Service
Steps:

Step 0: Create Auth User

Create user in auth_credentials table
Store hashed password
Generate UUID


Step 1: Get Subscription

HTTP GET to Subscription Service
Retrieve TRIAL subscription details
Compensation: None (read-only)


Step 2: Process Payment

HTTP POST to Payment Service
Process payment via LiqPay
Store transaction ID
Compensation: Delete auth user


Step 3: Create User

HTTP POST to User Service /api/v1/users/saga
Create user profile
Set subscription and expiration
Compensation:

Refund payment
Delete user from User Service
Delete auth user




Step 4: Send Notification

Kafka message to notifications topic
Email with PDF payment receipt
Compensation: None (non-critical)



SAGA State Management
Database Table: auth_service.saga_states
Columns:

saga_id - Unique SAGA transaction ID
user_id - Associated user UUID
user_email - User email
status - STARTED, IN_PROGRESS, COMPLETED, COMPENSATING, FAILED
current_step - Current SAGA step
payment_transaction_id - LiqPay transaction ID
subscription_name - Selected subscription
error_message - Failure reason
request_data - Original request JSON
created_at - SAGA start time
updated_at - Last modification time
completed_at - Completion time
retry_count - Retry attempts

Compensation Logic
CompensationHandler.java coordinates rollback:
javapublic boolean compensatePayment(SagaState sagaState) {
    // HTTP POST to Payment Service
    POST /api/v1/payment/refund
    {
        "transactionId": sagaState.getPaymentTransactionId(),
        "userEmail": sagaState.getUserEmail(),
        "reason": "SAGA_COMPENSATION"
    }
}

public boolean compensateUser(SagaState sagaState) {
    // HTTP DELETE to User Service
    DELETE /api/v1/users/saga/{userId}
    Header: X-Internal-API-Key: <saga-key>
}
Internal API Security
SAGA endpoints are protected with internal API key:
