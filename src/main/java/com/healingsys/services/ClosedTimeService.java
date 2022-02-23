package com.healingsys.services;

import com.healingsys.dto.closedTime.ClosedAppointmentDto;
import com.healingsys.dto.department.SimpleDepartmentDetailsDto;
import com.healingsys.entities.ClosedTime;
import com.healingsys.exceptions.*;
import com.healingsys.repositories.ClosedTimeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClosedTimeService {
    private final ClosedTimeRepository closedTimeRepository;
    private final ModelMapper mapper;

    public ClosedAppointmentDto getById(Long id)
            throws ApiNoSuchElementException {
        Optional<ClosedTime> closedTime = closedTimeRepository.findById(id);

        if (closedTime.isEmpty())
            throw new ApiNoSuchElementException(String.format("No find closed appointment with id: %s!", id));

        return mapToDto(closedTime.get());
    }


    public List<ClosedAppointmentDto> getClosedAppointmentByDepartment(Long departmentId)
            throws ApiNoSuchElementException {
        List<ClosedTime> closedTimeList = closedTimeRepository.findAllByDepartmentDetailsId(departmentId);

        if (closedTimeList.isEmpty())
            throw new ApiNoSuchElementException(
                    String.format("No find closed Appointments with department id: %s!", departmentId));

        return createClosedAppointmentDtoList(closedTimeList);
    }


    public List<ClosedAppointmentDto> getClosedAppointmentsByDepartmentFromDay(Long departmentId, LocalDate day)
            throws ApiNoSuchElementException {
        List<ClosedTime> closedTimeList =
                closedTimeRepository.findAllByDepartmentDetailsIdAndDateGreaterThanEqualOrderByDate(departmentId, day);

        if (closedTimeList.isEmpty())
            throw new ApiNoSuchElementException(
                    String.format("No find closed Appointments with department id: %s from %s!", departmentId, day));

        return createClosedAppointmentDtoList(closedTimeList);
    }


    public List<ClosedTime> getClosedEntitiesByDepartmentFromDay(Long departmentId, LocalDate day) {
        return closedTimeRepository.findAllByDepartmentDetailsIdAndDateGreaterThanEqualOrderByDate(departmentId, day);
    }

    public List<ClosedTime> getClosedEntitiesByDepartmentAndDay(Long departmentId, LocalDate day) {
        return closedTimeRepository.findAllByDepartmentDetailsIdAndDateOrderByClosedFrom(departmentId, day);
    }


    public String updateClosedAppointment(ClosedAppointmentDto closedAppointmentDto)
            throws ApiNoSuchElementException, ApiNoContentException, ApiIllegalMethodException, ApiIllegalArgumentException {
        Long id = closedAppointmentDto.getId();

        if (closedTimeRepository.findById(id).isEmpty())
            throw new ApiNoSuchElementException(String.format("Closed appointment not found with id: %s!", id));

        checkClosedAppointmentValues(closedAppointmentDto);

        ClosedTime closedTime = closedTimeRepository.findById(id).get();
        LocalDate closedTimeDate = closedTime.getDate();
        LocalDate appointmentDate = closedAppointmentDto.getDate();

        if (closedTimeDate.compareTo(appointmentDate) != 0)
            throw new ApiIllegalMethodException("Changing the date is not allowed!");

        closedTime.setClosedFrom(closedAppointmentDto.getClosedFrom());
        closedTime.setClosedTo(closedAppointmentDto.getClosedTo());

        closedTimeRepository.save(closedTime);

        return "Closed appointment updated!";
    }


    public String saveClosedAppointment(ClosedAppointmentDto closedAppointmentDto,
                                        SimpleDepartmentDetailsDto simpleDepartmentDetailsDto)
            throws ApiNoSuchElementException, ApiIllegalMethodException, ApiAlreadyExistException, ApiIllegalArgumentException {
        checkToSave(closedAppointmentDto, simpleDepartmentDetailsDto);

        closedTimeRepository.save(mapToEntity(closedAppointmentDto));

        return String.format("Closed appointment saved! | %s %s-%s",
                closedAppointmentDto.getDate(),
                closedAppointmentDto.getClosedFrom(),
                closedAppointmentDto.getClosedTo());
    }


    public String deleteClosedAppointment(ClosedAppointmentDto closedAppointmentDto)
            throws ApiNoSuchElementException, ApiIllegalMethodException {
        Long id = closedAppointmentDto.getId();
        LocalDate appointmentDate = closedAppointmentDto.getDate();
        LocalDate toDay = LocalDate.now();

        if (closedTimeRepository.findById(id).isEmpty())
            throw new ApiNoSuchElementException(String.format("Closed appointment not found with id: %s!", id));

        else if (appointmentDate.compareTo(toDay) <= 0)
            throw new ApiIllegalMethodException(String.format("Deletion is not allowed before the current (%s) date!", toDay));

        closedTimeRepository.deleteById(id);

        return String.format("Closed appointment deleted! | id: %s, date: %s %s-%s!",
                closedAppointmentDto.getId(),
                closedAppointmentDto.getDate(),
                closedAppointmentDto.getClosedFrom(),
                closedAppointmentDto.getClosedTo());
    }


    private void checkToSave(ClosedAppointmentDto closedAppointmentDto,
                             SimpleDepartmentDetailsDto simpleDepartmentDetailsDto)
            throws ApiNoSuchElementException, ApiIllegalMethodException, ApiAlreadyExistException, ApiIllegalArgumentException {
        LocalDate appointmentDate = closedAppointmentDto.getDate();
        LocalTime closedFrom = closedAppointmentDto.getClosedFrom();
        LocalTime closedTo = closedAppointmentDto.getClosedTo();
        Long departmentId = simpleDepartmentDetailsDto.getId();

        List<ClosedTime> byDate = closedTimeRepository.findAllByDepartmentDetailsIdAndDateOrderByClosedFrom(departmentId, appointmentDate);
        List<ClosedTime> byDateAndClosedFromTo = closedTimeRepository.findAllByDepartmentDetailsIdAndDateAndClosedFromAndClosedTo(departmentId, appointmentDate, closedFrom, closedTo);

        checkClosedAppointmentValues(closedAppointmentDto);

        if (!byDateAndClosedFromTo.isEmpty())
            throw new ApiAlreadyExistException("Closed appointment already exists!");

        openingClosingExceptions(closedAppointmentDto, simpleDepartmentDetailsDto);

        if (!byDate.isEmpty()) {
            for (var closedTime: byDate) {
                nullValuesExceptions(closedTime, closedAppointmentDto, simpleDepartmentDetailsDto);
                allReadyExistExceptions(closedTime, closedAppointmentDto);
            }
        }
    }


    private void checkClosedAppointmentValues(ClosedAppointmentDto closedAppointmentDto)
            throws ApiNoContentException, ApiIllegalMethodException, ApiIllegalArgumentException {
        LocalDate toDay = LocalDate.now();
        LocalDate appointmentDate = closedAppointmentDto.getDate();

        LocalTime closedFrom = closedAppointmentDto.getClosedFrom();
        LocalTime closedTo = closedAppointmentDto.getClosedTo();
        LocalTime standard = LocalTime.of(0,0);
        Duration durationOfClosedFrom = null;
        Duration durationOfClosedTo = null;

        if (closedFrom != null)
            durationOfClosedFrom = Duration.between(closedFrom, standard);

        else if (closedTo != null)
            durationOfClosedTo = Duration.between(closedTo, standard);

        if (appointmentDate == null)
            throw new ApiNoContentException("Missing date!");

        else if (appointmentDate.compareTo(toDay) <= 0)
            throw new ApiIllegalMethodException(String.format("The Operation is not allowed before the current (%s) date", toDay));

        if (closedFrom != null && closedTo != null && closedFrom.compareTo(closedTo) >= 0)
            throw new ApiIllegalArgumentException("The closing from time must be less than the closing to time!");

        if (durationOfClosedFrom != null && ((double) durationOfClosedFrom.toMinutes() / 30) % 1 != 0)
            throw new ApiIllegalArgumentException("The closed from time must be specified in 0.5 hours!");

        else if (durationOfClosedTo != null && ((double) durationOfClosedTo.toMinutes() / 30) % 1 != 0)
            throw new ApiIllegalArgumentException("The closed to time must be specified in 0.5 hours!");
    }


    private void openingClosingExceptions(ClosedAppointmentDto closedAppointmentDto,
                                          SimpleDepartmentDetailsDto simpleDepartmentDetailsDto)
            throws ApiIllegalArgumentException {
        LocalTime closedFrom = closedAppointmentDto.getClosedFrom();
        LocalTime closedTo = closedAppointmentDto.getClosedTo();
        LocalTime departmentOpening = simpleDepartmentDetailsDto.getOpening();
        LocalTime departmentClosing = simpleDepartmentDetailsDto.getClosing();

        if (closedFrom.compareTo(departmentOpening) < 0)
            throw new ApiIllegalArgumentException(
                    String.format("The 'closed-from' time (%s) lower, than the Department opening (%s) time!", closedFrom, departmentOpening));

        else if (closedTo.compareTo(departmentClosing) > 0)
            throw new ApiIllegalArgumentException(
                    String.format("The 'closed-to' time (%s) higher, than the Department closing (%s) time!", closedTo, departmentClosing));
    }


    private void nullValuesExceptions(ClosedTime closedTime,
                                      ClosedAppointmentDto closedAppointmentDto,
                                      SimpleDepartmentDetailsDto simpleDepartmentDetailsDto)
            throws ApiIllegalArgumentException {
        LocalTime savedClosedFrom = closedTime.getClosedFrom();
        LocalTime savedClosedTo = closedTime.getClosedTo();
        LocalTime closedFrom = closedAppointmentDto.getClosedFrom();
        LocalTime closedTo = closedAppointmentDto.getClosedTo();
        LocalTime departmentOpening = simpleDepartmentDetailsDto.getOpening();
        LocalTime departmentClosing = simpleDepartmentDetailsDto.getClosing();

        if (savedClosedFrom == null && savedClosedTo == null)
            throw new ApiIllegalArgumentException("Today is all day closing. Check the saved closing appointment!");

        else if (savedClosedFrom == null && closedFrom.compareTo(savedClosedTo) < 0)
            throw new ApiIllegalArgumentException(
                    String.format("Today form %s until %s is closing. Check the saved closing appointment!", departmentOpening, savedClosedTo));

        else {
            assert savedClosedFrom != null;
            if (closedTo.compareTo(savedClosedFrom) > 0 && savedClosedTo == null)
                throw new ApiIllegalArgumentException(
                        String.format("Today form %s until %s is closing. Check the saved closing appointment!",savedClosedFrom, departmentClosing));
        }

    }


    private void allReadyExistExceptions(ClosedTime closedTime,
                                         ClosedAppointmentDto closedAppointmentDto)
            throws ApiAlreadyExistException {
        LocalTime savedClosedFrom = closedTime.getClosedFrom();
        LocalTime savedClosedTo = closedTime.getClosedTo();
        LocalTime closedFrom = closedAppointmentDto.getClosedFrom();
        LocalTime closedTo = closedAppointmentDto.getClosedTo();

        if (closedFrom.compareTo(savedClosedFrom) > 0 && closedFrom.compareTo(savedClosedTo) < 0)
            throw new ApiAlreadyExistException(
                    String.format("The new 'closed-from' time is already exist in one closing appointment (%s - %s)!", savedClosedFrom, savedClosedTo));

        else if (closedTo.compareTo(savedClosedFrom) > 0 && closedTo.compareTo(savedClosedTo) < 0)
            throw new ApiAlreadyExistException(
                    String.format("The new 'closed-to' time is already exist in one closing appointment (%s - %s)!", savedClosedFrom, savedClosedTo));

        else if (closedFrom.compareTo(savedClosedFrom) < 0 && closedTo.compareTo(savedClosedTo) > 0)
            throw new ApiAlreadyExistException(
                    String.format("Between the 'closed-from' and 'closed-to' already exists a closing appointment (%s - %s)!", savedClosedFrom, savedClosedTo));


    }


    private List<ClosedAppointmentDto> createClosedAppointmentDtoList(List<ClosedTime> closedTimeList) {
        List<ClosedAppointmentDto> closedAppointmentDtoList = new ArrayList<>();

        for (var closedTime: closedTimeList) {
            closedAppointmentDtoList.add(mapToDto(closedTime));
        }

        return closedAppointmentDtoList;
    }

    private ClosedTime mapToEntity(ClosedAppointmentDto closedAppointmentDto) {
        return mapper.map(closedAppointmentDto, ClosedTime.class);
    }

    private ClosedAppointmentDto mapToDto(ClosedTime closedTime) {
        return mapper.map(closedTime, ClosedAppointmentDto.class);
    }
}
