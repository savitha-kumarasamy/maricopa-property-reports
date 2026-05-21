import uuid
from datetime import datetime
from typing import Any

from pydantic import BaseModel


class ReportResponse(BaseModel):
    id: uuid.UUID
    property_id: uuid.UUID
    report_type: str
    generated_at: datetime
    sent_at: datetime | None = None
    email_sent_to: str | None = None
    status: str
    report_data: dict[str, Any] | None = None
    pdf_url: str | None = None
    created_at: datetime

    model_config = {"from_attributes": True}


class ReportListResponse(BaseModel):
    reports: list[ReportResponse]
    total: int
