import logging
import uuid
from datetime import datetime, timezone
from typing import Any

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.property import Property
from app.models.report import Report
from app.services import maricopa_api

logger = logging.getLogger(__name__)


def _build_report_data(
    property_record: Property, api_data: dict[str, Any]
) -> dict[str, Any]:
    owner_data = api_data.get("owner", {})
    valuation_data = api_data.get("valuations", {})
    address_data = api_data.get("address", {})
    property_info = api_data.get("property_info", {})

    return {
        "property_summary": {
            "apn": property_record.apn,
            "address": address_data,
            "parcel_type": property_record.parcel_type,
        },
        "ownership": {
            "owner_details": owner_data,
            "last_updated": datetime.now(timezone.utc).isoformat(),
        },
        "valuations": valuation_data,
        "property_details": property_info,
        "generated_at": datetime.now(timezone.utc).isoformat(),
    }


async def generate_report(
    db: AsyncSession,
    property_id: uuid.UUID,
    user_id: uuid.UUID,
    report_type: str = "full",
) -> Report:
    result = await db.execute(
        select(Property).where(
            Property.id == property_id,
            Property.user_id == user_id,
        )
    )
    prop = result.scalar_one_or_none()
    if not prop:
        raise ValueError("Property not found or does not belong to user")

    api_data = await maricopa_api.fetch_full_property_data(prop.apn)

    if "error" not in api_data.get("parcel", {}):
        prop.raw_property_data = api_data
        prop.last_synced_at = datetime.now(timezone.utc)

        owner = api_data.get("owner", {})
        if isinstance(owner, dict):
            prop.owner_name = owner.get("name") or owner.get("owner_name")

        address = api_data.get("address", {})
        if isinstance(address, dict):
            parts = [
                address.get("street_address", ""),
                address.get("city", ""),
                address.get("zip", ""),
            ]
            prop.property_address = ", ".join(p for p in parts if p)

    report_data = _build_report_data(prop, api_data)

    report = Report(
        property_id=property_id,
        user_id=user_id,
        report_type=report_type,
        status="generated",
        report_data=report_data,
        generated_at=datetime.now(timezone.utc),
    )
    db.add(report)
    await db.flush()
    await db.refresh(report)

    logger.info("Report %s generated for property %s", report.id, prop.apn)
    return report
