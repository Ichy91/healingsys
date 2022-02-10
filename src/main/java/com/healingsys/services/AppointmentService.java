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


    public List<Appointment> getReservedAppointmentsByDepartmentAndDayAndHour(Long departmentId, LocalDate day, LocalTime time) {
        return appointmentRepository.findAllByDepartmentIdAndDateAndHourAndStatus(departmentId, day, time, AppointmentStatus.RESERVED);
    }

    public List<Appointment> getReservedAppointmentsByDepartmentIdAndUserId(Long departmentId, UUID userId) {
        return appointmentRepository.findAllByDepartmentIdAndUserIdAndStatus(departmentId, userId, AppointmentStatus.RESERVED);
    }

    public List<Appointment> getReservedAppointmentsByDepartmentAndUserAndDay(Long departmentId, UUID userId, LocalDate day) {
        return appointmentRepository.findAllByDepartmentIdAndUserIdAndDateAndStatus(departmentId, userId, day, AppointmentStatus.RESERVED);
    }

    public List<Appointment> getCompletedAppointmentsByDepartmentAndUserAndDay(Long departmentId, UUID userId, LocalDate day) {
        return appointmentRepository.findAllByDepartmentIdAndUserIdAndDateAndStatus(departmentId, userId, day, AppointmentStatus.COMPLETED);
    }

    public List<Appointment> getMissedAppointmentsByDepartmentAndUserAndDay(Long departmentId, UUID userId, LocalDate day) {
        return appointmentRepository.findAllByDepartmentIdAndUserIdAndDateAndStatus(departmentId, userId, day, AppointmentStatus.MISSED);
    }

    public List<Appointment> getCanceledAppointmentsByDepartmentAndUserAndDay(Long departmentId, UUID userId, LocalDate day) {
        return appointmentRepository.findAllByDepartmentIdAndUserIdAndDateAndStatus(departmentId, userId, day, AppointmentStatus.CANCELED);
    }

    public String updateAppointment(Appointment appointment) {
        appointmentRepository.save(appointment);

        return "The appointment updated!";
    }


}
