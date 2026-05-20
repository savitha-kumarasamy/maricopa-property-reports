import uuid
from datetime import datetime
from decimal import Decimal

from sqlalchemy import DateTime, ForeignKey, Integer, Numeric, func
from sqlalchemy.dialects.postgresql import JSONB, UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base


class ValuationHistory(Base):
    __tablename__ = "valuation_history"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    property_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("properties.id", ondelete="CASCADE"), nullable=False
    )
    tax_year: Mapped[int] = mapped_column(Integer, nullable=False)
    full_cash_value: Mapped[Decimal | None] = mapped_column(Numeric(15, 2))
    limited_value: Mapped[Decimal | None] = mapped_column(Numeric(15, 2))
    assessed_value: Mapped[Decimal | None] = mapped_column(Numeric(15, 2))
    raw_valuation: Mapped[dict | None] = mapped_column(JSONB)
    fetched_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )

    property: Mapped["Property"] = relationship(back_populates="valuation_history")  # noqa: F821
