package gov.maricopa.reports.report.service;

import gov.maricopa.reports.common.error.NotFoundException;
import gov.maricopa.reports.report.client.PropertyClient;
import gov.maricopa.reports.report.client.PropertyDto;
import gov.maricopa.reports.report.client.PropertyFullDataDto;
import gov.maricopa.reports.report.entity.ReportEntity;
import gov.maricopa.reports.report.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportService {

    private final ReportRepository repository;
    private final PropertyClient propertyClient;

    public ReportService(ReportRepository repository, PropertyClient propertyClient) {
        this.repository = repository;
        this.propertyClient = propertyClient;
    }

    @Transactional(readOnly = true)
    public List<ReportEntity> list(UUID userId) {
        return repository.findByUserIdOrderByGeneratedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long count(UUID userId) {
        return repository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public ReportEntity get(UUID userId, UUID id) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Report not found"));
    }

    @Transactional
    public ReportEntity generate(UUID userId, UUID propertyId) {
        PropertyFullDataDto full = propertyClient.refreshForReport(propertyId, userId);
        Map<String, Object> reportData = buildReportData(full.property(), full.data());

        ReportEntity report = new ReportEntity();
        report.setPropertyId(propertyId);
        report.setUserId(userId);
        report.setReportType("full");
        report.setStatus("generated");
        report.setReportData(reportData);
        return repository.save(report);
    }

    @Transactional
    public ReportEntity markSent(UUID id, String email) {
        ReportEntity report = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found"));
        report.setStatus("sent");
        report.setSentAt(OffsetDateTime.now(ZoneOffset.UTC));
        report.setEmailSentTo(email);
        return repository.save(report);
    }

    private Map<String, Object> buildReportData(PropertyDto property, Map<String, Object> apiData) {
        Map<String, Object> propertySummary = new LinkedHashMap<>();
        propertySummary.put("apn", property.apn());
        propertySummary.put("address", apiData.get("address"));
        propertySummary.put("parcel_type", property.parcelType());

        Map<String, Object> ownership = new LinkedHashMap<>();
        ownership.put("owner_details", apiData.get("owner"));
        ownership.put("last_updated", OffsetDateTime.now(ZoneOffset.UTC).toString());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("property_summary", propertySummary);
        data.put("ownership", ownership);
        data.put("valuations", apiData.get("valuations"));
        data.put("property_details", apiData.get("property_info"));
        data.put("generated_at", OffsetDateTime.now(ZoneOffset.UTC).toString());
        return data;
    }
}
