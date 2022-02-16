package com.healingsys.dto.appointment;

import com.healingsys.entities.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveAppointmentDto {
    private LocalDate date;
    private LocalTime hour;
    private AppointmentStatus status;
}
