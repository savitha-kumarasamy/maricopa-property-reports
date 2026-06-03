package gov.maricopa.reports.report.dto;

import java.util.List;

public record ReportListResponse(
        List<ReportResponse> reports,
        long total
) {
}
