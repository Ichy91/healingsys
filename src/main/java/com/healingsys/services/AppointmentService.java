package com.healingsys.services;

import com.healingsys.entities.Appointment;
import com.healingsys.entities.enums.AppointmentStatus;
import com.healingsys.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;


    public List<Appointment> getAppointmentsByDepartmentFromDay(Long departmentId, LocalDate day) {
        return appointmentRepository.findAllByDepartmentIdAndDateGreaterThanEqualOrderByDate(departmentId, day);
    }

    public List<Appointment> getReservedAppointmentsByDepartmentIdAndUserId(Long departmentId, UUID userId) {
        return appointmentRepository.findAllByDepartmentIdAndUserIdAndStatus(departmentId, userId, AppointmentStatus.RESERVED);
    }

    public String updateAppointment(Appointment appointment) {
        appointmentRepository.save(appointment);

        return "The appointment updated!";
    }


}
