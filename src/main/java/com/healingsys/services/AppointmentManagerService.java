package com.healingsys.services;

import com.healingsys.entities.Appointment;
import com.healingsys.entities.ClosedTime;
import com.healingsys.entities.DayOfWeek;
import com.healingsys.entities.DepartmentDetails;
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

    private UUID userId;
    private DepartmentDetails details;
    private List<Appointment> appointments;
    private List<ClosedTime> closedAppointments;

    private List<Day> days;


    public List<Day> appointmentHandler(Long departmentId, UUID userId)
            throws ApiNoSuchElementException, ApiIllegalArgumentException, ApiNotCompletedException {
        details = departmentDetailsService.getEntityById(departmentId);
        listsInitialisation();

        appointmentsHandler(userId);
        closedAppointmentsHandler();

        setupDays();

        return getDays();
    }

    //Initialisation of the used Lists
    private void listsInitialisation() {
        if (appointments == null) appointments = new ArrayList<>();
        else appointments.clear();

        if (closedAppointments == null) closedAppointments = new ArrayList<>();
        else closedAppointments.clear();

        if (days == null) days = new ArrayList<>();
        else days.clear();
    }

    //Day generating
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
        Day actualDay = new Day(appointments, closedAppointments, details, today);
        actualDay.dayHandler(userId);
        days.add(actualDay);
    }

    //Handling of appointments
    private void appointmentsHandler(UUID userId) throws ApiNoSuchElementException, ApiNotCompletedException {
        this.userId = userId;
        Long departmentId = details.getId();
        LocalDate today = LocalDate.now();

        if (userId != null) userAppointmentHandler(userId);

        appointments = appointmentService.getAppointmentsByDepartmentFromDay(departmentId, today);
    }

    private void userAppointmentHandler(UUID userId) throws ApiNoSuchElementException {
        userService.getById(userId);
        Long departmentId = details.getId();
        List<Appointment> userReservedAppointments =
                appointmentService.getReservedAppointmentsByDepartmentIdAndUserId(departmentId, userId);

        if (!userReservedAppointments.isEmpty())
            userReservedAppointmentHandler(userReservedAppointments);
    }

    private void userReservedAppointmentHandler(List<Appointment> reservedAppointments) throws ApiNotCompletedException{
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

    private void closedAppointmentsHandler() {
        LocalDate today = LocalDate.now();
        Long departmentId = details.getId();

        closedAppointments = closedTimeService.getClosedEntitiesByDepartmentFromDay(departmentId, today);
    }

}
