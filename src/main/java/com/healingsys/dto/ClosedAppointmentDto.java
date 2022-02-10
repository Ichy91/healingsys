package com.healingsys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClosedAppointmentDto {
    private Long id;
    private LocalDate date;
    private LocalTime closedFrom;
    private LocalTime closedTo;
}
