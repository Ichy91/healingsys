package com.healingsys.controllers;

import com.healingsys.dto.appointment.SimpleAppointmentDto;
import com.healingsys.dto.appointment.AppointmentDto;
import com.healingsys.exceptions.*;
import com.healingsys.services.AppointmentManagerService;
import com.healingsys.services.AppointmentService;
import com.healingsys.util.Day;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointment")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final AppointmentManagerService appointmentManagerService;

    @GetMapping("/appointments")
    public List<Day> getDepartmentDays(@RequestParam(value = "departmentId") Long departmentId,
                                       @RequestParam(value = "userId", required = false) UUID userId)
            throws ApiNoSuchElementException, ApiIllegalArgumentException, ApiNotCompletedException {

        return appointmentManagerService.appointmentHandler(departmentId, userId);
    }


    @GetMapping("/userAppointment")
    public AppointmentDto getUserAppointment(@RequestParam(value = "departmentId") Long departmentId,
                                             @RequestParam(value = "userId") UUID userId,
                                             @RequestBody SimpleAppointmentDto simpleAppointmentDto) {

        return appointmentService.getUserAppointment(departmentId, userId, simpleAppointmentDto);
    }


    @PostMapping("/reserving")
    public ResponseEntity<String> appointmentReservation(@RequestParam(value = "departmentId") Long departmentId,
                                                  @RequestParam(value = "userId") UUID userId,
                                                  @RequestBody AppointmentDto appointmentDto)
            throws ApiNoSuchElementException, ApiAlreadyExistException, ApiNoContentException, ApiIllegalArgumentException, ApiIllegalMethodException {

        return new ResponseEntity<>(
                appointmentService.appointmentReservation(departmentId, userId, appointmentDto),
                HttpStatus.CREATED);
    }


    @PutMapping("/canceling")
    public ResponseEntity<String> appointmentCanceling(@RequestBody AppointmentDto appointmentDto) {
        return null;
    }


    @PutMapping("/update")
    public ResponseEntity<String> updateAppointment(@RequestBody AppointmentDto appointmentDto) {
        return null;
    }
}
