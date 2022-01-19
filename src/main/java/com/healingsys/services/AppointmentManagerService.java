package com.healingsys.services;

import com.healingsys.util.Day;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@RequiredArgsConstructor
public class AppointmentManagerService {

    private final AppointmentService appointmentService;
    private List<Day> days;

    public void daisHandler(LocalDate toDayDate) {}

    private void setupDays(LocalDate toDayDate) {}


}
