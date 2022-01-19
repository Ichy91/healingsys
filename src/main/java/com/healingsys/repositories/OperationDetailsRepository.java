package com.healingsys.repositories;

import com.healingsys.entities.OperationDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationDetailsRepository extends JpaRepository<OperationDetails, Long> {
}
