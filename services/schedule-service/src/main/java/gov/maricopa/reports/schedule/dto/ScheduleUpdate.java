package gov.maricopa.reports.schedule.dto;

public record ScheduleUpdate(
        String frequency,
        Boolean isActive
) {
}
