package gov.maricopa.reports.report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "reports")
public class ReportEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType = "full";

    @CreationTimestamp
    @Column(name = "generated_at", nullable = false, updatable = false)
    private OffsetDateTime generatedAt;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "email_sent_to", length = 255)
    private String emailSentTo;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "pending";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_data")
    private Map<String, Object> reportData;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public UUID getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(UUID propertyId) {
        this.propertyId = propertyId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(OffsetDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getEmailSentTo() {
        return emailSentTo;
    }

    public void setEmailSentTo(String emailSentTo) {
        this.emailSentTo = emailSentTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getReportData() {
        return reportData;
    }

    public void setReportData(Map<String, Object> reportData) {
        this.reportData = reportData;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
