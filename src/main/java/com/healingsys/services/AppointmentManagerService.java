package com.healingsys.services;

import com.healingsys.dto.DepartmentDetailsDto;
import com.healingsys.entities.DayOfWeek;
import com.healingsys.exception.ApiIllegalArgumentException;
import com.healingsys.exception.ApiNoSuchElementException;
import com.healingsys.util.Day;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Data
@RequiredArgsConstructor
public class AppointmentManagerService {
    private final AppointmentService appointmentService;
    private final ClosedTimeService closedTimeService;
    private final DepartmentDetailsService departmentDetailsService;

    private List<Day> days;
    private DepartmentDetailsDto details;
    private Long departmentId;


    public List<Day> appointmentHandler(Long departmentId)
            throws ApiNoSuchElementException, ApiIllegalArgumentException {
        LocalDateTime now = LocalDateTime.now();
        details = departmentDetailsService.getById(departmentId);
        this.departmentId = departmentId;

        if (days == null) days = new ArrayList<>();
        else days.clear();

        setupDays(now);

        return getDays();
    }


    private void setupDays(LocalDateTime toDayDateTime) throws ApiIllegalArgumentException{
        LocalDate today = toDayDateTime.toLocalDate();
        Set<DayOfWeek> closedDay = details.getClosedDay();
        int numberOfDays = details.getMaxGeneratedDays();

        if (numberOfDays < 1)
            throw new ApiIllegalArgumentException(String.format(
                    "The number of days (%s) that can be generated, is too small in the %s department.",
                    numberOfDays, details.getName()));

        while (days.size() < numberOfDays) {
            if (!closedDay.toString().contains(today.getDayOfWeek().toString())) {
                generateDay(today);
            }
            today = today.plusDays(1);
        }
    }

    private void generateDay(LocalDate today) {
        Day actualDay = new Day(appointmentService, closedTimeService, details, today);
        actualDay.dayHandler(departmentId);
        days.add(actualDay);
    }
}
