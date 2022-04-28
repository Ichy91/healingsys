package com.healingsys.repositories;

import com.healingsys.entities.Department;
import com.healingsys.entities.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findAllByStatus(Status status);

    List<Department> findAllByName(String name);
}
