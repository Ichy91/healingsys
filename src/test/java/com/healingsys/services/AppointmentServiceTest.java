package com.healingsys.services;


import com.healingsys.dto.appointment.AppointmentDto;
import com.healingsys.dto.appointment.SimpleAppointmentDto;
import com.healingsys.entities.Appointment;
import com.healingsys.entities.Department;
import com.healingsys.entities.User;
import com.healingsys.entities.enums.AppointmentStatus;
import com.healingsys.entities.enums.Role;
import com.healingsys.entities.enums.Status;
import com.healingsys.repositories.AppointmentRepository;
import com.healingsys.util.DataHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@PrepareForTest(AppointmentService.class)
class AppointmentServiceTest {
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private UserService userService;
    @Mock private DepartmentService departmentService;
    @Mock private ClosedTimeService closedTimeService;
    @Mock private DataHandler dataHandler;
    @Mock private ModelMapper mapper;

    @InjectMocks private AppointmentService underTest;

    private Long departmentId;
    private Long appointmentId;
    private UUID userId;
    private User user;
    private Department department;
    private Appointment appointment;
    private Appointment completeAppointment;
    private AppointmentDto appointmentDto;


    @BeforeEach
    void initTest() {
        departmentId = Math.abs(new Random().nextLong());
        userId = UUID.randomUUID();
    }

    @Nested
    class GettingFromDB {
        @Test
        void canGetAppointmentsByDepartmentFromDay() {
            //Arrange
            LocalDate day = LocalDate.of(2022, 2,22);

            //Act
            underTest.getAppointmentsByDepartmentFromDay(departmentId, day);

            //Assert
            verify(appointmentRepository).findAllByDepartmentIdAndDateGreaterThanEqualOrderByDate(departmentId, day);
        }

        @Test
        void canGetReservedAppointmentsByDepartmentIdAndUserId() {
            //Arrange
            AppointmentStatus status = AppointmentStatus.RESERVED;

            //Act
            underTest.getReservedAppointmentsByDepartmentIdAndUserId(departmentId, userId);

            //Assert
            verify(appointmentRepository).findAllByDepartmentIdAndUserIdAndStatus(departmentId, userId, status);
        }
    }

    @Nested
    class Positive {
        @BeforeEach
        void initPositiveTest() {
            appointmentId = Math.abs(new Random().nextLong());

            user = new User(
                    userId,
                    Timestamp.valueOf("2022-02-16 08:31:41.223187"),
                    null,
                    "Istvan Farago",
                    null,
                    null,
                    "Y3472944G",
                    "+34603233448",
                    "faragoistvan91@gmail.com",
                    Status.ACTIVE,
                    Role.PATIENT,
                    null,
                    null,
                    null);

            department = new Department(
                    departmentId,
                    Timestamp.valueOf("2022-02-10 14:24:05.812337"),
                    null,
                    "Registration",
                    LocalTime.of(8,0),
                    LocalTime.of(17, 0),
                    5,
                    1,
                    2,
                    null,
                    Status.ACTIVE);

            appointmentDto = new AppointmentDto(
                    appointmentId,
                    LocalDate.of(2022,2,22),
                    LocalTime.of(10,0),
                    AppointmentStatus.RESERVED);

            appointment = new Appointment(
                    appointmentId,
                    null,
                    null,
                    appointmentDto.getDate(),
                    appointmentDto.getHour(),
                    appointmentDto.getStatus());

            completeAppointment = new Appointment(
                    appointmentId,
                    user,
                    department,
                    appointmentDto.getDate(),
                    appointmentDto.getHour(),
                    appointmentDto.getStatus());
        }

        @Test
        void getUserAppointmentDto_foundAppointment_returnAppointmentDto() {
            //Arrange
            appointment.setId(null);

            SimpleAppointmentDto simpleAppointmentDto = new SimpleAppointmentDto(
                    appointmentDto.getDate(),
                    appointmentDto.getHour(),
                    appointmentDto.getStatus());

            //When
            when(mapper.map(simpleAppointmentDto, Appointment.class)).thenReturn(appointment);
            when(userService.getById(userId)).thenReturn(user);
            when(departmentService.getEntityById(departmentId)).thenReturn(department);
            when(appointmentRepository.findAllByDepartmentIdAndUserIdAndStatusAndDateAndHour(departmentId, userId, simpleAppointmentDto.getStatus(), simpleAppointmentDto.getDate(), simpleAppointmentDto.getHour())).thenReturn(List.of(completeAppointment));
            when(mapper.map(completeAppointment, AppointmentDto.class)).thenReturn(appointmentDto);

            //Act
            AppointmentDto result = underTest.getUserAppointmentDto(departmentId, userId, simpleAppointmentDto);

            //Assert
            assertEquals(appointmentDto, result);
        }

