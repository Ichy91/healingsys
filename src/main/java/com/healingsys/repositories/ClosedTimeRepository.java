package com.healingsys.repositories;

import com.healingsys.entities.ClosedTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ClosedTimeRepository extends JpaRepository<ClosedTime, Long> {

    List<ClosedTime> findAllByDepartmentDetailsId(Long departmentId);

    List<ClosedTime> findAllByDepartmentDetailsIdAndDateGreaterThanEqualOrderByDate(Long departmentId, LocalDate date);

    List<ClosedTime> findAllByDepartmentDetailsIdAndDateAndClosedFromAndClosedTo(Long departmentId, LocalDate date, LocalTime closedFrom, LocalTime closedTo);

    List<ClosedTime> findAllByDepartmentDetailsIdAndDateOrderByClosedFrom(Long departmentId, LocalDate date);
}
