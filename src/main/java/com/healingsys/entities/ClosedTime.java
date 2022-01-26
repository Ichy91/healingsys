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

    private LocalTime closeFrom;

    private LocalTime closeTo;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "operation_details_id", referencedColumnName = "id")
    private OperationDetails operationDetails;
}
