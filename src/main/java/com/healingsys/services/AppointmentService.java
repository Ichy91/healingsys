package com.healingsys.services;

import com.healingsys.dto.appointment.AppointmentDto;
import com.healingsys.dto.appointment.SimpleAppointmentDto;
import com.healingsys.entities.Appointment;
import com.healingsys.entities.ClosedTime;
import com.healingsys.entities.enums.AppointmentStatus;
import com.healingsys.exceptions.*;
import com.healingsys.repositories.AppointmentRepository;
import com.healingsys.util.DataHandler;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public AppointmentDto getUserAppointment(Long departmentId, UUID userId, SimpleAppointmentDto simpleAppointmentDto)
            throws ApiNoSuchElementException, ApiNoContentException, ApiIllegalArgumentException, ApiIllegalMethodException {
        Appointment appointment = mapToEntity(simpleAppointmentDto);
        appointment.setUser(userService.getById(userId));
        appointment.setDepartment(departmentDetailsService.getEntityById(departmentId));

        checkValues(appointment);

        List<Appointment> appointments =
                appointmentRepository.findAllByDepartmentIdAndUserIdAndStatusAndDateAndHour(
                        departmentId,
                        userId,
                        simpleAppointmentDto.getStatus(),
                        simpleAppointmentDto.getDate(),
                        simpleAppointmentDto.getHour());

        if (appointments.isEmpty())
            throw new ApiNoSuchElementException("Appointment not found!");

        else if (appointments.size() > 1)
            throw new ApiIllegalMethodException(String.format(
                    "APPOINTMENT DUPLICATION!\n" +
                            "Department id: %s\n" +
                            "User id: %s\n" +
                            "Status: %s\n" +
                            "Date: %s\n" +
                            "Hour: %s",
                    departmentId,
                    userId,
                    simpleAppointmentDto.getStatus(),
                    simpleAppointmentDto.getDate(),
                    simpleAppointmentDto.getHour()));

        else appointment.setId(appointments.get(0).getId());

        return mapToDto(appointment);
    }

    //Reservation
    public String appointmentReservation(Long departmentId, UUID userId, AppointmentDto appointmentDto)
            throws ApiNoSuchElementException, ApiAlreadyExistException, ApiNoContentException, ApiIllegalArgumentException, ApiIllegalMethodException {
        Appointment newAppointment = mapToEntity(appointmentDto);
        newAppointment.setUser(userService.getById(userId));
        newAppointment.setDepartment(departmentDetailsService.getEntityById(departmentId));

        checkValues(newAppointment);

        if (appointmentDto.getStatus().equals(AppointmentStatus.CANCELED))
            newAppointment.setStatus(AppointmentStatus.RESERVED);

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
            throws ApiAlreadyExistException, ApiIllegalMethodException {
        LocalDateTime now = LocalDateTime.now();

        Long departmentId = appointment.getDepartment().getId();
        UUID userId = appointment.getUser().getId();
        LocalDate date = appointment.getDate();
        LocalTime hour = appointment.getHour();

        int departmentSlotCapacity = appointment.getDepartment().getSlotMaxCapacity();
        int numberOfReservations =
                appointmentRepository.findAllByDepartmentIdAndDateAndHourAndStatus(departmentId, date, hour, AppointmentStatus.RESERVED).size();

        List<Appointment> userReservedAppointments = getReservedAppointmentsByDepartmentIdAndUserId(departmentId, userId);

        if (!userReservedAppointments.isEmpty())
            throw new ApiAlreadyExistException(String.format(
                    "YOU HAVE RESERVATION!\n" +
                            "Department: %s,\n" +
                            "Date: %s, \n" +
                            "Hour: %s",
                    userReservedAppointments.get(0).getDepartment().getName(),
                    userReservedAppointments.get(0).getDate(),
                    userReservedAppointments.get(0).getHour()));

        if (now.compareTo(LocalDateTime.of(date, hour)) <= 0)
            throw new ApiIllegalMethodException(String.format("Reservation not supported before this time: %s!", now));

        if (!appointment.getStatus().equals(AppointmentStatus.RESERVED))
            throw new ApiIllegalMethodException(String.format(
                    "%s method not supported in the Reservation!", appointment.getStatus()));

        if (departmentSlotCapacity <= numberOfReservations)
            throw new ApiIllegalMethodException(String.format(
                    "The Reservation not supported!\n" +
                            "Slot capacity: %s\n" +
                            "Num of reservations: %s", departmentSlotCapacity, numberOfReservations));

        checkClosedHours(appointment);
    }

    private void checkClosedHours(Appointment appointment) throws ApiIllegalMethodException {
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

    //Canceling
    public String appointmentCanceling(AppointmentDto appointmentDto)
            throws ApiNoSuchElementException, ApiNoContentException, ApiIllegalArgumentException, ApiIllegalMethodException {
        checkValues(mapToEntity(appointmentDto));
        return appointmentUpdater(appointmentDto, AppointmentStatus.CANCELED);
    }

    //Updating
    public ResponseEntity<String> updateAppointmentHandler(Long departmentId, UUID userId, AppointmentDto appointmentDto)
            throws ApiNoSuchElementException, ApiNoContentException, ApiIllegalArgumentException, ApiIllegalMethodException {
        checkValues(mapToEntity(appointmentDto));

        AppointmentStatus status = appointmentDto.getStatus();
        String content = null;
        HttpStatus httpStatus = HttpStatus.ACCEPTED;

        if (status.equals(AppointmentStatus.RESERVED)) {
            content = appointmentReservation(departmentId, userId, appointmentDto);
            httpStatus = HttpStatus.CREATED;
        }

        else if (status.equals(AppointmentStatus.CANCELED))
            content = appointmentUpdater(appointmentDto, AppointmentStatus.CANCELED);

        else if (status.equals(AppointmentStatus.MISSED))
            content = appointmentUpdater(appointmentDto, AppointmentStatus.MISSED);

        else if (status.equals(AppointmentStatus.COMPLETED))
            content = appointmentUpdater(appointmentDto, AppointmentStatus.COMPLETED);

        return new ResponseEntity<>(content, httpStatus);
    }

    public String appointmentUpdater(AppointmentDto appointmentDto, AppointmentStatus status) {
        Appointment appointmentToUpdate = mapToEntity(appointmentDto);
        Appointment appointmentFromDb;

        appointmentFromDb = appointmentRepository.getById(appointmentToUpdate.getId());
        appointmentToUpdate.setUser(appointmentFromDb.getUser());
        appointmentToUpdate.setDepartment(appointmentFromDb.getDepartment());

        if (!appointmentToUpdate.getStatus().equals(status))
            throw new ApiIllegalMethodException(String.format(
                    "%s method not supported in the %s method!", appointmentToUpdate.getStatus(), status));

        return String.format("%s\n" +
                        "User: %s\n" +
                        "Department: %s\n" +
                        "Date: %s\n" +
                        "Hour: %s\n" +
                        "Status: %s",
                update(appointmentToUpdate),
                appointmentToUpdate.getUser().getName(),
                appointmentToUpdate.getDepartment().getName(),
                appointmentToUpdate.getDate(),
                appointmentToUpdate.getHour(),
                appointmentToUpdate.getStatus());
    }

    public String update(Appointment appointment) {
        appointmentRepository.save(appointment);

        return "The appointment updated!";
    }

    //Appointment values checking
    private void checkValues(Appointment appointment)
            throws ApiNoSuchElementException, ApiNoContentException, ApiIllegalArgumentException {
        Long id = appointment.getId();
        LocalDate date = appointment.getDate();
        LocalTime hour = appointment.getHour();
        AppointmentStatus status = appointment.getStatus();

        List<LocalTime> departmentHours = generateDepartmentHours(
                        appointment.getDepartment().getOpening(),
                        appointment.getDepartment().getClosing(),
                        appointment.getDepartment().getSlotLengthInHour());

        if (date == null)
            throw new ApiNoContentException("Empty the date!");

        else if (hour == null)
            throw new ApiNoContentException("Empty the hour!");

        else if (status == null)
            throw new ApiNoContentException("Empty the Appointment status!");

        else if (!List.of(AppointmentStatus.values()).contains(status))
            throw new ApiIllegalArgumentException(String.format("%s it does not exist!", status));

        else if (!departmentHours.contains(hour))
            throw new ApiIllegalArgumentException(String.format(
                    "This hour: %s not compatible with the department: %s!", hour, appointment.getDepartment().getName()));

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

    private Appointment mapToEntity(SimpleAppointmentDto simpleAppointmentDto) {
        return mapper.map(simpleAppointmentDto, Appointment.class);
    }

    private Appointment mapToEntity(AppointmentDto appointmentDto) {
        return mapper.map(appointmentDto, Appointment.class);
    }

    private AppointmentDto mapToDto(Appointment appointment) {
        return mapper.map(appointment, AppointmentDto.class);
    }
}
