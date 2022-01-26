package com.healingsys.repositories;

import com.healingsys.entities.ClosedTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClosedTimeRepository extends JpaRepository<ClosedTime, Long> {
}
