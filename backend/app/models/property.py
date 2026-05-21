import uuid
from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, String, func
from sqlalchemy.dialects.postgresql import JSONB, UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base


class Property(Base):
    __tablename__ = "properties"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False
    )
    apn: Mapped[str] = mapped_column(String(50), nullable=False, index=True)
    property_address: Mapped[str | None] = mapped_column(String(500))
    owner_name: Mapped[str | None] = mapped_column(String(255))
    parcel_type: Mapped[str | None] = mapped_column(String(50))
    last_synced_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    raw_property_data: Mapped[dict | None] = mapped_column(JSONB)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), onupdate=func.now()
    )

    user: Mapped["User"] = relationship(back_populates="properties")  # noqa: F821
    reports: Mapped[list["Report"]] = relationship(  # noqa: F821
        back_populates="property", cascade="all, delete-orphan"
    )
    report_schedules: Mapped[list["ReportSchedule"]] = relationship(  # noqa: F821
        back_populates="property", cascade="all, delete-orphan"
    )
    valuation_history: Mapped[list["ValuationHistory"]] = relationship(  # noqa: F821
        back_populates="property", cascade="all, delete-orphan"
    )
