package com.healingsys.services;

import com.healingsys.dto.appointment.SaveAppointmentDto;
import com.healingsys.entities.Appointment;
import com.healingsys.entities.ClosedTime;
import com.healingsys.entities.enums.AppointmentStatus;
import com.healingsys.exception.*;
import com.healingsys.repositories.AppointmentRepository;
import com.healingsys.util.DataHandler;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    private final UserService userService;
    private final DepartmentDetailsService departmentDetailsService;
    private final ClosedTimeService closedTimeService;
    private final DataHandler dataHandler;
    private final ModelMapper mapper;

    //Getting methods
    public List<Appointment> getAppointmentsByDepartmentFromDay(Long departmentId, LocalDate day) {
        return appointmentRepository.findAllByDepartmentIdAndDateGreaterThanEqualOrderByDate(departmentId, day);
    }

    public List<Appointment> getReservedAppointmentsByDepartmentIdAndUserId(Long departmentId, UUID userId) {
        return appointmentRepository.findAllByDepartmentIdAndUserIdAndStatus(departmentId, userId, AppointmentStatus.RESERVED);
    }

    //Reservation
    public String appointmentReservation(Long departmentId, UUID userId, SaveAppointmentDto saveAppointmentDto)
            throws ApiNoSuchElementException, ApiAlreadyExistException, ApiNoContentException, ApiIllegalArgumentException, ApiIllegalMethodException {
        Appointment newAppointment = mapToEntity(saveAppointmentDto);
        newAppointment.setDepartment(departmentDetailsService.getEntityById(departmentId));
        newAppointment.setUser(userService.getById(userId));

        checkValues(newAppointment);
        checkToReservation(newAppointment);

        appointmentRepository.save(newAppointment);

        return String.format("Appointment reserved!\n" +
                "Patient: %s\n" +
                "Department: %s\n" +
                "Date: %s\n" +
                "Hour: %s,",
                newAppointment.getUser().getName(),
                newAppointment.getDepartment().getName(),
                newAppointment.getDate(),
                newAppointment.getHour());
    }

    //Checking to reservations
    private void checkToReservation(Appointment appointment)
            throws ApiIllegalMethodException {
        LocalDateTime now = LocalDateTime.now();
        Long departmentId = appointment.getDepartment().getId();
        LocalDate date = appointment.getDate();
        LocalTime hour = appointment.getHour();
        int departmentSlotCapacity = appointment.getDepartment().getSlotMaxCapacity();
        int numberOfReservations =
                appointmentRepository.findAllByDepartmentIdAndDateAndHourAndStatus(departmentId, date, hour, AppointmentStatus.RESERVED).size();

        if (now.compareTo(LocalDateTime.of(date, hour)) <= 0)
            throw new ApiIllegalMethodException(String.format("Reservation not supported before this time: %s!", now));

        if (!appointment.getStatus().equals(AppointmentStatus.RESERVED))
            throw new ApiIllegalMethodException(String.format(
                    "%s method not supported in the Reservation. Use updating!", appointment.getStatus()));

        if (departmentSlotCapacity <= numberOfReservations)
            throw new ApiIllegalMethodException(String.format(
                    "The Reservation not supported!\n" +
                            "Slot capacity: %s\n" +
                            "Num of reservations: %s", departmentSlotCapacity, numberOfReservations));

        checkClosedHours(appointment);
    }

    private void checkClosedHours(Appointment appointment) {
        Long departmentId = appointment.getDepartment().getId();
        LocalDate date = appointment.getDate();
        List<ClosedTime> closedAppointments = closedTimeService.getClosedEntitiesByDepartmentAndDay(departmentId, date);
        List<LocalTime> closedHours = new ArrayList<>();

        if (!closedAppointments.isEmpty())
            closedHours = dataHandler.setupClosedHours(appointment.getDepartment(), closedAppointments, closedHours);

        if (!closedHours.isEmpty() && closedHours.contains(appointment.getHour()))
            throw new ApiIllegalMethodException(String.format(
                    "The Reservation not supported! The %s department it's closed in this time: %s!",
                    appointment.getDepartment().getName(),
                    appointment.getHour()));
    }

    //Updating
    public String updateAppointment(Appointment appointment) {
        appointmentRepository.save(appointment);

        return "The appointment updated!";
    }

    //Appointment values checking
    private void checkValues(Appointment appointment)
            throws ApiAlreadyExistException, ApiNoSuchElementException, ApiNoContentException, ApiIllegalArgumentException {
        Long id = appointment.getId();
        Long departmentId = appointment.getDepartment().getId();
        UUID userId = appointment.getUser().getId();
        AppointmentStatus status = appointment.getStatus();
        LocalDate date = appointment.getDate();
        LocalTime hour = appointment.getHour();

        List<Appointment> appointments =
                appointmentRepository.findAllByDepartmentIdAndUserIdAndStatusAndDateAndHour(departmentId, userId, status, date, hour);

        List<LocalTime> departmentHours = generateDepartmentHours(
                        appointment.getDepartment().getOpening(),
                        appointment.getDepartment().getClosing(),
                        appointment.getDepartment().getSlotLengthInHour());

        if (date == null)
            throw new ApiNoContentException("Empty the date!");

        else if (hour == null)
            throw new ApiNoContentException("Empty the hour!");

        else if (!departmentHours.contains(hour))
            throw new ApiIllegalArgumentException(String.format(
                    "This hour: %s not compatible with the department: %s!", hour, appointment.getDepartment().getName()));

        if (id == null && !appointments.isEmpty())
            throw new ApiAlreadyExistException("The appointment already exist!");

        if (id != null && appointmentRepository.findById(id).isEmpty())
            throw new ApiNoSuchElementException(String.format("Appointment not found with id: %s!", id));

    }

    //Assistive methods
    private List<LocalTime> generateDepartmentHours(LocalTime opening, LocalTime closing, double slotLength) {
        List<LocalTime> hours = new ArrayList<>();
        int slotLengthInMinute = (int) (slotLength * 60);

        while (opening.compareTo(closing) < 0) {
            hours.add(opening);
            opening = opening.plusMinutes(slotLengthInMinute);
        }

        return hours;
    }

    private Appointment mapToEntity(SaveAppointmentDto saveAppointmentDto) {
        return mapper.map(saveAppointmentDto, Appointment.class);
    }


}
