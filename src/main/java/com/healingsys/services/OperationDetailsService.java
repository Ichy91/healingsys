package com.healingsys.services;

import com.healingsys.dto.OperationDetailsDto;
import com.healingsys.dto.SimpleOperationDetailsDto;
import com.healingsys.entities.OperationDetails;
import com.healingsys.entities.enums.Status;
import com.healingsys.repositories.OperationDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OperationDetailsService {
    private final OperationDetailsRepository operationDetailsRepository;
    private final ModelMapper mapper;

    public List<SimpleOperationDetailsDto> getAllActive() {
        List<OperationDetails> actives = operationDetailsRepository.findAllByStatus(Status.ACTIVE);

        if (actives.isEmpty()) {
            throw new NoSuchElementException("There is no such Operations!");
        }

        return createSimpleDtoList(actives);
    }

    public List<SimpleOperationDetailsDto> getAllInactive() {
        List<OperationDetails> inactivates = operationDetailsRepository.findAllByStatus(Status.INACTIVE);

        if (inactivates.isEmpty()) {
            throw new NoSuchElementException("There is no such Operations!");
        }

        return createSimpleDtoList(inactivates);
    }

    public List<SimpleOperationDetailsDto> getAllDeleted() {
        List<OperationDetails> deleted = operationDetailsRepository.findAllByStatus(Status.DELETED);

        if (deleted.isEmpty()) {
            throw new NoSuchElementException("There is no such Operations!");
        }

        return createSimpleDtoList(deleted);
    }

    public OperationDetailsDto getById(Long id) {
        Optional<OperationDetails> operationDetails = operationDetailsRepository.findById(id);

        if (operationDetails.isEmpty()) {
            throw new NoSuchElementException("There is no such Operations!");
        }

        return mapToModifyDto(operationDetails.get());
    }

    public String saveOperationDetails(OperationDetailsDto operationDetailsDto) {
        OperationDetails operationDetails = mapToEntity(operationDetailsDto);
        operationDetailsRepository.save(operationDetails);

        return "Operation details saved!";
    }

    public String updateOperationDetails(OperationDetailsDto operationDetailsDto, Long id) {

        if (operationDetailsRepository.findById(id).isEmpty()) {
            throw new NoSuchElementException(String.format("No find Operation with %s id", id));
        }
        else {
            OperationDetails operationDetails = mapToEntity(operationDetailsDto);
            operationDetails.setId(id);
            operationDetailsRepository.save(operationDetails);

            return "Operation details updated!";
        }

    }

    private List<SimpleOperationDetailsDto> createSimpleDtoList(List<OperationDetails> operationDetailsList) {
        List<SimpleOperationDetailsDto> simpleOperationDetailsDtoList = new ArrayList<>();

        for (var operationDetails: operationDetailsList) {
            simpleOperationDetailsDtoList.add(mapToSimpleDto(operationDetails));
        }

        return simpleOperationDetailsDtoList;
    }

    //Dto - Entity mapping
    private SimpleOperationDetailsDto mapToSimpleDto(OperationDetails operationDetails) {
        return mapper.map(operationDetails, SimpleOperationDetailsDto.class);
    }

    private OperationDetailsDto mapToModifyDto(OperationDetails operationDetails) {
        return mapper.map(operationDetails, OperationDetailsDto.class);
    }

    private OperationDetails mapToEntity(OperationDetailsDto operationDetailsDto) {
        return mapper.map(operationDetailsDto, OperationDetails.class);
    }
}
