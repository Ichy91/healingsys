package com.healingsys.services;

import com.healingsys.entities.OperationDetails;
import com.healingsys.util.Day;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Data
@RequiredArgsConstructor
public class AppointmentManagerService {
    private final AppointmentService appointmentService;
    private List<Day> days;
    private OperationDetails details;


    public List<Day> appointmentHandler(LocalDateTime toDayDateTime) {
        details = appointmentService.getOperationDetails().get();

        if (days == null || days.isEmpty()) setupDays(toDayDateTime);

        return getDays();
    }

    private void setupDays(LocalDateTime toDayDateTime) {
        days = new ArrayList<>();
        LocalDate today = toDayDateTime.toLocalDate();

        while (days.size() < details.getMaxGeneratedDays()) {
            if (!details.getClosedDay().toString().contains(today.getDayOfWeek().toString())) {
                generateDay(today);
            }
            today = today.plusDays(1);
        }
    }

    private void generateDay(LocalDate today) {
        Day actualDay = new Day(today, details);
        actualDay.dayHandler();
        days.add(actualDay);
    }
}
