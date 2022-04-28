package com.healingsys.repositories;

import com.healingsys.entities.ClosedTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ClosedTimeRepository extends JpaRepository<ClosedTime, Long> {

    List<ClosedTime> findAllByDepartmentId(Long departmentId);

    List<ClosedTime> findAllByDepartmentIdAndDateGreaterThanEqualOrderByDate(Long departmentId, LocalDate date);

    List<ClosedTime> findAllByDepartmentIdAndDateAndClosedFromAndClosedTo(Long departmentId, LocalDate date, LocalTime closedFrom, LocalTime closedTo);

    List<ClosedTime> findAllByDepartmentIdAndDateOrderByClosedFrom(Long departmentId, LocalDate date);
}
