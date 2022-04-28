package com.healingsys.dto.department;

import com.healingsys.entities.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleDepartmentDto {
    private Long id;
    private String name;
    private LocalTime opening;
    private LocalTime closing;
    private Set<DayOfWeek> closedDay;
}
