package com.chicamax.sentinella.plantmanagement.infrastructure.persistence.jpa;

import com.chicamax.sentinella.plantmanagement.domain.model.aggregates.Operator;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperatorRepository extends JpaRepository<Operator, UUID> {
    boolean existsByEmail(String email);
}
