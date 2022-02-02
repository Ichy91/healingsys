package com.healingsys.controllers;

import com.healingsys.dto.ClosedAppointmentDto;
import com.healingsys.dto.SimpleDepartmentDetailsDto;
import com.healingsys.exception.ApiRequestException;
import com.healingsys.services.ClosedTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/closed-time")
public class ClosedTimeController {
    public final ClosedTimeService closedTimeService;

    @GetMapping("/{id}")
    public ClosedAppointmentDto getById(@PathVariable Long id) throws ApiRequestException {
        return closedTimeService.getById(id);
    }

    @GetMapping("/department")
    public List<ClosedAppointmentDto> getAllByDepartment(@RequestParam(value = "departmentId") Long departmentId) throws ApiRequestException {
        return closedTimeService.getClosedAppointmentByDepartment(departmentId);
    }

    @GetMapping("/department-day")
    public List<ClosedAppointmentDto> getAllByDepartmentFromDay(@RequestParam(value = "departmentId") Long departmentId,
                                                                @RequestParam(value = "day") LocalDate day) throws ApiRequestException {
        return closedTimeService.getClosedAppointmentsByDepartmentFromDay(departmentId, day);
    }
    @PostMapping("/save")
    public String addClosedAppointment(@RequestBody ClosedAppointmentDto closedAppointmentDto,
                                       @RequestBody SimpleDepartmentDetailsDto simpleDepartmentDetailsDto) throws ApiRequestException {
        return closedTimeService.saveClosedAppointment(closedAppointmentDto, simpleDepartmentDetailsDto);
    }

    @PutMapping("/update")
    public String updateClosedAppointment(@RequestBody ClosedAppointmentDto closedAppointmentDto) throws ApiRequestException {
        return closedTimeService.updateClosedAppointment(closedAppointmentDto);
    }

    @DeleteMapping("/delete")
    public String deleteClosedAppointment(@RequestBody ClosedAppointmentDto closedAppointmentDto) throws ApiRequestException {
        return closedTimeService.deleteClosedAppointment(closedAppointmentDto);
    }
}
