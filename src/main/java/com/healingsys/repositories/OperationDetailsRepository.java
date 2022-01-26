package com.healingsys.repositories;

import com.healingsys.entities.OperationDetails;
import com.healingsys.entities.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationDetailsRepository extends JpaRepository<OperationDetails, Long> {

    public List<OperationDetails> findAllByStatus(Status status);
}
