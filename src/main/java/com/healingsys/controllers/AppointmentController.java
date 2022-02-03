package com.healingsys.controllers;

import com.healingsys.exception.ApiIllegalArgumentException;
import com.healingsys.exception.ApiNoSuchElementException;
import com.healingsys.services.AppointmentManagerService;
import com.healingsys.services.AppointmentService;
import com.healingsys.util.Day;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointment")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final AppointmentManagerService appointmentManagerService;

    @GetMapping("/appointments/{departmentId}")
    public List<Day> getDepartmentDays(@PathVariable Long departmentId)
            throws ApiNoSuchElementException, ApiIllegalArgumentException {

        return appointmentManagerService.appointmentHandler(departmentId);
    }
}
