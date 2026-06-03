package gov.maricopa.reports.schedule.service;

import gov.maricopa.reports.common.error.BadRequestException;
import gov.maricopa.reports.common.error.NotFoundException;
import gov.maricopa.reports.schedule.client.ServiceClients;
import gov.maricopa.reports.schedule.entity.ReportScheduleEntity;
import gov.maricopa.reports.schedule.repository.ReportScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class ScheduleService {

    private final ReportScheduleRepository repository;
    private final ServiceClients clients;

    public ScheduleService(ReportScheduleRepository repository, ServiceClients clients) {
        this.repository = repository;
        this.clients = clients;
    }

    @Transactional(readOnly = true)
    public List<ReportScheduleEntity> list(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public ReportScheduleEntity create(UUID userId, UUID propertyId, String frequency) {
        if (!Frequencies.isValid(frequency)) {
            throw new BadRequestException(
                    "Invalid frequency. Must be one of: " + String.join(", ", Frequencies.DELTAS.keySet()));
        }

        clients.verifyProperty(propertyId, userId);

        repository.findByPropertyIdAndUserIdAndActiveTrue(propertyId, userId).ifPresent(s -> {
            throw new BadRequestException("Active schedule already exists for this property");
        });

        ReportScheduleEntity schedule = new ReportScheduleEntity();
        schedule.setPropertyId(propertyId);
        schedule.setUserId(userId);
        schedule.setFrequency(frequency);
        schedule.setActive(true);
        schedule.setNextRunAt(OffsetDateTime.now(ZoneOffset.UTC).plus(Frequencies.delta(frequency)));
        return repository.save(schedule);
    }

    @Transactional
    public ReportScheduleEntity update(UUID userId, UUID scheduleId, String frequency, Boolean active) {
        ReportScheduleEntity schedule = repository.findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new NotFoundException("Schedule not found"));

        if (frequency != null) {
            if (!Frequencies.isValid(frequency)) {
                throw new BadRequestException(
                        "Invalid frequency. Must be one of: " + String.join(", ", Frequencies.DELTAS.keySet()));
            }
            schedule.setFrequency(frequency);
            schedule.setNextRunAt(OffsetDateTime.now(ZoneOffset.UTC).plus(Frequencies.delta(frequency)));
        }

        if (active != null) {
            schedule.setActive(active);
        }

        return repository.save(schedule);
    }

    @Transactional
    public void delete(UUID userId, UUID scheduleId) {
        ReportScheduleEntity schedule = repository.findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new NotFoundException("Schedule not found"));
        repository.delete(schedule);
    }
}
