package gov.maricopa.reports.schedule.controller;

import gov.maricopa.reports.common.security.CurrentUserId;
import gov.maricopa.reports.schedule.dto.ScheduleCreate;
import gov.maricopa.reports.schedule.dto.ScheduleResponse;
import gov.maricopa.reports.schedule.dto.ScheduleUpdate;
import gov.maricopa.reports.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public List<ScheduleResponse> list(@CurrentUserId UUID userId) {
        return scheduleService.list(userId).stream().map(ScheduleResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse create(@CurrentUserId UUID userId, @Valid @RequestBody ScheduleCreate request) {
        return ScheduleResponse.from(scheduleService.create(userId, request.propertyId(), request.frequency()));
    }

    @PutMapping("/{id}")
    public ScheduleResponse update(
            @CurrentUserId UUID userId,
            @PathVariable UUID id,
            @RequestBody ScheduleUpdate request) {
        return ScheduleResponse.from(
                scheduleService.update(userId, id, request.frequency(), request.isActive()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUserId UUID userId, @PathVariable UUID id) {
        scheduleService.delete(userId, id);
    }
}
