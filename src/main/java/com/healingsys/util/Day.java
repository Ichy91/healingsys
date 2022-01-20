package com.healingsys.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.healingsys.entities.OperationDetails;
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

    @JsonIgnore
    private OperationDetails details;
    private LocalDate day;
    private List<Slot> slots;

    public Day(LocalDate day, OperationDetails details) {
        this.day = day;
        this.details = details;
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
            slots.add(generateSlot(details.getSlotMaxCapacity(), time));
            time = time.plusMinutes(slotLengthInMinute);
        }
    }

    private Slot generateSlot(int capacity, LocalTime time) {
        Slot actualSlot = new Slot();
        actualSlot.setCapacity(capacity);
        actualSlot.setTime(time);
        return actualSlot;
    }

    private int calculateNumberOfSlots() {
        long openHours = ChronoUnit.HOURS.between(details.getOpen(), details.getClosed());
        return (int) Math.floor(openHours / details.getSlotLengthInHour());
    }


}
