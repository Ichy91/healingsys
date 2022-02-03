package com.healingsys.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.healingsys.dto.ClosedAppointmentDto;
import com.healingsys.dto.DepartmentDetailsDto;
import com.healingsys.services.AppointmentService;
import com.healingsys.services.ClosedTimeService;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
public class Day {
    @JsonIgnore
    private AppointmentService appointmentService;
    @JsonIgnore
    private ClosedTimeService closedTimeService;
    @JsonIgnore
    private DepartmentDetailsDto details;
    @JsonIgnore
    private List<LocalTime> closedHours;

    private LocalDate day;
    private List<Slot> slots;


    public Day(AppointmentService appointmentService,
               ClosedTimeService closedTimeService,
               DepartmentDetailsDto details,
               LocalDate day) {
        this.appointmentService = appointmentService;
        this.closedTimeService = closedTimeService;
        this.details = details;
        this.day = day;
    }


    public void dayHandler(Long departmentId) {
        List<ClosedAppointmentDto> closedAppointments =
                closedTimeService.getAllClosedAppointmentByDepartmentAndDay(departmentId, day);

        if (closedAppointments != null) setupClosedHours(closedAppointments);

        if (slots == null) slots = new ArrayList<>();
        else slots.clear();

        setupSlots();
    }

    private void setupClosedHours(List<ClosedAppointmentDto> closedAppointments) {
        LocalTime startTime;
        LocalTime endTime;
        long slotLengthInMinute = (long) (details.getSlotLengthInHour() * 60);

        for (var closedAppointment: closedAppointments) {
            if(closedAppointment.getClosedForm() == null) startTime = details.getOpening();
            else startTime = closedAppointment.getClosedForm();

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
        int numberOfReservation = appointmentService.getReservedAppointmentsByDayAndHour(day, slot.getTime()).size();
        slot.setCapacity(details.getSlotMaxCapacity());
        slot.setReserved(numberOfReservation);

        if (closedHours.contains(slot.getTime()))
            slot.setSlotStatus(SlotStatus.INACTIVE);

        else if (numberOfReservation >= slot.getCapacity())
            slot.setSlotStatus(SlotStatus.INACTIVE);

        else if (slot.getTime().compareTo(LocalTime.now()) <= 0 && this.day.compareTo(LocalDate.now()) <= 0)
            slot.setSlotStatus(SlotStatus.INACTIVE);

        else slot.setSlotStatus(SlotStatus.ACTIVE);
    }

    private int calculateNumberOfSlots() {
        long openHours = ChronoUnit.HOURS.between(details.getOpening(), details.getClosing());
        return (int) Math.floor(openHours / details.getSlotLengthInHour());
    }


}
