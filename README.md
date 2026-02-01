# Credoline - Online Loan Application Platform

A production-quality monorepo for an online loan application platform featuring phone verification, real-time decisioning, and comprehensive security measures.

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Frontend (React + Vite)                        │
│                                 localhost:5173                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                      │                                       │
│                                  HTTP/HTTPS                                 │
│                                      │                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                          Backend (Spring Boot 3.2)                          │
│                                localhost:8080                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │ OTP Service │  │ Loan Service│  │Decision Eng.│  │ CRM Client (Mock)   │ │
│  │ (Rate Ltd)  │  │ (JWT Auth)  │  │ (Scoring)   │  │ (Async Processing)  │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────────────┘ │
├─────────────────────────────────────────────────────────────────────────────┤
│                          PostgreSQL 15 (Encrypted)                          │
│                                localhost:5432                                │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 📋 Features

- **7-Step Application Wizard**: Giriş → Məlumatlar → Məbləğ → Təklif → Video KYC → Müqavilə → Nəticə
- **OTP Verification**: SMS-based phone verification with rate limiting and lockout protection
- **JWT Authentication**: Short-lived tokens (15 min) for secure session management
- **Credit Decisioning**: Deterministic scoring engine with explainable reason codes
- **Field Encryption**: AES-256-GCM encryption for sensitive data (FIN, address)
- **CRM Integration**: Async adapter pattern with mock implementation
- **GDPR Compliance**: Consent capture, audit logging, no PII in logs

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose
- Java 21 (for local development)
- Node.js 20+ (for local development)
- Maven 3.9+

### Run with Docker (Recommended)

```bash
# Clone and start all services
cp .env.example .env
docker-compose up -d

# Access the application
# Frontend: http://localhost:80
# Backend API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Run Locally (Development)

#### 1. Start PostgreSQL
```bash
docker-compose up -d postgres
```

#### 2. Start Backend
```bash
cd backend
mvn spring-boot:run
```

#### 3. Start Frontend
```bash
cd frontend
npm install
npm run dev
```

Access the application at http://localhost:5173

## 📡 API Endpoints

### OTP Service (Public)
| Method | Endpoint                                    | Description            |
| ------ | ------------------------------------------- | ---------------------- |
| POST   | `/api/v1/kredo-ms/otp-service/generate-otp` | Generate OTP for phone |
| POST   | `/api/v1/kredo-ms/otp-service/verify-otp`   | Verify OTP and get JWT |

### Loan Application (Requires JWT)
| Method | Endpoint                                                         | Description         |
| ------ | ---------------------------------------------------------------- | ------------------- |
| POST   | `/api/v1/kredo-ms/loan-application/apply-to-loan`                | Submit application  |
| POST   | `/api/v1/kredo-ms/loan-application/{id}/submit-requested-amount` | Submit loan amount  |
| GET    | `/api/v1/kredo-ms/loan-application/{id}/result`                  | Get decision result |

## 📝 Request/Response Examples

### Generate OTP
```bash
curl -X POST http://localhost:8080/api/v1/kredo-ms/otp-service/generate-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+994501234567",
    "channel": "SMS"
  }'

# Response
{
  "requestId": "123e4567-e89b-12d3-a456-426614174000",
  "ttlSeconds": 120
}
```

### Verify OTP
```bash
# Check backend logs for OTP code in dev mode
curl -X POST http://localhost:8080/api/v1/kredo-ms/otp-service/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+994501234567",
    "requestId": "123e4567-e89b-12d3-a456-426614174000",
    "otpCode": "123456"
  }'

# Response
{
  "verified": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresInSeconds": 900
}
```

### Apply for Loan
```bash
curl -X POST http://localhost:8080/api/v1/kredo-ms/loan-application/apply-to-loan \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "phoneNumber": "+994501234567",
    "firstName": "Turan",
    "lastName": "Aliyev",
    "fin": "AZE1234567",
    "dateOfBirth": "1999-05-10",
    "employmentStatus": "EMPLOYED",
    "monthlyIncome": 1200.00,
    "existingMonthlyDebt": 150.00,
    "address": "Baku, Azerbaijan",
    "consent": {
      "termsAccepted": true,
      "privacyAccepted": true
    }
  }'

# Response
{
  "applicationId": "456e7890-e89b-12d3-a456-426614174000",
  "status": "INFO_SUBMITTED"
}
```

### Submit Requested Amount
```bash
curl -X POST http://localhost:8080/api/v1/kredo-ms/loan-application/{applicationId}/submit-requested-amount \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "requestedAmount": 3000.00,
    "termMonths": 12
  }'

