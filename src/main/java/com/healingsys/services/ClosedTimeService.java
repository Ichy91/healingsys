package com.healingsys.services;

import com.healingsys.dto.ClosedAppointmentDto;
import com.healingsys.dto.SimpleDepartmentDetailsDto;
import com.healingsys.entities.ClosedTime;
import com.healingsys.exception.ApiRequestException;
import com.healingsys.repositories.ClosedTimeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

    public ClosedAppointmentDto getById(Long id) throws ApiRequestException {
        Optional<ClosedTime> closedTime = closedTimeRepository.findById(id);

        if (closedTime.isEmpty())
            throw new ApiRequestException(
                    String.format("No find closed appointment with %s id", id),
                    HttpStatus.NOT_FOUND);

        return mapToDto(closedTime.get());
    }

    public List<ClosedAppointmentDto> getClosedAppointmentByDepartment(Long departmentId) throws ApiRequestException {
        List<ClosedTime> closedTimeList = closedTimeRepository.findAllByDepartmentDetailsId(departmentId);

        if (closedTimeList.isEmpty())
            throw new ApiRequestException("No find closed Appointments!", HttpStatus.NOT_FOUND);

        return createClosedAppointmentDtoList(closedTimeList);
    }

    public List<ClosedAppointmentDto> getClosedAppointmentsByDepartmentFromDay(Long departmentId, LocalDate day) throws ApiRequestException {
        List<ClosedTime> closedTimeList =
                closedTimeRepository.findAllByDepartmentDetailsIdAndDateGreaterThanEqualOrderByDate(departmentId, day);

        if (closedTimeList.isEmpty())
            throw new ApiRequestException("No find closed Appointments!", HttpStatus.NOT_FOUND);

        return createClosedAppointmentDtoList(closedTimeList);
    }

    public String updateClosedAppointment(ClosedAppointmentDto closedAppointmentDto) throws ApiRequestException {
        Long id = closedAppointmentDto.getId();
        LocalDate appointmentDate = closedAppointmentDto.getDate();
        LocalDate toDay = LocalDate.now();

        if (closedTimeRepository.findById(id).isEmpty())
            throw new ApiRequestException(
                    String.format("Closed appointment not found with id: %s", id),
                    HttpStatus.NOT_FOUND);

        else if (appointmentDate.compareTo(toDay) <= 0)
            throw new ApiRequestException(
                    String.format("Updating is not allowed before the current (%s) date", toDay),
                    HttpStatus.UNAUTHORIZED);

        ClosedTime closedTime = closedTimeRepository.findById(id).get();

        closedTime.setDate(closedAppointmentDto.getDate());
        closedTime.setClosedFrom(closedAppointmentDto.getClosedForm());
        closedTime.setClosedTo(closedAppointmentDto.getClosedTo());

        return "Closed appointment updated!";
    }

    public String saveClosedAppointment(ClosedAppointmentDto closedAppointmentDto,
                                        SimpleDepartmentDetailsDto simpleDepartmentDetailsDto) throws ApiRequestException {
        checkToSave(closedAppointmentDto, simpleDepartmentDetailsDto);

        closedTimeRepository.save(mapToEntity(closedAppointmentDto));

        return String.format("Closed appointment saved! | %s %s-%s",
                closedAppointmentDto.getDate(),
                closedAppointmentDto.getClosedForm(),
                closedAppointmentDto.getClosedTo());
    }

    public String deleteClosedAppointment(ClosedAppointmentDto closedAppointmentDto) throws ApiRequestException {
        Long id = closedAppointmentDto.getId();
        LocalDate appointmentDate = closedAppointmentDto.getDate();
        LocalDate toDay = LocalDate.now();

        if (closedTimeRepository.findById(id).isEmpty())
            throw new ApiRequestException(
                    String.format("Closed appointment not found with id: %s", id),
                    HttpStatus.NOT_FOUND);

        else if (appointmentDate.compareTo(toDay) <= 0)
            throw new ApiRequestException(
                    String.format("Deletion is not allowed before the current (%s) date", toDay),
                    HttpStatus.UNAUTHORIZED);

        closedTimeRepository.deleteById(id);

        return String.format("Closed appointment deleted! | id: %s, date: %s %s-%s",
                closedAppointmentDto.getId(),
                closedAppointmentDto.getDate(),
                closedAppointmentDto.getClosedForm(),
                closedAppointmentDto.getClosedTo());
    }

    private void checkToSave(ClosedAppointmentDto closedAppointmentDto,
                             SimpleDepartmentDetailsDto simpleDepartmentDetailsDto) throws ApiRequestException {
        LocalDate appointmentDate = closedAppointmentDto.getDate();
        LocalDate toDay = LocalDate.now();
        LocalTime closedFrom = closedAppointmentDto.getClosedForm();
        LocalTime closedTo = closedAppointmentDto.getClosedTo();
        Long departmentId = simpleDepartmentDetailsDto.getId();

        List<ClosedTime> byDate = closedTimeRepository.findAllByDepartmentDetailsIdAndDate(departmentId, appointmentDate);
        List<ClosedTime> byDateAndClosedFromTo = closedTimeRepository.findAllByDepartmentDetailsIdAndDateAndClosedFromAndClosedTo(departmentId, appointmentDate, closedFrom, closedTo);

        if (appointmentDate.compareTo(toDay) <= 0)
            throw new ApiRequestException(
                    String.format("Saving is not allowed before the current (%s) date", toDay),
                    HttpStatus.UNAUTHORIZED);

        else if (!byDateAndClosedFromTo.isEmpty())
            throw new ApiRequestException(
                    "Closed appointment already exists!",
                    HttpStatus.BAD_REQUEST);

        openingClosingExceptions(closedAppointmentDto, simpleDepartmentDetailsDto);

        if (!byDate.isEmpty()) {
            for (var closedTime: byDate) {
                nullValuesExceptions(closedTime, closedAppointmentDto, simpleDepartmentDetailsDto);
                allReadyExistExceptions(closedTime, closedAppointmentDto);
            }
        }
    }

    private void openingClosingExceptions(ClosedAppointmentDto closedAppointmentDto,
                                          SimpleDepartmentDetailsDto simpleDepartmentDetailsDto) throws ApiRequestException {
        LocalTime closedFrom = closedAppointmentDto.getClosedForm();
        LocalTime closedTo = closedAppointmentDto.getClosedTo();
        LocalTime departmentOpening = simpleDepartmentDetailsDto.getOpening();
        LocalTime departmentClosing = simpleDepartmentDetailsDto.getClosing();

        if (closedFrom.compareTo(departmentOpening) < 0)
            throw new ApiRequestException(
                    String.format("The 'closed-from' time (%s) lower, than the Department opening (%s) time!", closedFrom, departmentOpening),
                    HttpStatus.BAD_REQUEST);

        else if (closedTo.compareTo(departmentClosing) > 0)
            throw new ApiRequestException(
                    String.format("The 'closed-to' time (%s) higher, than the Department closing (%s) time!", closedTo, departmentClosing),
                    HttpStatus.BAD_REQUEST);
    }

    private void nullValuesExceptions(ClosedTime closedTime,
                                      ClosedAppointmentDto closedAppointmentDto,
                                      SimpleDepartmentDetailsDto simpleDepartmentDetailsDto) throws ApiRequestException {
        LocalTime savedClosedFrom = closedTime.getClosedFrom();
        LocalTime savedClosedTo = closedTime.getClosedTo();
        LocalTime closedFrom = closedAppointmentDto.getClosedForm();
        LocalTime closedTo = closedAppointmentDto.getClosedTo();
        LocalTime departmentOpening = simpleDepartmentDetailsDto.getOpening();
        LocalTime departmentClosing = simpleDepartmentDetailsDto.getClosing();

        if (savedClosedFrom == null && savedClosedTo == null)
            throw new ApiRequestException(
                    "Today is all day closing. Check the saved closing appointment!",
                    HttpStatus.BAD_REQUEST);

        else if (savedClosedFrom == null && closedFrom.compareTo(savedClosedTo) < 0)
            throw new ApiRequestException(
                    String.format("Today form %s until %s is closing. Check the saved closing appointment!", departmentOpening, savedClosedTo),
                    HttpStatus.BAD_REQUEST);

        else {
            assert savedClosedFrom != null;
            if (closedTo.compareTo(savedClosedFrom) > 0 && savedClosedTo == null)
                throw new ApiRequestException(
                        String.format("Today form %s until %s is closing. Check the saved closing appointment!",savedClosedFrom, departmentClosing),
                        HttpStatus.BAD_REQUEST);
        }

    }

    private void allReadyExistExceptions(ClosedTime closedTime,
                                         ClosedAppointmentDto closedAppointmentDto) throws ApiRequestException{
        LocalTime savedClosedFrom = closedTime.getClosedFrom();
        LocalTime savedClosedTo = closedTime.getClosedTo();
        LocalTime closedFrom = closedAppointmentDto.getClosedForm();
        LocalTime closedTo = closedAppointmentDto.getClosedTo();

        if (closedFrom.compareTo(savedClosedFrom) > 0 && closedFrom.compareTo(savedClosedTo) < 0)
            throw new ApiRequestException(
                    String.format("The new 'closed-from' time is already exist in one closing appointment (%s - %s)!", savedClosedFrom, savedClosedTo),
                    HttpStatus.BAD_REQUEST);

        else if (closedTo.compareTo(savedClosedFrom) > 0 && closedTo.compareTo(savedClosedTo) < 0)
            throw new ApiRequestException(
                    String.format("The new 'closed-to' time is already exist in one closing appointment (%s - %s)!", savedClosedFrom, savedClosedTo),
                    HttpStatus.BAD_REQUEST);

        else if (closedFrom.compareTo(savedClosedFrom) < 0 && closedTo.compareTo(savedClosedTo) > 0)
            throw new ApiRequestException(
                    String.format("Between the 'closed-from' and 'closed-to' already exists a closing appointment (%s - %s)!", savedClosedFrom, savedClosedTo),
                    HttpStatus.BAD_REQUEST);


    }

    private List<ClosedAppointmentDto> createClosedAppointmentDtoList(List<ClosedTime> closedTimeList) throws ApiRequestException {
        if (closedTimeList.isEmpty())
            throw new ApiRequestException("The list is empty!", HttpStatus.NOT_FOUND);

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
