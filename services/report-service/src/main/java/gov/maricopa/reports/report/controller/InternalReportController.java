package gov.maricopa.reports.report.controller;

import gov.maricopa.reports.report.dto.ReportResponse;
import gov.maricopa.reports.report.service.ReportService;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Service-to-service endpoints used by the schedule-service. Not exposed through the gateway.
 */
@RestController
@RequestMapping("/internal/reports")
public class InternalReportController {

    private final ReportService reportService;

    public InternalReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/generate")
    public ReportResponse generate(@RequestParam UUID propertyId, @RequestParam UUID userId) {
        return ReportResponse.from(reportService.generate(userId, propertyId));
    }

    @PatchMapping("/{id}/mark-sent")
    public ReportResponse markSent(@PathVariable UUID id, @RequestParam String email) {
        return ReportResponse.from(reportService.markSent(id, email));
    }
}
