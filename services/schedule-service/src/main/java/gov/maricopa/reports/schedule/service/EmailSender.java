package gov.maricopa.reports.schedule.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    private final JavaMailSender mailSender;
    private final String smtpUser;
    private final String smtpPassword;
    private final String from;
    private final String fromName;

    public EmailSender(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String smtpUser,
            @Value("${spring.mail.password:}") String smtpPassword,
            @Value("${app.email.from}") String from,
            @Value("${app.email.from-name}") String fromName) {
        this.mailSender = mailSender;
        this.smtpUser = smtpUser;
        this.smtpPassword = smtpPassword;
        this.from = from;
        this.fromName = fromName;
    }

    /** Sends a report email. Returns false (and skips) when SMTP is not configured. */
    public boolean sendReportEmail(String toEmail, String subject, Map<String, Object> reportData) {
        if (smtpUser == null || smtpUser.isBlank() || smtpPassword == null || smtpPassword.isBlank()) {
            log.warn("SMTP not configured — skipping email send to {}", toEmail);
            return false;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(renderHtml(reportData), true);
            mailSender.send(message);
            log.info("Report email sent to {}", toEmail);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", toEmail, ex.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private String renderHtml(Map<String, Object> reportData) {
        Object summaryObj = reportData != null ? reportData.get("property_summary") : null;
        String apn = "Unknown";
        if (summaryObj instanceof Map<?, ?> summary && summary.get("apn") != null) {
            apn = summary.get("apn").toString();
        }
        return """
                <html><body style="font-family: Arial, sans-serif;">
                <h2>Maricopa Property Report</h2>
                <p>Your scheduled property report has been generated.</p>
                <p><strong>APN:</strong> %s</p>
                <p>Log in to your dashboard to view the full report details.</p>
                </body></html>
                """.formatted(apn);
    }
}
