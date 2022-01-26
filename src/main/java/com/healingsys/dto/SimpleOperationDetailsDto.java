package com.healingsys.dto;

import com.healingsys.entities.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleOperationDetailsDto {
    private Long id;
    private String name;
    private LocalTime open;
    private LocalTime closed;
    private Set<DayOfWeek> closedDay;
}
