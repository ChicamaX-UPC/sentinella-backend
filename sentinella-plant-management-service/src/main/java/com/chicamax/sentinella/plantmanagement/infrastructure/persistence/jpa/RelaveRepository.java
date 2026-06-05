package com.chicamax.sentinella.plantmanagement.infrastructure.persistence.jpa;

import com.chicamax.sentinella.plantmanagement.domain.model.aggregates.Relave;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelaveRepository extends JpaRepository<Relave, UUID> {
}