        @Test
        void appointmentReservation_savingNewReservedAppointment_returnReservationData() {
            //Arrange
            String expectation = String.format(
                    "Appointment reserved!\n" +
                    "Patient: %s\n" +
                    "Department: %s\n" +
                    "Date: %s\n" +
                    "Hour: %s,",
                    user.getName(),
                    department.getName(),
                    appointmentDto.getDate(),
                    appointmentDto.getHour());

            //When
            when(mapper.map(appointmentDto, Appointment.class)).thenReturn(appointment);
            when(userService.getById(userId)).thenReturn(user);
            when(departmentService.getEntityById(departmentId)).thenReturn(department);
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(completeAppointment));

            //Act
            String result = underTest.appointmentReservation(departmentId, userId, appointmentDto);

            //Assert
            assertEquals(expectation, result);
        }

        @Test
        void appointmentReservation_canceledAppointmentToReserved_returnReservationData() {
            //Arrange
            appointmentDto.setStatus(AppointmentStatus.CANCELED);

            String expectation = String.format(
                    "Appointment reserved!\n" +
                            "Patient: %s\n" +
                            "Department: %s\n" +
                            "Date: %s\n" +
                            "Hour: %s,",
                    user.getName(),
                    department.getName(),
                    appointmentDto.getDate(),
                    appointmentDto.getHour());

            //When
            when(mapper.map(appointmentDto, Appointment.class)).thenReturn(appointment);
            when(userService.getById(userId)).thenReturn(user);
            when(departmentService.getEntityById(departmentId)).thenReturn(department);
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(completeAppointment));

            //Act
            String result = underTest.appointmentReservation(departmentId, userId, appointmentDto);

