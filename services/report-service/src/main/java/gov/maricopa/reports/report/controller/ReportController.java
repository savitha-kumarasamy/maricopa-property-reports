package gov.maricopa.reports.report.controller;

import gov.maricopa.reports.common.security.CurrentUserId;
import gov.maricopa.reports.report.dto.ReportListResponse;
import gov.maricopa.reports.report.dto.ReportResponse;
import gov.maricopa.reports.report.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ReportListResponse list(@CurrentUserId UUID userId) {
        List<ReportResponse> reports = reportService.list(userId).stream()
                .map(ReportResponse::from)
                .toList();
        return new ReportListResponse(reports, reportService.count(userId));
    }

    @GetMapping("/{id}")
    public ReportResponse get(@CurrentUserId UUID userId, @PathVariable UUID id) {
        return ReportResponse.from(reportService.get(userId, id));
    }

    @PostMapping("/generate/{propertyId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse generate(@CurrentUserId UUID userId, @PathVariable UUID propertyId) {
        return ReportResponse.from(reportService.generate(userId, propertyId));
    }
}
