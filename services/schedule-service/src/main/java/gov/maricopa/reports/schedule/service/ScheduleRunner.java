package gov.maricopa.reports.schedule.service;

import gov.maricopa.reports.schedule.client.ServiceClients;
import gov.maricopa.reports.schedule.entity.ReportScheduleEntity;
import gov.maricopa.reports.schedule.repository.ReportScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Periodically processes due schedules: generates a report, emails it, and reschedules.
 * Mirrors the original APScheduler job that ran every 5 minutes.
 */
@Component
public class ScheduleRunner {

    private static final Logger log = LoggerFactory.getLogger(ScheduleRunner.class);

    private final ReportScheduleRepository repository;
    private final ServiceClients clients;
    private final EmailSender emailSender;

    public ScheduleRunner(ReportScheduleRepository repository, ServiceClients clients, EmailSender emailSender) {
        this.repository = repository;
        this.clients = clients;
        this.emailSender = emailSender;
    }

    @Scheduled(fixedRateString = "${app.scheduler.interval-ms}")
    @Transactional
    public void processDueSchedules() {
        List<ReportScheduleEntity> due =
                repository.findByActiveTrueAndNextRunAtLessThanEqual(OffsetDateTime.now(ZoneOffset.UTC));
        for (ReportScheduleEntity schedule : due) {
            try {
                execute(schedule);
            } catch (Exception ex) {
                log.error("Failed to process schedule {}: {}", schedule.getId(), ex.getMessage());
            }
        }
    }

    private void execute(ReportScheduleEntity schedule) {
        ServiceClients.GeneratedReport report = clients.generateReport(schedule.getPropertyId(), schedule.getUserId());
        ServiceClients.UserInfo user = clients.getUser(schedule.getUserId());

        if (user != null && report != null && report.reportData() != null) {
            Object summary = report.reportData().get("property_summary");
            String apn = "Unknown";
            if (summary instanceof java.util.Map<?, ?> m && m.get("apn") != null) {
                apn = m.get("apn").toString();
            }
            boolean sent = emailSender.sendReportEmail(
                    user.email(), "Maricopa Property Report - " + apn, report.reportData());
            if (sent) {
                clients.markReportSent(report.id(), user.email());
            }
        }

        schedule.setNextRunAt(OffsetDateTime.now(ZoneOffset.UTC).plus(Frequencies.delta(schedule.getFrequency())));
        repository.save(schedule);
        log.info("Schedule {} executed, next run at {}", schedule.getId(), schedule.getNextRunAt());
    }
}
