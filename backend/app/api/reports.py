import uuid

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.deps import get_current_user
from app.database import get_db
from app.models.report import Report
from app.models.user import User
from app.schemas.report import ReportListResponse, ReportResponse
from app.services.report_generator import generate_report

router = APIRouter(prefix="/api/reports", tags=["reports"])


@router.get("", response_model=ReportListResponse)
async def list_reports(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> dict:
    result = await db.execute(
        select(Report)
        .where(Report.user_id == current_user.id)
        .order_by(Report.generated_at.desc())
    )
    reports = list(result.scalars().all())
    count_result = await db.execute(
        select(func.count()).select_from(Report).where(Report.user_id == current_user.id)
    )
    total = count_result.scalar() or 0
    return {"reports": reports, "total": total}


@router.get("/{report_id}", response_model=ReportResponse)
async def get_report(
    report_id: uuid.UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> Report:
    result = await db.execute(
        select(Report).where(
            Report.id == report_id,
            Report.user_id == current_user.id,
        )
    )
    report = result.scalar_one_or_none()
    if not report:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Report not found")
    return report


@router.post("/generate/{property_id}", response_model=ReportResponse, status_code=status.HTTP_201_CREATED)
async def generate_report_endpoint(
    property_id: uuid.UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> Report:
    try:
        return await generate_report(db, property_id, current_user.id)
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(exc))
