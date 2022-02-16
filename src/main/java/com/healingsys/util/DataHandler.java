package com.healingsys.util;

import com.healingsys.entities.ClosedTime;
import com.healingsys.entities.DepartmentDetails;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class DataHandler {

    //Daily closed hours setting
    public List<LocalTime> setupClosedHours(DepartmentDetails details, List<ClosedTime> closedAppointments, List<LocalTime> closedHours) {
        LocalTime startTime;
        LocalTime endTime;
        int slotLengthInMinute = (int) (details.getSlotLengthInHour() * 60);

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

        return closedHours;
    }
}
