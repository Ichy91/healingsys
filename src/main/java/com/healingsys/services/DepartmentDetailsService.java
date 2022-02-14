package com.healingsys.services;

import com.healingsys.dto.DepartmentDetailsDto;
import com.healingsys.dto.SimpleDepartmentDetailsDto;
import com.healingsys.entities.DepartmentDetails;
import com.healingsys.entities.enums.Status;
import com.healingsys.exception.ApiAlreadyExistException;
import com.healingsys.exception.ApiIllegalArgumentException;
import com.healingsys.exception.ApiNoContentException;
import com.healingsys.exception.ApiNoSuchElementException;
import com.healingsys.repositories.DepartmentDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentDetailsService {
    private final DepartmentDetailsRepository departmentDetailsRepository;
    private final ModelMapper mapper;


    public List<SimpleDepartmentDetailsDto> getAllActive()
            throws ApiNoSuchElementException {
        List<DepartmentDetails> actives = departmentDetailsRepository.findAllByStatus(Status.ACTIVE);

        if (actives.isEmpty())
            throw new ApiNoSuchElementException(String.format("There is no such %s Departments!", Status.ACTIVE));

        return createSimpleDtoList(actives);
    }


    public List<SimpleDepartmentDetailsDto> getAllInactive()
            throws ApiNoSuchElementException {
        List<DepartmentDetails> inactivates = departmentDetailsRepository.findAllByStatus(Status.INACTIVE);

        if (inactivates.isEmpty())
            throw new ApiNoSuchElementException(String.format("There is no such %s Departments!", Status.INACTIVE));

        return createSimpleDtoList(inactivates);
    }


    public List<SimpleDepartmentDetailsDto> getAllDeleted()
            throws ApiNoSuchElementException {
        List<DepartmentDetails> deleted = departmentDetailsRepository.findAllByStatus(Status.DELETED);

        if (deleted.isEmpty())
            throw new ApiNoSuchElementException(String.format("There is no such %s Departments!", Status.DELETED));

        return createSimpleDtoList(deleted);
    }


    public DepartmentDetailsDto getDepartmentDtoById(Long id)
            throws ApiNoSuchElementException {

        return mapToDepartmentDto(getById(id));
    }


    public DepartmentDetails getEntityById(Long id)
            throws ApiNoSuchElementException {

        return getById(id);
    }


    public String saveDepartmentDetails(DepartmentDetailsDto departmentDetailsDto)
            throws ApiAlreadyExistException, ApiIllegalArgumentException, ApiNoContentException {
        checkValues(departmentDetailsDto);
        String departmentName = departmentDetailsDto.getName();

        if (!departmentDetailsRepository.findAllByName(departmentName).isEmpty())
            throw new ApiAlreadyExistException("Department already exist!");

        DepartmentDetails departmentDetails = mapToEntity(departmentDetailsDto);
        departmentDetailsRepository.save(departmentDetails);

        return "Operation details saved!";
    }


    public String updateDepartmentDetails(DepartmentDetailsDto departmentDetailsDto, Long id)
            throws ApiNoSuchElementException, ApiIllegalArgumentException, ApiNoContentException {
        if (departmentDetailsRepository.findById(id).isEmpty())
            throw new ApiNoSuchElementException(String.format("No find Department with id: %s!", id));

        checkValues(departmentDetailsDto);

        DepartmentDetails departmentDetails = mapToEntity(departmentDetailsDto);
        departmentDetails.setId(id);
        departmentDetailsRepository.save(departmentDetails);

        return "Operation details updated!";
    }


    private DepartmentDetails getById(Long departmentId)
            throws ApiNoSuchElementException {
        Optional<DepartmentDetails> departmentDetails = departmentDetailsRepository.findById(departmentId);

        if (departmentDetails.isEmpty())
            throw new ApiNoSuchElementException(String.format("No find Department with id: %s!", departmentId));

        return departmentDetails.get();
    }


    private void checkValues(DepartmentDetailsDto departmentDetailsDto)
            throws ApiIllegalArgumentException, ApiNoContentException {
        String departmentName = departmentDetailsDto.getName();

        LocalTime opening = departmentDetailsDto.getOpening();
        LocalTime closing = departmentDetailsDto.getClosing();
        LocalTime standard = LocalTime.of(0,0);
        Duration durationOfOpening = Duration.between(opening, standard);
        Duration durationOfClosing = Duration.between(closing, standard);

        int maxGeneratedDays = departmentDetailsDto.getMaxGeneratedDays();
        int slotMaxCapacity = departmentDetailsDto.getSlotMaxCapacity();
        double slotLengthInHour = departmentDetailsDto.getSlotLengthInHour();


        if (departmentName == null || departmentName.equals(""))
            throw new ApiNoContentException("Give a name for the Department!");

        if (((double) durationOfOpening.toMinutes() / 30) % 1 != 0)
            throw new ApiIllegalArgumentException("The opening time must be specified in 0.5 hours!");

        if (((double) durationOfClosing.toMinutes() / 30) % 1 != 0)
            throw new ApiIllegalArgumentException("The closing time must be specified in 0.5 hours!");

        if (opening.compareTo(closing) >= 0)
            throw new ApiIllegalArgumentException("The opening time must be less than the closing time!");

        if (maxGeneratedDays < 1)
            throw new ApiIllegalArgumentException("The number of days generated cannot be less than 1!");

        if (maxGeneratedDays > 30)
            throw new ApiIllegalArgumentException("The number of days generated cannot be more than 30!");

        if (slotMaxCapacity < 1)
            throw new ApiIllegalArgumentException("The slot capacity cannot be less than 1!");

        if ((slotLengthInHour / 0.5) % 1 != 0)
            throw new ApiIllegalArgumentException("The length of the slots can only be increased or decreased by 0.5 hours!");

        if (slotLengthInHour < 0.5)
            throw new ApiIllegalArgumentException("The length of the slot should not be less than 0.5 hours, if the duration of the slot per person is less than 0.5 hours, increase its capacity!");

        if (slotLengthInHour > 5.0)
            throw new ApiIllegalArgumentException("The duration of the slot cannot be more than 5.0 hours!");
    }


    private List<SimpleDepartmentDetailsDto> createSimpleDtoList(List<DepartmentDetails> departmentDetailsList) {
        List<SimpleDepartmentDetailsDto> simpleDepartmentDetailsDtoList = new ArrayList<>();

        for (var operationDetails: departmentDetailsList) {
            simpleDepartmentDetailsDtoList.add(mapToSimpleDto(operationDetails));
        }

        return simpleDepartmentDetailsDtoList;
    }

    private SimpleDepartmentDetailsDto mapToSimpleDto(DepartmentDetails departmentDetails) {
        return mapper.map(departmentDetails, SimpleDepartmentDetailsDto.class);
    }

    private DepartmentDetailsDto mapToDepartmentDto(DepartmentDetails departmentDetails) {
        return mapper.map(departmentDetails, DepartmentDetailsDto.class);
    }

    private DepartmentDetails mapToEntity(DepartmentDetailsDto departmentDetailsDto) {
        return mapper.map(departmentDetailsDto, DepartmentDetails.class);
    }
}
