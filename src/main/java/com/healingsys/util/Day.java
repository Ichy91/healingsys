package com.healingsys.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.healingsys.dto.ClosedAppointmentDto;
import com.healingsys.dto.DepartmentDetailsDto;
import com.healingsys.entities.Appointment;
import com.healingsys.entities.enums.AppointmentStatus;
import com.healingsys.services.AppointmentService;
import com.healingsys.services.ClosedTimeService;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Day {
    @JsonIgnore
    private AppointmentService appointmentService;
    @JsonIgnore
    private ClosedTimeService closedTimeService;
    @JsonIgnore
    private DepartmentDetailsDto details;
    @JsonIgnore
    private Long departmentId;
    @JsonIgnore
    private List<LocalTime> closedHours;
    @JsonIgnore
    private List<LocalTime> reservedHours;
    @JsonIgnore
    private List<LocalTime> completedHours;
    @JsonIgnore
    private List<LocalTime> missedHours;
    @JsonIgnore
    private List<LocalTime> canceledHours;
    @JsonIgnore
    private boolean hasReservation;

    private LocalDate day;
    private List<Slot> slots;


    public Day(AppointmentService appointmentService,
               ClosedTimeService closedTimeService,
               DepartmentDetailsDto details,
               LocalDate day,
               Long departmentId) {
        this.appointmentService = appointmentService;
        this.closedTimeService = closedTimeService;
        this.details = details;
        this.day = day;
        this.departmentId = departmentId;
    }


    public void dayHandler(List<Appointment> reservedAppointments, UUID userId) {
        List<ClosedAppointmentDto> closedAppointments =
                closedTimeService.getAllClosedAppointmentByDepartmentAndDay(departmentId, day);

        closedHours = listOfHoursInitialisation(closedHours);
        reservedHours = listOfHoursInitialisation(reservedHours);
        completedHours = listOfHoursInitialisation(completedHours);
        missedHours = listOfHoursInitialisation(missedHours);
        canceledHours = listOfHoursInitialisation(canceledHours);

        if (userId != null) userAppointmentsHandler(reservedAppointments, userId);
        else hasReservation = false;

        if (closedAppointments != null) setupClosedHours(closedAppointments);

        if (slots == null) slots = new ArrayList<>();
        else slots.clear();

        setupSlots();
    }


    private List<LocalTime> listOfHoursInitialisation(List<LocalTime> hoursList) {
        if (hoursList == null) hoursList = new ArrayList<>();
        else hoursList.clear();

        return hoursList;
    }


    private void setupClosedHours(List<ClosedAppointmentDto> closedAppointments) {
        LocalTime startTime;
        LocalTime endTime;
        long slotLengthInMinute = (long) (details.getSlotLengthInHour() * 60);

        for (var closedAppointment: closedAppointments) {
            if(closedAppointment.getClosedFrom() == null) startTime = details.getOpening();
            else startTime = closedAppointment.getClosedFrom();

            if(closedAppointment.getClosedTo() == null) endTime = details.getClosing();
            else endTime = closedAppointment.getClosedTo();

            while (startTime.compareTo(endTime) < 0) {
                closedHours.add(startTime);
                startTime = startTime.plusMinutes(slotLengthInMinute);
            }
        }
    }


    private void setupSlots() {
        int slotsNumberOfDay = calculateNumberOfSlots();
        long slotLengthInMinute = (long) (details.getSlotLengthInHour() * 60);
        LocalTime time = details.getOpening();

        for (int i = 0; i < slotsNumberOfDay; i++) {
            slots.add(generateSlot(time));
            time = time.plusMinutes(slotLengthInMinute);
        }
    }


    private Slot generateSlot(LocalTime time) {
        Slot actualSlot = new Slot();
        actualSlot.setTime(time);
        slotSetting(actualSlot);

        return actualSlot;
    }


    private void slotSetting(Slot slot) {
        int numberOfReservation = appointmentService.getReservedAppointmentsByDepartmentAndDayAndHour(departmentId, day, slot.getTime()).size();
        slot.setCapacity(details.getSlotMaxCapacity());
        slot.setReserved(numberOfReservation);

        if (hasReservation)
            slot.setSlotStatus(SlotStatus.INACTIVE);

        else if (closedHours.contains(slot.getTime()))
            slot.setSlotStatus(SlotStatus.INACTIVE);

        else if (numberOfReservation >= slot.getCapacity())
            slot.setSlotStatus(SlotStatus.INACTIVE);

        else if (slot.getTime().compareTo(LocalTime.now()) <= 0 && this.day.compareTo(LocalDate.now()) <= 0)
            slot.setSlotStatus(SlotStatus.INACTIVE);

        else slot.setSlotStatus(SlotStatus.ACTIVE);

        slotAppointmentStatusSetting(slot);
    }


    public void slotAppointmentStatusSetting(Slot slot) {
        if (reservedHours.contains(slot.getTime()))
            slot.setAppointmentStatus(AppointmentStatus.RESERVED);

        else if (completedHours.contains(slot.getTime()))
            slot.setAppointmentStatus(AppointmentStatus.COMPLETED);

        else if (missedHours.contains(slot.getTime()))
            slot.setAppointmentStatus(AppointmentStatus.MISSED);

        else if (canceledHours.contains(slot.getTime()))
            slot.setAppointmentStatus(AppointmentStatus.CANCELED);
    }


    private void userAppointmentsHandler(List<Appointment> reservedAppointments, UUID userId) {
        hasReservation = !reservedAppointments.isEmpty();

        List<Appointment> reservedAppointmentsToDay =
                appointmentService.getReservedAppointmentsByDepartmentAndUserAndDay(departmentId, userId, day);
        List<Appointment> completedAppointmentsToDay =
                appointmentService.getCompletedAppointmentsByDepartmentAndUserAndDay(departmentId, userId, day);
        List<Appointment> missedAppointmentsToDay =
                appointmentService.getMissedAppointmentsByDepartmentAndUserAndDay(departmentId, userId, day);
        List<Appointment> canceledAppointmentsToDay =
                appointmentService.getCanceledAppointmentsByDepartmentAndUserAndDay(departmentId, userId, day);

        appointmentHoursHandler(reservedAppointmentsToDay, reservedHours);
        appointmentHoursHandler(completedAppointmentsToDay, completedHours);
        appointmentHoursHandler(missedAppointmentsToDay, missedHours);
        appointmentHoursHandler(canceledAppointmentsToDay, canceledHours);
    }


    private void appointmentHoursHandler(List<Appointment> appointments, List<LocalTime> hours) {
        if (!appointments.isEmpty()){
            for (var appointment : appointments)
                hours.add(appointment.getHour());
        }
    }


    private int calculateNumberOfSlots() {
        long openHours = ChronoUnit.HOURS.between(details.getOpening(), details.getClosing());
        return (int) Math.floor(openHours / details.getSlotLengthInHour());
    }
}
