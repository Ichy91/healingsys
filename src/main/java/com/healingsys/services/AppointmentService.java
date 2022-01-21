package com.healingsys.services;

import com.healingsys.entities.Appointment;
import com.healingsys.entities.OperationDetails;
import com.healingsys.entities.enums.AppointmentStatus;
import com.healingsys.repositories.AppointmentRepository;
import com.healingsys.repositories.DayOfWeekRepository;
import com.healingsys.repositories.OperationDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final OperationDetailsRepository operationDetailsRepository;
    private final DayOfWeekRepository dayOfWeekRepository;


    public Optional<OperationDetails> getOperationDetails() {
        long id = 1;
        return operationDetailsRepository.findById(id);
    }

    public List<Appointment> getReservedAppointmentsByDayAndHour(LocalDate day, LocalTime time) {
        return appointmentRepository.findAllByDateAndHourAndStatus(day, time, AppointmentStatus.RESERVED);
    }
}
