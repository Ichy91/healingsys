package com.healingsys.controllers;

import com.healingsys.services.AppointmentManagerService;
import com.healingsys.services.AppointmentService;
import com.healingsys.util.Day;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointment")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final AppointmentManagerService appointmentManagerService;

    @GetMapping("/appointments")
    public List<Day> getDays() {
        return appointmentManagerService.appointmentHandler(LocalDateTime.now());
    }
}
