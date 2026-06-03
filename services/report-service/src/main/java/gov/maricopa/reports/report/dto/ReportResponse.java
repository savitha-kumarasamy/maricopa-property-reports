package gov.maricopa.reports.report.dto;

import gov.maricopa.reports.report.entity.ReportEntity;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        UUID propertyId,
        String reportType,
        OffsetDateTime generatedAt,
        OffsetDateTime sentAt,
        String emailSentTo,
        String status,
        Map<String, Object> reportData,
        String pdfUrl,
        OffsetDateTime createdAt
) {
    public static ReportResponse from(ReportEntity r) {
        return new ReportResponse(
                r.getId(),
                r.getPropertyId(),
                r.getReportType(),
                r.getGeneratedAt(),
                r.getSentAt(),
                r.getEmailSentTo(),
                r.getStatus(),
                r.getReportData(),
                r.getPdfUrl(),
                r.getCreatedAt()
        );
    }
}
