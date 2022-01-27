package com.healingsys.services;

import com.healingsys.dto.DepartmentDetailsDto;
import com.healingsys.dto.SimpleDepartmentDetailsDto;
import com.healingsys.entities.DepartmentDetails;
import com.healingsys.entities.enums.Status;
import com.healingsys.repositories.DepartmentDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentDetailsService {
    private final DepartmentDetailsRepository departmentDetailsRepository;
    private final ModelMapper mapper;

    public List<SimpleDepartmentDetailsDto> getAllActive() {
        List<DepartmentDetails> actives = departmentDetailsRepository.findAllByStatus(Status.ACTIVE);

        if (actives.isEmpty()) {
            throw new NoSuchElementException("There is no such Operations!");
        }

        return createSimpleDtoList(actives);
    }

    public List<SimpleDepartmentDetailsDto> getAllInactive() {
        List<DepartmentDetails> inactivates = departmentDetailsRepository.findAllByStatus(Status.INACTIVE);

        if (inactivates.isEmpty()) {
            throw new NoSuchElementException("There is no such Operations!");
        }

        return createSimpleDtoList(inactivates);
    }

    public List<SimpleDepartmentDetailsDto> getAllDeleted() {
        List<DepartmentDetails> deleted = departmentDetailsRepository.findAllByStatus(Status.DELETED);

        if (deleted.isEmpty()) {
            throw new NoSuchElementException("There is no such Operations!");
        }

        return createSimpleDtoList(deleted);
    }

    public DepartmentDetailsDto getById(Long id) {
        Optional<DepartmentDetails> operationDetails = departmentDetailsRepository.findById(id);

        if (operationDetails.isEmpty()) {
            throw new NoSuchElementException("There is no such Operations!");
        }

        return mapToModifyDto(operationDetails.get());
    }

    public String saveDepartmentDetails(DepartmentDetailsDto departmentDetailsDto) {
        DepartmentDetails departmentDetails = mapToEntity(departmentDetailsDto);
        departmentDetailsRepository.save(departmentDetails);

        return "Operation details saved!";
    }

    public String updateDepartmentDetails(DepartmentDetailsDto departmentDetailsDto, Long id) {

        if (departmentDetailsRepository.findById(id).isEmpty()) {
            throw new NoSuchElementException(String.format("No find Operation with %s id", id));
        }
        else {
            DepartmentDetails departmentDetails = mapToEntity(departmentDetailsDto);
            departmentDetails.setId(id);
            departmentDetailsRepository.save(departmentDetails);

            return "Operation details updated!";
        }

    }

    private List<SimpleDepartmentDetailsDto> createSimpleDtoList(List<DepartmentDetails> departmentDetailsList) {
        List<SimpleDepartmentDetailsDto> simpleDepartmentDetailsDtoList = new ArrayList<>();

        for (var operationDetails: departmentDetailsList) {
            simpleDepartmentDetailsDtoList.add(mapToSimpleDto(operationDetails));
        }

        return simpleDepartmentDetailsDtoList;
    }

    //Dto - Entity mapping
    private SimpleDepartmentDetailsDto mapToSimpleDto(DepartmentDetails departmentDetails) {
        return mapper.map(departmentDetails, SimpleDepartmentDetailsDto.class);
    }

    private DepartmentDetailsDto mapToModifyDto(DepartmentDetails departmentDetails) {
        return mapper.map(departmentDetails, DepartmentDetailsDto.class);
    }

    private DepartmentDetails mapToEntity(DepartmentDetailsDto departmentDetailsDto) {
        return mapper.map(departmentDetailsDto, DepartmentDetails.class);
    }
}
