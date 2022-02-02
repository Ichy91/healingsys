package com.healingsys.services;

import com.healingsys.dto.DepartmentDetailsDto;
import com.healingsys.dto.SimpleDepartmentDetailsDto;
import com.healingsys.entities.DepartmentDetails;
import com.healingsys.entities.enums.Status;
import com.healingsys.exception.ApiRequestException;
import com.healingsys.repositories.DepartmentDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentDetailsService {
    private final DepartmentDetailsRepository departmentDetailsRepository;
    private final ModelMapper mapper;

    public List<SimpleDepartmentDetailsDto> getAllActive() throws ApiRequestException {
        List<DepartmentDetails> actives = departmentDetailsRepository.findAllByStatus(Status.ACTIVE);

        if (actives.isEmpty())
            throw new ApiRequestException(
                    String.format("There is no such %s Departments!", Status.ACTIVE),
                    HttpStatus.NOT_FOUND);

        return createSimpleDtoList(actives);
    }

    public List<SimpleDepartmentDetailsDto> getAllInactive() throws ApiRequestException {
        List<DepartmentDetails> inactivates = departmentDetailsRepository.findAllByStatus(Status.INACTIVE);

        if (inactivates.isEmpty())
            throw new ApiRequestException(
                    String.format("There is no such %s Departments!", Status.INACTIVE),
                    HttpStatus.NOT_FOUND);

        return createSimpleDtoList(inactivates);
    }

    public List<SimpleDepartmentDetailsDto> getAllDeleted() throws ApiRequestException{
        List<DepartmentDetails> deleted = departmentDetailsRepository.findAllByStatus(Status.DELETED);

        if (deleted.isEmpty())
            throw new ApiRequestException(
                    String.format("There is no such %s Departments!", Status.DELETED),
                    HttpStatus.NOT_FOUND);

        return createSimpleDtoList(deleted);
    }

    public DepartmentDetailsDto getById(Long id) throws ApiRequestException {
        Optional<DepartmentDetails> operationDetails = departmentDetailsRepository.findById(id);

        if (operationDetails.isEmpty())
            throw new ApiRequestException(
                    String.format("No find Department with %s id", id),
                    HttpStatus.NOT_FOUND);

        return mapToModifyDto(operationDetails.get());
    }

    public String saveDepartmentDetails(DepartmentDetailsDto departmentDetailsDto) throws ApiRequestException {
        String departmentName = departmentDetailsDto.getName();

        if (!departmentDetailsRepository.findAllByName(departmentName).isEmpty())
            throw new ApiRequestException("Department already exist!", HttpStatus.CONFLICT);

        DepartmentDetails departmentDetails = mapToEntity(departmentDetailsDto);
        departmentDetailsRepository.save(departmentDetails);

        return "Operation details saved!";
    }

    public String updateDepartmentDetails(DepartmentDetailsDto departmentDetailsDto, Long id) throws ApiRequestException {
        if (departmentDetailsRepository.findById(id).isEmpty())
            throw new ApiRequestException(
                    String.format("No find Department with %s id", id),
                    HttpStatus.NOT_FOUND);

        DepartmentDetails departmentDetails = mapToEntity(departmentDetailsDto);
        departmentDetails.setId(id);
        departmentDetailsRepository.save(departmentDetails);

        return "Operation details updated!";
    }

    private List<SimpleDepartmentDetailsDto> createSimpleDtoList(List<DepartmentDetails> departmentDetailsList) throws ApiRequestException {
        if (departmentDetailsList.isEmpty())
            throw new ApiRequestException("The list is empty!", HttpStatus.NOT_FOUND);

        List<SimpleDepartmentDetailsDto> simpleDepartmentDetailsDtoList = new ArrayList<>();

        for (var operationDetails: departmentDetailsList) {
            simpleDepartmentDetailsDtoList.add(mapToSimpleDto(operationDetails));
        }

        return simpleDepartmentDetailsDtoList;
    }

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
