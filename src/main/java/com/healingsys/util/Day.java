package com.healingsys.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.healingsys.entities.Appointment;
import com.healingsys.entities.ClosedTime;
import com.healingsys.entities.Department;
import com.healingsys.entities.enums.AppointmentStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class Day {
    @JsonIgnore
    private DataHandler dataHandler;
    @JsonIgnore
    private Department details;
    @JsonIgnore
    private List<Appointment> appointments;
    @JsonIgnore
    private List<ClosedTime> closedAppointments;

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


    public Day(List<Appointment> appointments,
               List<ClosedTime> closedAppointments,
               Department details,
               LocalDate day,
               DataHandler dataHandler) {
        this.appointments = appointments;
        this.closedAppointments = closedAppointments;
        this.details = details;
        this.day = day;
        this.dataHandler = dataHandler;
    }


    public void dayHandler(UUID userId) {

        closedHours = listOfHoursInitialisation(closedHours);
        reservedHours = listOfHoursInitialisation(reservedHours);
        completedHours = listOfHoursInitialisation(completedHours);
        missedHours = listOfHoursInitialisation(missedHours);
        canceledHours = listOfHoursInitialisation(canceledHours);

        if (userId != null) userAppointmentsHandler(userId);
        else hasReservation = false;

        if (!closedAppointments.isEmpty())
            closedHours = dataHandler.setupClosedHours(details, closedAppointments, closedHours);

        if (slots == null) slots = new ArrayList<>();
        else slots.clear();

        setupSlots();
    }

    //Initialisation of the used list
    private List<LocalTime> listOfHoursInitialisation(List<LocalTime> hoursList) {
        if (hoursList == null) hoursList = new ArrayList<>();
        else hoursList.clear();

        return hoursList;
    }

    //Slot generating
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
        int numberOfReservation = (int) appointments.stream()
                .filter(appointment -> appointment.getStatus().equals(AppointmentStatus.RESERVED) &&
                        appointment.getDate().compareTo(day) == 0 &&
                        appointment.getHour().compareTo(slot.getTime()) == 0)
                .count();

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

    private void slotAppointmentStatusSetting(Slot slot) {
        if (reservedHours.contains(slot.getTime()))
            slot.setAppointmentStatus(AppointmentStatus.RESERVED);

        else if (completedHours.contains(slot.getTime()))
            slot.setAppointmentStatus(AppointmentStatus.COMPLETED);

        else if (missedHours.contains(slot.getTime()))
            slot.setAppointmentStatus(AppointmentStatus.MISSED);

        else if (canceledHours.contains(slot.getTime()))
            slot.setAppointmentStatus(AppointmentStatus.CANCELED);
    }

    //Appointments of User handling
    private void userAppointmentsHandler(UUID userId) {
        List<Appointment> userReservations = appointments.stream()
                .filter(appointment -> appointment.getUser().getId().equals(userId) &&
                        appointment.getStatus().equals(AppointmentStatus.RESERVED))
                .collect(Collectors.toList());

        hasReservation = !userReservations.isEmpty();

        List<Appointment> userReservedAppointmentsToDay = userAppointmentsFiltering(userId, AppointmentStatus.RESERVED);

        List<Appointment> userCompletedAppointmentsToDay = userAppointmentsFiltering(userId, AppointmentStatus.COMPLETED);

        List<Appointment> userMissedAppointmentsToDay = userAppointmentsFiltering(userId, AppointmentStatus.MISSED);

        List<Appointment> userCanceledAppointmentsToDay = userAppointmentsFiltering(userId, AppointmentStatus.CANCELED);

        appointmentHoursHandler(userReservedAppointmentsToDay, reservedHours);
        appointmentHoursHandler(userCompletedAppointmentsToDay, completedHours);
        appointmentHoursHandler(userMissedAppointmentsToDay, missedHours);
        appointmentHoursHandler(userCanceledAppointmentsToDay, canceledHours);
    }

    private List<Appointment> userAppointmentsFiltering(UUID userId, AppointmentStatus status) {
        return appointments.stream()
                .filter(appointment -> appointment.getUser().getId().equals(userId) &&
                        appointment.getStatus().equals(status) &&
                        appointment.getDate().compareTo(day) == 0)
                .sorted(Comparator.comparing(Appointment::getHour))
                .collect(Collectors.toList());
    }

    private void appointmentHoursHandler(List<Appointment> appointments, List<LocalTime> hours) {
        if (!appointments.isEmpty()){
            for (var appointment : appointments)
                hours.add(appointment.getHour());
        }
    }

    //Helper methods
    private int calculateNumberOfSlots() {
        long openHours = ChronoUnit.HOURS.between(details.getOpening(), details.getClosing());
        return (int) Math.floor(openHours / details.getSlotLengthInHour());
    }
}
