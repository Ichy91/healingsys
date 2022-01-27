package com.healingsys.entities;

import com.healingsys.entities.enums.Status;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdDate;

    @UpdateTimestamp
    private Timestamp updatedDate;

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
