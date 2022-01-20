package com.healingsys.services;

import com.healingsys.entities.OperationDetails;
import com.healingsys.repositories.AppointmentRepository;
import com.healingsys.repositories.DayOfWeekRepository;
import com.healingsys.repositories.OperationDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final OperationDetailsRepository operationDetailsRepository;
    private final DayOfWeekRepository dayOfWeekRepository;


    public Optional<OperationDetails> getOperationDetails() {
        long id = 1;
        return operationDetailsRepository.findById(id);
    }
}
