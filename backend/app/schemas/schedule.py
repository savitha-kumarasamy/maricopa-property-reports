import uuid
from datetime import datetime

from pydantic import BaseModel


class ScheduleCreate(BaseModel):
    property_id: uuid.UUID
    frequency: str  # daily, weekly, monthly, quarterly


class ScheduleUpdate(BaseModel):
    frequency: str | None = None
    is_active: bool | None = None


class ScheduleResponse(BaseModel):
    id: uuid.UUID
    property_id: uuid.UUID
    frequency: str
    next_run_at: datetime
    is_active: bool
    created_at: datetime
    updated_at: datetime

    model_config = {"from_attributes": True}
