package com.healingsys.controllers;

import com.healingsys.dto.closedTime.ClosedAppointmentDto;
import com.healingsys.dto.department.SimpleDepartmentDto;
import com.healingsys.exceptions.*;
import com.healingsys.services.ClosedTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/closed-time")
public class ClosedTimeController {
    public final ClosedTimeService closedTimeService;


    @GetMapping("/{id}")
    public ClosedAppointmentDto getById(@PathVariable Long id)
            throws ApiNoSuchElementException {

        return closedTimeService.getById(id);
    }


    @GetMapping("/department")
    public List<ClosedAppointmentDto> getAllByDepartment(@RequestParam(value = "departmentId") Long departmentId)
            throws ApiNoSuchElementException {

        return closedTimeService.getClosedAppointmentByDepartment(departmentId);
    }


    @GetMapping("/department-day")
    public List<ClosedAppointmentDto> getAllByDepartmentFromDay(@RequestParam(value = "departmentId") Long departmentId,
                                                                @RequestParam(value = "day") LocalDate day)
            throws ApiNoSuchElementException {

        return closedTimeService.getClosedAppointmentsByDepartmentFromDay(departmentId, day);
    }


    @PostMapping("/save")
    public ResponseEntity<String> addClosedAppointment(@RequestBody ClosedAppointmentDto closedAppointmentDto,
                                                      @RequestBody SimpleDepartmentDto simpleDepartmentDto)
            throws ApiNoSuchElementException, ApiIllegalMethodException, ApiAlreadyExistException, ApiIllegalArgumentException {

        return new ResponseEntity<>(
                closedTimeService.saveClosedAppointment(closedAppointmentDto, simpleDepartmentDto),
                HttpStatus.CREATED);
    }


    @PutMapping("/update")
    public ResponseEntity<String> updateClosedAppointment(@RequestBody ClosedAppointmentDto closedAppointmentDto)
            throws ApiNoSuchElementException, ApiNoContentException, ApiIllegalMethodException {

        return new ResponseEntity<>(
                closedTimeService.updateClosedAppointment(closedAppointmentDto),
                HttpStatus.ACCEPTED);
    }


    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteClosedAppointment(@RequestBody ClosedAppointmentDto closedAppointmentDto)
            throws ApiNoSuchElementException, ApiIllegalMethodException {

        return new ResponseEntity<>(
                closedTimeService.deleteClosedAppointment(closedAppointmentDto),
                HttpStatus.ACCEPTED);
    }
}
