package com.healingsys.repositories;

import com.healingsys.entities.Appointment;
import com.healingsys.entities.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByDateAndHourAndStatus(LocalDate day, LocalTime time, AppointmentStatus status);
}