            //Assert
            assertEquals(expectation, result);
        }

        @Test
        void appointmentCanceling_reservedAppointmentToCanceled_returnUpdatedData() {
            //Arrange
            appointmentDto.setStatus(AppointmentStatus.CANCELED);

            String expectation = String.format(
                    "The appointment updated!\n" +
                        "User: %s\n" +
                        "Department: %s\n" +
                        "Date: %s\n" +
                        "Hour: %s\n" +
                        "Status: %s",
                    user.getName(),
                    department.getName(),
                    appointmentDto.getDate(),
                    appointmentDto.getHour(),
                    AppointmentStatus.CANCELED);

            //When
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(completeAppointment));
            when(appointmentRepository.getById(appointmentId)).thenReturn(completeAppointment);

            //Act
            String result = underTest.appointmentCanceling(appointmentDto);

            //Assert
            assertEquals(expectation, result);
        }

        @Test
        void updateAppointmentHandler_newAppointmentReservation_returnReservedResponse() {
            //Arrange
            appointmentDto.setId(null);
            appointment.setId(null);

            ResponseEntity<String> expectation = new ResponseEntity<>(
                    String.format(
                        "Appointment reserved!\n" +
                            "Patient: %s\n" +
                            "Department: %s\n" +
                            "Date: %s\n" +
                            "Hour: %s,",
                        user.getName(),
                        department.getName(),
                        appointmentDto.getDate(),
                        appointmentDto.getHour()),
                    HttpStatus.CREATED);

            //When
            when(mapper.map(appointmentDto, Appointment.class)).thenReturn(appointment);
            when(userService.getById(userId)).thenReturn(user);
            when(departmentService.getEntityById(departmentId)).thenReturn(department);

            //Act
            ResponseEntity<String> result = underTest.updateAppointmentHandler(departmentId, userId, appointmentDto);

            //Assert
            assertEquals(expectation, result);
        }

        @Test
        void updateAppointmentHandler_reservation_returnReservedResponse() {
            //Arrange
            ResponseEntity<String> expectation = new ResponseEntity<>(
                    String.format(
                            "Appointment reserved!\n" +
                                    "Patient: %s\n" +
                                    "Department: %s\n" +
                                    "Date: %s\n" +
                                    "Hour: %s,",
                            user.getName(),
                            department.getName(),
                            appointmentDto.getDate(),
                            appointmentDto.getHour()),
                    HttpStatus.CREATED);

            //When
            when(mapper.map(appointmentDto, Appointment.class)).thenReturn(appointment);
            when(userService.getById(userId)).thenReturn(user);
            when(departmentService.getEntityById(departmentId)).thenReturn(department);
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(completeAppointment));

            //Act
            ResponseEntity<String> result = underTest.updateAppointmentHandler(departmentId, userId, appointmentDto);

            //Assert
            assertEquals(expectation, result);
        }

        @Test
        void updateAppointmentHandler_canceling_returnCanceledResponse() {
            //Arrange
            appointmentDto.setStatus(AppointmentStatus.CANCELED);
            appointment.setStatus(AppointmentStatus.CANCELED);

            ResponseEntity<String> expectation = new ResponseEntity<>(
                    String.format(
                            "The appointment updated!\n" +
                                    "User: %s\n" +
                                    "Department: %s\n" +
                                    "Date: %s\n" +
                                    "Hour: %s\n" +
                                    "Status: %s",
                            user.getName(),
                            department.getName(),
                            appointmentDto.getDate(),
                            appointmentDto.getHour(),
                            AppointmentStatus.CANCELED),
                    HttpStatus.ACCEPTED);

            //When
            when(mapper.map(appointmentDto, Appointment.class)).thenReturn(appointment);
            when(userService.getById(userId)).thenReturn(user);
            when(departmentService.getEntityById(departmentId)).thenReturn(department);
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(completeAppointment));

            //Act
            ResponseEntity<String> result = underTest.updateAppointmentHandler(departmentId, userId, appointmentDto);

            //Assert
            assertEquals(expectation, result);
        }

        @Test
        void updateAppointmentHandler_missing_returnMissedResponse() {
            //Arrange
            appointmentDto.setStatus(AppointmentStatus.MISSED);
            appointment.setStatus(AppointmentStatus.MISSED);

            ResponseEntity<String> expectation = new ResponseEntity<>(
                    String.format(
                            "The appointment updated!\n" +
                                    "User: %s\n" +
                                    "Department: %s\n" +
                                    "Date: %s\n" +
                                    "Hour: %s\n" +
                                    "Status: %s",
                            user.getName(),
                            department.getName(),
                            appointmentDto.getDate(),
                            appointmentDto.getHour(),
                            AppointmentStatus.MISSED),
                    HttpStatus.ACCEPTED);

            //When
            when(mapper.map(appointmentDto, Appointment.class)).thenReturn(appointment);
            when(userService.getById(userId)).thenReturn(user);
            when(departmentService.getEntityById(departmentId)).thenReturn(department);
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(completeAppointment));

            //Act
            ResponseEntity<String> result = underTest.updateAppointmentHandler(departmentId, userId, appointmentDto);

            //Assert
            assertEquals(expectation, result);
        }

        @Test
        void updateAppointmentHandler_completing_returnCompletedResponse() {
            //Arrange
            appointmentDto.setStatus(AppointmentStatus.COMPLETED);
            appointment.setStatus(AppointmentStatus.COMPLETED);

            ResponseEntity<String> expectation = new ResponseEntity<>(
                    String.format(
                            "The appointment updated!\n" +
                                    "User: %s\n" +
                                    "Department: %s\n" +
                                    "Date: %s\n" +
                                    "Hour: %s\n" +
                                    "Status: %s",
                            user.getName(),
                            department.getName(),
                            appointmentDto.getDate(),
                            appointmentDto.getHour(),
                            AppointmentStatus.COMPLETED),
                    HttpStatus.ACCEPTED);

            //When
            when(mapper.map(appointmentDto, Appointment.class)).thenReturn(appointment);
            when(userService.getById(userId)).thenReturn(user);
            when(departmentService.getEntityById(departmentId)).thenReturn(department);
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(completeAppointment));

            //Act
            ResponseEntity<String> result = underTest.updateAppointmentHandler(departmentId, userId, appointmentDto);

            //Assert
            assertEquals(expectation, result);
        }

        @Test
        void update_updatingAppointment_returnUpdatedString() {
            //Arrange
            String expectation = "The appointment updated!";

            //When
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(completeAppointment));

            //Act
            String result = underTest.update(completeAppointment);

            //Assert
            assertEquals(expectation, result);
        }
    }

    @Nested
    class Negative {
        @BeforeEach
        void initNegativeTest() {

        }

    }

}
