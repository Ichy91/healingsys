package com.healingsys.services;

import com.healingsys.dto.department.DepartmentDto;
import com.healingsys.dto.department.SimpleDepartmentDto;
import com.healingsys.entities.Department;
import com.healingsys.entities.enums.Status;
import com.healingsys.exceptions.ApiAlreadyExistException;
import com.healingsys.exceptions.ApiIllegalArgumentException;
import com.healingsys.exceptions.ApiNoContentException;
import com.healingsys.exceptions.ApiNoSuchElementException;
import com.healingsys.repositories.DepartmentRepository;
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
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final ModelMapper mapper;


    public List<SimpleDepartmentDto> getAllActive()
            throws ApiNoSuchElementException {
        List<Department> actives = departmentRepository.findAllByStatus(Status.ACTIVE);

        if (actives.isEmpty())
            throw new ApiNoSuchElementException(String.format("There is no such %s Departments!", Status.ACTIVE));

        return createSimpleDtoList(actives);
    }


    public List<SimpleDepartmentDto> getAllInactive()
            throws ApiNoSuchElementException {
        List<Department> inactivates = departmentRepository.findAllByStatus(Status.INACTIVE);

        if (inactivates.isEmpty())
            throw new ApiNoSuchElementException(String.format("There is no such %s Departments!", Status.INACTIVE));

        return createSimpleDtoList(inactivates);
    }


    public List<SimpleDepartmentDto> getAllDeleted()
            throws ApiNoSuchElementException {
        List<Department> deleted = departmentRepository.findAllByStatus(Status.DELETED);

        if (deleted.isEmpty())
            throw new ApiNoSuchElementException(String.format("There is no such %s Departments!", Status.DELETED));

        return createSimpleDtoList(deleted);
    }


    public DepartmentDto getDepartmentDtoById(Long id)
            throws ApiNoSuchElementException {

        return mapToDepartmentDto(getById(id));
    }


    public Department getEntityById(Long id)
            throws ApiNoSuchElementException {

        return getById(id);
    }


    public String saveDepartmentDetails(DepartmentDto departmentDto)
            throws ApiAlreadyExistException, ApiIllegalArgumentException, ApiNoContentException {
        checkValues(departmentDto);
        String departmentName = departmentDto.getName();

        if (!departmentRepository.findAllByName(departmentName).isEmpty())
            throw new ApiAlreadyExistException("Department already exist!");

        Department department = mapToEntity(departmentDto);
        departmentRepository.save(department);

        return "Operation details saved!";
    }


    public String updateDepartmentDetails(DepartmentDto departmentDto, Long id)
            throws ApiNoSuchElementException, ApiIllegalArgumentException, ApiNoContentException {
        if (departmentRepository.findById(id).isEmpty())
            throw new ApiNoSuchElementException(String.format("No find Department with id: %s!", id));

        checkValues(departmentDto);

        Department department = mapToEntity(departmentDto);
        department.setId(id);
        departmentRepository.save(department);

        return "Operation details updated!";
    }


    private Department getById(Long departmentId)
            throws ApiNoSuchElementException {
        Optional<Department> departmentDetails = departmentRepository.findById(departmentId);

        if (departmentDetails.isEmpty())
            throw new ApiNoSuchElementException(String.format("No find Department with id: %s!", departmentId));

        return departmentDetails.get();
    }


    private void checkValues(DepartmentDto departmentDto)
            throws ApiIllegalArgumentException, ApiNoContentException {
        String departmentName = departmentDto.getName();

        LocalTime opening = departmentDto.getOpening();
        LocalTime closing = departmentDto.getClosing();
        LocalTime standard = LocalTime.of(0,0);
        Duration durationOfOpening = Duration.between(opening, standard);
        Duration durationOfClosing = Duration.between(closing, standard);

        int maxGeneratedDays = departmentDto.getMaxGeneratedDays();
        int slotMaxCapacity = departmentDto.getSlotMaxCapacity();
        double slotLengthInHour = departmentDto.getSlotLengthInHour();


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


    private List<SimpleDepartmentDto> createSimpleDtoList(List<Department> departmentList) {
        List<SimpleDepartmentDto> simpleDepartmentDtoList = new ArrayList<>();

        for (var operationDetails: departmentList) {
            simpleDepartmentDtoList.add(mapToSimpleDto(operationDetails));
        }

        return simpleDepartmentDtoList;
    }

    private SimpleDepartmentDto mapToSimpleDto(Department department) {
        return mapper.map(department, SimpleDepartmentDto.class);
    }

    private DepartmentDto mapToDepartmentDto(Department department) {
        return mapper.map(department, DepartmentDto.class);
    }

    private Department mapToEntity(DepartmentDto departmentDto) {
        return mapper.map(departmentDto, Department.class);
    }
}
