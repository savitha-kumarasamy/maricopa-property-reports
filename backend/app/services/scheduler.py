import asyncio
import logging
from datetime import datetime, timedelta, timezone

from apscheduler.schedulers.asyncio import AsyncIOScheduler
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import async_session_factory
from app.models.report import Report
from app.models.report_schedule import ReportSchedule
from app.models.user import User
from app.services.email_service import send_report_email
from app.services.report_generator import generate_report

logger = logging.getLogger(__name__)

scheduler = AsyncIOScheduler()

FREQUENCY_DELTAS = {
    "daily": timedelta(days=1),
    "weekly": timedelta(weeks=1),
    "monthly": timedelta(days=30),
    "quarterly": timedelta(days=90),
}


async def _process_due_schedules() -> None:
    async with async_session_factory() as db:
        now = datetime.now(timezone.utc)
        result = await db.execute(
            select(ReportSchedule).where(
                ReportSchedule.is_active.is_(True),
                ReportSchedule.next_run_at <= now,
            )
        )
        schedules = result.scalars().all()

        for schedule in schedules:
            try:
                await _execute_schedule(db, schedule)
            except Exception:
                logger.exception(
                    "Failed to process schedule %s", schedule.id
                )

        await db.commit()


async def _execute_schedule(db: AsyncSession, schedule: ReportSchedule) -> None:
    report = await generate_report(
        db, schedule.property_id, schedule.user_id
    )

    user_result = await db.execute(
        select(User).where(User.id == schedule.user_id)
    )
    user = user_result.scalar_one_or_none()

    if user and report.report_data:
        sent = await send_report_email(
            to_email=user.email,
            subject=f"Maricopa Property Report - {report.report_data.get('property_summary', {}).get('apn', 'Unknown')}",
            report_data=report.report_data,
        )
        if sent:
            report.status = "sent"
            report.sent_at = datetime.now(timezone.utc)
            report.email_sent_to = user.email

    delta = FREQUENCY_DELTAS.get(schedule.frequency, timedelta(days=30))
    schedule.next_run_at = datetime.now(timezone.utc) + delta

    logger.info("Schedule %s executed, next run at %s", schedule.id, schedule.next_run_at)


def start_scheduler() -> None:
    scheduler.add_job(
        _process_due_schedules,
        "interval",
        minutes=5,
        id="process_due_schedules",
        replace_existing=True,
    )
    scheduler.start()
    logger.info("Report scheduler started")


def stop_scheduler() -> None:
    scheduler.shutdown()
    logger.info("Report scheduler stopped")
