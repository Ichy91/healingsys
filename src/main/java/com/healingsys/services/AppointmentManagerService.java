package com.healingsys.services;

import com.healingsys.entities.enums.NameOfDay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentManagerService {

    private LocalTime open;
    private LocalTime closed;
    private int maxGeneratedDays;
    private double slotLengthInHour;
    private int slotMaxCapacity;
    private Set<NameOfDay> closedDay;
    private List<com.healingsys.util.Day> days;

    public void daisHandler(LocalDate toDayDate) {}

    private void setupDays(LocalDate toDayDate) {}


}
