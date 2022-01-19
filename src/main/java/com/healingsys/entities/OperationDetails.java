package com.healingsys.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalTime open;

    @NotNull
    private LocalTime closed;

    @NotNull
    private int maxGeneratedDays;

    @NotNull
    private double slotLengthInHour;

    @NotNull
    private int slotMaxCapacity;

    @OneToMany
    private Set<DayOfWeek> closedDay;
}
