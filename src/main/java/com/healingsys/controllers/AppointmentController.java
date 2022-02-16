package com.healingsys.controllers;

import com.healingsys.dto.appointment.SaveAppointmentDto;
import com.healingsys.exception.*;
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

    @PostMapping("/reserving")
    public ResponseEntity<String> appointmentReservation(@RequestParam(value = "departmentId") Long departmentId,
                                                  @RequestParam(value = "userId") UUID userId,
                                                  @RequestBody SaveAppointmentDto saveAppointmentDto)
            throws ApiNoSuchElementException, ApiAlreadyExistException, ApiNoContentException, ApiIllegalArgumentException, ApiIllegalMethodException {

        return new ResponseEntity<>(
                appointmentService.appointmentReservation(departmentId, userId, saveAppointmentDto),
                HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateAppointment(@RequestParam(value = "departmentId") Long departmentId,
                                                    @RequestParam(value = "userId") UUID userId,
                                                    @RequestBody SaveAppointmentDto saveAppointmentDto) {
        return null;
    }
}
