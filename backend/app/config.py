from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    APP_NAME: str = "Maricopa Property Reports"
    DEBUG: bool = False

    DATABASE_URL: str = "postgresql+asyncpg://postgres:postgres@localhost:5432/maricopa_reports"
    DATABASE_URL_SYNC: str = "postgresql+psycopg2://postgres:postgres@localhost:5432/maricopa_reports"

    SECRET_KEY: str = "change-me-in-production-use-a-real-secret-key"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7

    MARICOPA_API_BASE_URL: str = "https://mcassessor.maricopa.gov"
    MARICOPA_API_TOKEN: str = ""

    SMTP_HOST: str = "smtp.gmail.com"
    SMTP_PORT: int = 587
    SMTP_USER: str = ""
    SMTP_PASSWORD: str = ""
    EMAIL_FROM: str = "noreply@maricopa-reports.com"
    EMAIL_FROM_NAME: str = "Maricopa Property Reports"

    FRONTEND_URL: str = "http://localhost:5173"

    model_config = {"env_file": ".env", "extra": "ignore"}


settings = Settings()
