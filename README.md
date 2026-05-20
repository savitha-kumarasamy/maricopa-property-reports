# Maricopa County Property Reports

A full-stack web application for tracking Maricopa County property ownership details, tax assessments, and valuation history. Users can set up periodic email reports delivered on a configurable schedule.

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
| Backend | Python 3.12, FastAPI, SQLAlchemy 2.0, Alembic |
| Database | PostgreSQL with JSONB |
| Auth | JWT (python-jose + passlib/bcrypt) |
| Email | aiosmtplib with Jinja2 templates |
| Scheduling | APScheduler |
| Frontend | React 18, TypeScript, Vite, Tailwind CSS |
| External API | [Maricopa County Assessor API](https://mcassessor.maricopa.gov) |

## Prerequisites

- Python 3.11+
- Node.js 18+
- PostgreSQL 14+

## Quick Start

### 1. Database Setup

```bash
createdb maricopa_reports
```

### 2. Backend

```bash
cd backend
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
cp .env.example .env  # Edit with your settings
alembic upgrade head
uvicorn app.main:app --reload
```

The API will be available at `http://localhost:8000` with interactive docs at `/docs`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at `http://localhost:5173`.

### 4. Docker (Alternative)

```bash
cp backend/.env.example .env
docker compose up --build
```

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
├── backend/
│   ├── app/
│   │   ├── main.py              # FastAPI entry point
│   │   ├── config.py            # Environment settings
│   │   ├── database.py          # SQLAlchemy async engine
│   │   ├── models/              # ORM models
│   │   ├── schemas/             # Pydantic schemas
│   │   ├── api/                 # Route handlers
│   │   ├── services/            # Business logic
│   │   └── templates/           # Email templates
│   ├── alembic/                 # Database migrations
│   ├── requirements.txt
│   └── Dockerfile
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
