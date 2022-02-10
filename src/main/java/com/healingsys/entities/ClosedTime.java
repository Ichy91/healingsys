package com.healingsys.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClosedTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDate date;

    private LocalTime closedFrom;

    private LocalTime closedTo;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "department_details_id", referencedColumnName = "id")
    private DepartmentDetails departmentDetails;
}
