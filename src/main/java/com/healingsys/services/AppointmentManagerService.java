package com.healingsys.services;

import com.healingsys.dto.DepartmentDetailsDto;
import com.healingsys.entities.Appointment;
import com.healingsys.entities.DayOfWeek;
import com.healingsys.entities.enums.AppointmentStatus;
import com.healingsys.exception.ApiIllegalArgumentException;
import com.healingsys.exception.ApiNoSuchElementException;
import com.healingsys.exception.ApiNotCompletedException;
import com.healingsys.util.Day;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Data
@RequiredArgsConstructor
public class AppointmentManagerService {
    private final AppointmentService appointmentService;
    private final ClosedTimeService closedTimeService;
    private final DepartmentDetailsService departmentDetailsService;
    private final UserService userService;

    private List<Day> days;
    private List<Appointment> reservedAppointments;
    private DepartmentDetailsDto details;
    private Long departmentId;
    private UUID userId;


    public List<Day> appointmentHandler(Long departmentId, UUID userId)
            throws ApiNoSuchElementException, ApiIllegalArgumentException, ApiNotCompletedException {
        details = departmentDetailsService.getById(departmentId);
        this.departmentId = departmentId;

        if (reservedAppointments == null) reservedAppointments = new ArrayList<>();
        else reservedAppointments.clear();

        if (userId != null) userAppointmentHandler(userId);

        if (days == null) days = new ArrayList<>();
        else days.clear();

        setupDays();

        return getDays();
    }


    private void setupDays() throws ApiIllegalArgumentException{
        LocalDate today = LocalDate.now();
        Set<DayOfWeek> closedDay = details.getClosedDay();
        int numberOfDays = details.getMaxGeneratedDays();

        if (numberOfDays < 1)
            throw new ApiIllegalArgumentException(String.format(
                    "The number of days (%s) that can be generated, is too small in the %s department!",
                    numberOfDays, details.getName()));

        while (days.size() < numberOfDays) {
            if (!closedDay.toString().contains(today.getDayOfWeek().toString())) {
                generateDay(today);
            }
            today = today.plusDays(1);
        }
    }

    private void generateDay(LocalDate today) {
        Day actualDay = new Day(appointmentService, closedTimeService, details, today, departmentId);
        actualDay.dayHandler(reservedAppointments, userId);
        days.add(actualDay);
    }


    private void userAppointmentHandler(UUID userId) throws ApiNoSuchElementException, ApiNotCompletedException {
        userService.getById(userId);
        this.userId = userId;
        reservedAppointments =
                appointmentService.getReservedAppointmentsByDepartmentIdAndUserId(departmentId, userId);

        if (!reservedAppointments.isEmpty())
            reservedAppointmentHandler(reservedAppointments);
    }

    private void reservedAppointmentHandler(List<Appointment> reservedAppointments) throws ApiNotCompletedException{
        LocalDateTime now = LocalDateTime.now();

        for (var appointment: reservedAppointments) {
            LocalDateTime appointmentLocalDate = LocalDateTime.of(appointment.getDate(), appointment.getHour());

            if (appointmentLocalDate.compareTo(now) < 0) {
                appointment.setStatus(AppointmentStatus.MISSED);
                throw new ApiNotCompletedException(String.format(
                        "You had an reserved appointment on %s %s and %s for status: %s!",
                                appointment.getDate(),
                                appointment.getHour(),
                                appointmentService.updateAppointment(appointment),
                                AppointmentStatus.MISSED));
            }
        }
    }
}
