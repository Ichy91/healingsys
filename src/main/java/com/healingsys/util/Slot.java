package com.healingsys.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Slot {

    private int capacity;
    private int reserved;
    private SlotStatus slotStatus;
    private LocalTime time;
}
