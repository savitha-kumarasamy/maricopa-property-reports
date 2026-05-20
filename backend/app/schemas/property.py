import uuid
from datetime import datetime
from typing import Any

from pydantic import BaseModel


class PropertyCreate(BaseModel):
    apn: str


class PropertyResponse(BaseModel):
    id: uuid.UUID
    apn: str
    property_address: str | None = None
    owner_name: str | None = None
    parcel_type: str | None = None
    last_synced_at: datetime | None = None
    raw_property_data: dict[str, Any] | None = None
    created_at: datetime

    model_config = {"from_attributes": True}


class PropertySearchResult(BaseModel):
    apn: str
    address: str | None = None
    owner_name: str | None = None
    parcel_type: str | None = None


class PropertySearchResponse(BaseModel):
    results: list[PropertySearchResult]
    total: int
    page: int
