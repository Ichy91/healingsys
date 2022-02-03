package com.healingsys.dto;

import com.healingsys.entities.DayOfWeek;
import com.healingsys.entities.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentDetailsDto {
    private String name;
    private LocalTime opening;
    private LocalTime closing;
    private int maxGeneratedDays;
    private double slotLengthInHour;
    private int slotMaxCapacity;
    private Set<DayOfWeek> closedDay;
    private Status status;
}
