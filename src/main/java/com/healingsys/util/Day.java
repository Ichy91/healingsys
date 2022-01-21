package com.healingsys.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.healingsys.entities.OperationDetails;
import com.healingsys.services.AppointmentService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Day {
    private LocalDate day;
    private List<Slot> slots;

    @JsonIgnore
    private OperationDetails details;
    @JsonIgnore
    private AppointmentService appointmentService;

    public Day(LocalDate day, OperationDetails details, AppointmentService appointmentService) {
        this.day = day;
        this.details = details;
        this.appointmentService = appointmentService;
    }


    public void dayHandler() {
        if (slots == null || slots.isEmpty()) setupSlots();
    }

    private void setupSlots() {
        slots = new ArrayList<>();

        int slotsNumberOfDay = calculateNumberOfSlots();
        long slotLengthInMinute = (long) (details.getSlotLengthInHour() * 60);
        LocalTime time = details.getOpen();

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

        if (numberOfReservation >= slot.getCapacity()) slot.setSlotStatus(SlotStatus.INACTIVE);
        else slot.setSlotStatus(SlotStatus.ACTIVE);
    }

    private int calculateNumberOfSlots() {
        long openHours = ChronoUnit.HOURS.between(details.getOpen(), details.getClosed());
        return (int) Math.floor(openHours / details.getSlotLengthInHour());
    }


}
