package com.healingsys.services;

import com.healingsys.entities.Appointment;
import com.healingsys.entities.enums.AppointmentStatus;
import com.healingsys.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;


    public List<Appointment> getReservedAppointmentsByDayAndHour(LocalDate day, LocalTime time) {
        return appointmentRepository.findAllByDateAndHourAndStatus(day, time, AppointmentStatus.RESERVED);
    }
}
