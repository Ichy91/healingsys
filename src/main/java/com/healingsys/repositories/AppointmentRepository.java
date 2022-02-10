package com.healingsys.repositories;

import com.healingsys.entities.Appointment;
import com.healingsys.entities.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByDepartmentIdAndDateAndHourAndStatus(Long departmentId, LocalDate day, LocalTime time, AppointmentStatus status);

    List<Appointment> findAllByDepartmentIdAndUserIdAndStatus(Long departmentId, UUID userId, AppointmentStatus status);

    List<Appointment> findAllByDepartmentIdAndUserIdAndDateAndStatus(Long departmentId, UUID userId, LocalDate day, AppointmentStatus status);
}
