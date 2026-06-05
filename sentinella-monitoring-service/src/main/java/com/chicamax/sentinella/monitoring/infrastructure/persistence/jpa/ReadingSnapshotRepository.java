package com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa;

import com.chicamax.sentinella.monitoring.domain.model.entities.ReadingSnapshot;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingSnapshotRepository extends JpaRepository<ReadingSnapshot, UUID> {
}
