# Maricopa County Property Reports

A full-stack web application for tracking Maricopa County property ownership details, tax assessments, and valuation history. Users can set up periodic email reports delivered on a configurable schedule.

The backend is implemented as **Java Spring Boot microservices** behind an API gateway. (An earlier FastAPI monolith remains in git history under `backend/`.)

## Architecture

```
              ┌─────────────┐
  Frontend ──▶│ API Gateway │  :8080  (Spring Cloud Gateway, CORS, routing)
              └──────┬──────┘
        ┌────────────┼─────────────┬──────────────┐
        ▼            ▼             ▼              ▼
  Auth Service  Property Svc   Report Svc    Schedule Svc
     :8081         :8082         :8083          :8084
        └────────────┴──────┬──────┴──────────────┘
                            ▼
                     PostgreSQL (shared)
```

- **API Gateway** (`:8080`) — single entry point; routes `/api/auth/**`, `/api/properties/**`, `/api/reports/**`, `/api/schedules/**` to the services and handles CORS.
- **Auth Service** (`:8081`) — registration, login, refresh, current user. BCrypt password hashing, JWT issuance/validation.
- **Property Service** (`:8082`) — property CRUD plus the Maricopa County Assessor API client.
- **Report Service** (`:8083`) — report generation and retrieval. Calls the property service to refresh data.
- **Schedule Service** (`:8084`) — schedule CRUD, a `@Scheduled` runner (every 5 min) that generates and emails due reports via the report/auth services.

Services share one PostgreSQL database. Each service validates the JWT independently using a shared secret; inter-service calls use internal (`/internal/**`) endpoints not exposed through the gateway.

## Features

- **User Authentication** — Register and log in with JWT-based auth (access + refresh tokens)
- **Property Tracking** — Search and add Maricopa County properties by APN or address
- **Real-time Data** — Pulls ownership, valuation, and tax data from the Maricopa County Assessor API
- **Report Generation** — Generate on-demand reports with full property details
- **Periodic Email Reports** — Schedule daily, weekly, monthly, or quarterly reports delivered by email
- **Report History** — Browse and view all past reports

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2, Spring Cloud Gateway, Spring Data JPA, Maven (multi-module) |
| Database | PostgreSQL with JSONB (Hibernate) |
| Auth | JWT (jjwt) + BCrypt (spring-security-crypto) |
| Email | Spring Mail (JavaMailSender) |
| Scheduling | Spring `@Scheduled` |
| Frontend | React 18, TypeScript, Vite, Tailwind CSS |
| External API | [Maricopa County Assessor API](https://mcassessor.maricopa.gov) |

## Prerequisites

- Java 17+
- Maven 3.6.3+
- Node.js 18+
- PostgreSQL 14+

## Quick Start

### 1. Database Setup

```bash
createdb maricopa_reports
```

### 2. Backend (microservices)

Build all modules, then start each service (each reads `DB_*`, `JWT_SECRET`, etc. from the environment; sensible localhost defaults are baked in):

```bash
cd services
mvn -DskipTests install

# Start each in its own terminal (or background):
java -jar auth-service/target/auth-service-1.0.0.jar       # :8081
java -jar property-service/target/property-service-1.0.0.jar  # :8082
java -jar report-service/target/report-service-1.0.0.jar   # :8083
java -jar schedule-service/target/schedule-service-1.0.0.jar # :8084
java -jar gateway/target/gateway-1.0.0.jar                 # :8080
```

The gateway will be available at `http://localhost:8080` and is the only port the frontend talks to. Tables are created automatically by Hibernate (`ddl-auto=update`).

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at `http://localhost:5173`.

### 4. Docker (Alternative)

```bash
docker compose up --build
```

This builds and starts Postgres, all five Spring Boot services, and the frontend (served by Nginx on `:3000`, proxying `/api` to the gateway).

## Configuration

### Maricopa County Assessor API

To use the Maricopa County Assessor API, you need an API token. Request one at [mcassessor.maricopa.gov](https://mcassessor.maricopa.gov) by selecting "API Question/Token" in the contact form.

Set `MARICOPA_API_TOKEN` in your `.env` file.

### Email (SMTP)

Configure SMTP settings in `.env` to enable email delivery:

```
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASSWORD=your-app-password
```

## API Endpoints

### Auth
- `POST /api/auth/register` — Create account
- `POST /api/auth/login` — Login
- `POST /api/auth/refresh` — Refresh token
- `GET /api/auth/me` — Current user

### Properties
- `GET /api/properties` — List tracked properties
- `POST /api/properties` — Add property (by APN)
- `GET /api/properties/{id}` — Property details
- `DELETE /api/properties/{id}` — Remove property
- `POST /api/properties/{id}/sync` — Refresh from Maricopa API
- `GET /api/properties/search?q=...` — Search Maricopa County

### Reports
- `GET /api/reports` — List reports
- `GET /api/reports/{id}` — Report details
- `POST /api/reports/generate/{property_id}` — Generate report

> Endpoint paths and request/response shapes are unchanged from the original FastAPI backend, so the frontend integrates without contract changes.

### Schedules
- `GET /api/schedules` — List schedules
- `POST /api/schedules` — Create schedule
- `PUT /api/schedules/{id}` — Update schedule
- `DELETE /api/schedules/{id}` — Delete schedule

## Database Schema

The system uses 5 tables in PostgreSQL:

- **users** — User accounts with hashed passwords
- **properties** — Tracked properties with APN and cached Maricopa API data (JSONB)
- **reports** — Generated reports with full data snapshots (JSONB)
- **report_schedules** — Periodic report configuration (frequency, next_run_at)
- **valuation_history** — Historical valuation records per property

## Project Structure

```
maricopa-property-reports/
├── services/                    # Java Spring Boot microservices (Maven multi-module)
│   ├── pom.xml                  # Parent POM (dependency management)
│   ├── common/                  # Shared JWT, security filter, error handling
│   ├── auth-service/            # :8081
│   ├── property-service/        # :8082 (+ Maricopa API client)
│   ├── report-service/          # :8083
│   ├── schedule-service/        # :8084 (+ @Scheduled runner, email)
│   ├── gateway/                 # :8080 (Spring Cloud Gateway)
│   └── Dockerfile               # Shared multi-stage build (MODULE arg)
├── backend/                     # Legacy FastAPI monolith (kept for reference)
├── frontend/
│   ├── src/
│   │   ├── api/                 # API client functions
│   │   ├── components/          # Shared components
│   │   ├── context/             # React contexts (Auth)
│   │   ├── pages/               # Page components
│   │   └── types/               # TypeScript types
│   ├── package.json
│   └── Dockerfile
├── docker-compose.yml
└── README.md
```
