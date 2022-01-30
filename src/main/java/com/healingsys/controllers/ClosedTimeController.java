package com.healingsys.controllers;

import com.healingsys.dto.ClosedAppointmentDto;
import com.healingsys.dto.SimpleDepartmentDetailsDto;
import com.healingsys.services.ClosedTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/closed-time")
public class ClosedTimeController {
    public final ClosedTimeService closedTimeService;

    @GetMapping("/{id}")
    public ClosedAppointmentDto getById(@PathVariable Long id) throws NoSuchElementException {
        return closedTimeService.getById(id);
    }

    @GetMapping("/department")
    public List<ClosedAppointmentDto> getAllByDepartment(@RequestParam(value = "departmentId") Long departmentId) throws NoSuchElementException {
        return closedTimeService.getClosedAppointmentByDepartment(departmentId);
    }

    @GetMapping("/department-day")
    public List<ClosedAppointmentDto> getAllByDepartmentFromDay(@RequestParam(value = "departmentId") Long departmentId,
                                                                @RequestParam(value = "day") LocalDate day) throws NoSuchElementException {
        return closedTimeService.getClosedAppointmentsByDepartmentFromDay(departmentId, day);
    }
    @PostMapping("/save")
    public String addClosedAppointment(@RequestBody ClosedAppointmentDto closedAppointmentDto,
                                       @RequestBody SimpleDepartmentDetailsDto simpleDepartmentDetailsDto) throws IllegalAccessException, IllegalArgumentException {
        return closedTimeService.saveClosedAppointment(closedAppointmentDto, simpleDepartmentDetailsDto);
    }

    @PutMapping("/update")
    public String updateClosedAppointment(@RequestBody ClosedAppointmentDto closedAppointmentDto) throws IllegalAccessException, NoSuchElementException {
        return closedTimeService.updateClosedAppointment(closedAppointmentDto);
    }

    @DeleteMapping("/delete")
    public String deleteClosedAppointment(@RequestBody ClosedAppointmentDto closedAppointmentDto) throws IllegalAccessException, NoSuchElementException {
        return closedTimeService.deleteClosedAppointment(closedAppointmentDto);
    }
}