# Response
{
  "applicationId": "456e7890-e89b-12d3-a456-426614174000",
  "status": "SCORING"
}
```

### Get Result
```bash
curl http://localhost:8080/api/v1/kredo-ms/loan-application/{applicationId}/result \
  -H "Authorization: Bearer <token>"

# Response
{
  "applicationId": "456e7890-e89b-12d3-a456-426614174000",
  "status": "COMPLETED",
  "decision": "APPROVED",
  "score": 720,
  "approvedAmount": 2500.00,
  "apr": 18.5,
  "reasonCodes": ["INCOME_GOOD", "DTI_OK", "EMPLOYMENT_STABLE", "AGE_PRIME"],
  "lastUpdated": "2026-02-01T12:00:00Z"
}
```

## 🔧 Environment Variables

| Variable                         | Description                    | Default       |
| -------------------------------- | ------------------------------ | ------------- |
| `DB_HOST`                        | PostgreSQL host                | localhost     |
| `DB_PORT`                        | PostgreSQL port                | 5432          |
| `DB_NAME`                        | Database name                  | kredo_loan    |
| `DB_USERNAME`                    | Database user                  | kredo         |
| `DB_PASSWORD`                    | Database password              | kredo_secret  |
| `JWT_SECRET`                     | JWT signing key (min 64 chars) | *dev default* |
| `KREDO_DB_ENCRYPTION_KEY_BASE64` | AES-256 key (base64)           | *dev default* |

### Generate Production Keys
```bash
# Generate JWT secret
openssl rand -base64 64

# Generate encryption key (32 bytes = 256 bits)
openssl rand -base64 32
```

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## 🔒 Security & Compliance

### Data Protection
- **Encryption at Rest**: FIN and address fields encrypted with AES-256-GCM
- **Password Hashing**: OTP codes hashed with BCrypt before storage
- **JWT Security**: Short-lived tokens (15 min), HS256 signing

### Rate Limiting
- OTP endpoints: 10 requests/minute per IP
- Max OTP attempts: 5 per request
- Lockout duration: 5 minutes

### GDPR Compliance
- **Consent Capture**: Terms and privacy acceptance recorded with timestamp
- **No PII Logging**: Sensitive data masked in all logs
- **Audit Trail**: All application status transitions logged
- **Data Minimization**: Only necessary data collected

### Security Headers
- CORS configured for frontend origins only
- X-Request-Id for request tracing
- No sensitive data in error responses

## 📊 Decision Engine

The scoring engine evaluates applications based on:

| Factor     | Weight | Criteria                              |
| ---------- | ------ | ------------------------------------- |
| Income     | High   | Higher income = higher score          |
| DTI Ratio  | High   | < 30% = good, > 50% = reject          |
| Employment | Medium | Employed > Self-employed > Unemployed |
| Age        | Low    | 25-55 optimal range                   |

### Decision Thresholds
- Score ≥ 700: **APPROVED**
- Score 600-699: **MANUAL_REVIEW**
- Score < 600: **REJECTED**

### Hard Rules (Automatic Rejection)
- Age < 18 or > 70
- Zero income
- Missing consent

## 🏗️ Project Structure

```
kredo-loan-platform/
├── docker-compose.yml          # Container orchestration
├── .env.example                # Environment template
├── README.md                   # This file
│
├── backend/
│   ├── pom.xml                 # Maven configuration
│   ├── Dockerfile
│   └── src/
│       ├── main/java/az/kredo/loan/
│       │   ├── KredoLoanApplication.java
│       │   ├── config/         # Security, CORS, Rate limiting
│       │   ├── controller/     # REST endpoints
│       │   ├── dto/            # Request/Response objects
│       │   ├── entity/         # JPA entities
│       │   ├── exception/      # Error handling
│       │   ├── integration/    # CRM client
│       │   ├── repository/     # Data access
│       │   ├── security/       # JWT, Encryption
│       │   └── service/        # Business logic
│       └── main/resources/
│           ├── application.yml
│           └── db/migration/   # Flyway scripts
│
└── frontend/
    ├── package.json
    ├── vite.config.ts
    ├── Dockerfile
    └── src/
        ├── components/         # Reusable UI components
        ├── pages/              # Route pages
        ├── services/           # API client
        ├── types/              # TypeScript definitions
        └── test/               # Component tests
```

## 📈 Status Flow

```
OTP_PENDING → OTP_VERIFIED → INFO_SUBMITTED → SCORING → OFFER_PENDING → OFFER_ACCEPTED → COMPLETED
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Run tests: `mvn test` and `npm test`
4. Submit a pull request

## 📄 License

Proprietary - Credoline © 2026
