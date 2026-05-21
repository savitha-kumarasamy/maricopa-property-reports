import uuid
from datetime import datetime, timezone

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.deps import get_current_user
from app.database import get_db
from app.models.property import Property
from app.models.user import User
from app.schemas.property import PropertyCreate, PropertyResponse, PropertySearchResponse, PropertySearchResult
from app.services import maricopa_api

router = APIRouter(prefix="/api/properties", tags=["properties"])


@router.get("/search", response_model=PropertySearchResponse)
async def search_properties(
    q: str = Query(..., min_length=1),
    page: int = Query(1, ge=1),
    _current_user: User = Depends(get_current_user),
) -> dict:
    data = await maricopa_api.search_property(q, page)

    results = []
    raw_results = data.get("results", data.get("RealProperty", []))
    if isinstance(raw_results, list):
        for item in raw_results:
            results.append(
                PropertySearchResult(
                    apn=item.get("APN", item.get("apn", "")),
                    address=item.get("Address", item.get("address")),
                    owner_name=item.get("OwnerName", item.get("owner_name")),
                    parcel_type=item.get("ParcelType", item.get("parcel_type")),
                )
            )

    return {
        "results": results,
        "total": data.get("total", len(results)),
        "page": page,
    }


@router.get("", response_model=list[PropertyResponse])
async def list_properties(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> list[Property]:
    result = await db.execute(
        select(Property)
        .where(Property.user_id == current_user.id)
        .order_by(Property.created_at.desc())
    )
    return list(result.scalars().all())


@router.post("", response_model=PropertyResponse, status_code=status.HTTP_201_CREATED)
async def add_property(
    data: PropertyCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> Property:
    existing = await db.execute(
        select(Property).where(
            Property.user_id == current_user.id,
            Property.apn == data.apn,
        )
    )
    if existing.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Property already tracked",
        )

    api_data = await maricopa_api.fetch_full_property_data(data.apn)

    address_str = None
    address = api_data.get("address", {})
    if isinstance(address, dict) and "error" not in address:
        parts = [
            address.get("street_address", ""),
            address.get("city", ""),
            address.get("zip", ""),
        ]
        address_str = ", ".join(p for p in parts if p) or None

    owner_name = None
    owner = api_data.get("owner", {})
    if isinstance(owner, dict) and "error" not in owner:
        owner_name = owner.get("name") or owner.get("owner_name")

    parcel = api_data.get("parcel", {})
    parcel_type = None
    if isinstance(parcel, dict) and "error" not in parcel:
        parcel_type = parcel.get("parcel_type") or parcel.get("ParcelType")

    prop = Property(
        user_id=current_user.id,
        apn=data.apn,
        property_address=address_str,
        owner_name=owner_name,
        parcel_type=parcel_type,
        raw_property_data=api_data,
        last_synced_at=datetime.now(timezone.utc),
    )
    db.add(prop)
    await db.flush()
    await db.refresh(prop)
    return prop


@router.get("/{property_id}", response_model=PropertyResponse)
async def get_property(
    property_id: uuid.UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> Property:
    result = await db.execute(
        select(Property).where(
            Property.id == property_id,
            Property.user_id == current_user.id,
        )
    )
    prop = result.scalar_one_or_none()
    if not prop:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Property not found")
    return prop


@router.delete("/{property_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_property(
    property_id: uuid.UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> None:
    result = await db.execute(
        select(Property).where(
            Property.id == property_id,
            Property.user_id == current_user.id,
        )
    )
    prop = result.scalar_one_or_none()
    if not prop:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Property not found")
    await db.delete(prop)


@router.post("/{property_id}/sync", response_model=PropertyResponse)
async def sync_property(
    property_id: uuid.UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> Property:
    result = await db.execute(
        select(Property).where(
            Property.id == property_id,
            Property.user_id == current_user.id,
        )
    )
    prop = result.scalar_one_or_none()
    if not prop:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Property not found")

    api_data = await maricopa_api.fetch_full_property_data(prop.apn)
    prop.raw_property_data = api_data
    prop.last_synced_at = datetime.now(timezone.utc)

    owner = api_data.get("owner", {})
    if isinstance(owner, dict) and "error" not in owner:
        prop.owner_name = owner.get("name") or owner.get("owner_name")

    address = api_data.get("address", {})
    if isinstance(address, dict) and "error" not in address:
        parts = [
            address.get("street_address", ""),
            address.get("city", ""),
            address.get("zip", ""),
        ]
        prop.property_address = ", ".join(p for p in parts if p) or prop.property_address

    await db.flush()
    await db.refresh(prop)
    return prop
