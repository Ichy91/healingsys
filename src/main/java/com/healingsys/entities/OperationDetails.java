package com.healingsys.entities;

import com.healingsys.entities.enums.Status;
import lombok.*;

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
    private String name;

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

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<DayOfWeek> closedDay;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;
}
