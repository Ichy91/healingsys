package com.healingsys.repositories;

import com.healingsys.entities.DepartmentDetails;
import com.healingsys.entities.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentDetailsRepository extends JpaRepository<DepartmentDetails, Long> {

    public List<DepartmentDetails> findAllByStatus(Status status);
}
