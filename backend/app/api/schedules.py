import uuid
from datetime import datetime, timedelta, timezone

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.deps import get_current_user
from app.database import get_db
from app.models.property import Property
from app.models.report_schedule import ReportSchedule
from app.models.user import User
from app.schemas.schedule import ScheduleCreate, ScheduleResponse, ScheduleUpdate

router = APIRouter(prefix="/api/schedules", tags=["schedules"])

FREQUENCY_DELTAS = {
    "daily": timedelta(days=1),
    "weekly": timedelta(weeks=1),
    "monthly": timedelta(days=30),
    "quarterly": timedelta(days=90),
}

VALID_FREQUENCIES = set(FREQUENCY_DELTAS.keys())


@router.get("", response_model=list[ScheduleResponse])
async def list_schedules(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> list[ReportSchedule]:
    result = await db.execute(
        select(ReportSchedule)
        .where(ReportSchedule.user_id == current_user.id)
        .order_by(ReportSchedule.created_at.desc())
    )
    return list(result.scalars().all())


@router.post("", response_model=ScheduleResponse, status_code=status.HTTP_201_CREATED)
async def create_schedule(
    data: ScheduleCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> ReportSchedule:
    if data.frequency not in VALID_FREQUENCIES:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid frequency. Must be one of: {', '.join(VALID_FREQUENCIES)}",
        )

    prop_result = await db.execute(
        select(Property).where(
            Property.id == data.property_id,
            Property.user_id == current_user.id,
        )
    )
    if not prop_result.scalar_one_or_none():
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Property not found")

    existing = await db.execute(
        select(ReportSchedule).where(
            ReportSchedule.property_id == data.property_id,
            ReportSchedule.user_id == current_user.id,
            ReportSchedule.is_active.is_(True),
        )
    )
    if existing.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Active schedule already exists for this property",
        )

    delta = FREQUENCY_DELTAS[data.frequency]
    schedule = ReportSchedule(
        property_id=data.property_id,
        user_id=current_user.id,
        frequency=data.frequency,
        next_run_at=datetime.now(timezone.utc) + delta,
    )
    db.add(schedule)
    await db.flush()
    await db.refresh(schedule)
    return schedule


@router.put("/{schedule_id}", response_model=ScheduleResponse)
async def update_schedule(
    schedule_id: uuid.UUID,
    data: ScheduleUpdate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> ReportSchedule:
    result = await db.execute(
        select(ReportSchedule).where(
            ReportSchedule.id == schedule_id,
            ReportSchedule.user_id == current_user.id,
        )
    )
    schedule = result.scalar_one_or_none()
    if not schedule:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Schedule not found")

    if data.frequency is not None:
        if data.frequency not in VALID_FREQUENCIES:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Invalid frequency. Must be one of: {', '.join(VALID_FREQUENCIES)}",
            )
        schedule.frequency = data.frequency
        delta = FREQUENCY_DELTAS[data.frequency]
        schedule.next_run_at = datetime.now(timezone.utc) + delta

    if data.is_active is not None:
        schedule.is_active = data.is_active

    await db.flush()
    await db.refresh(schedule)
    return schedule


@router.delete("/{schedule_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_schedule(
    schedule_id: uuid.UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> None:
    result = await db.execute(
        select(ReportSchedule).where(
            ReportSchedule.id == schedule_id,
            ReportSchedule.user_id == current_user.id,
        )
    )
    schedule = result.scalar_one_or_none()
    if not schedule:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Schedule not found")
    await db.delete(schedule)
