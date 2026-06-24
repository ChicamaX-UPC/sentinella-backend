package com.chicamax.sentinella.blockchain.infrastructure.persistence.jpa;

import com.chicamax.sentinella.blockchain.domain.model.aggregates.LedgerRecord;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRecordRepository extends JpaRepository<LedgerRecord, UUID> {

    List<LedgerRecord> findByEntityIdOrderByRegisteredAtDesc(UUID entityId);

    List<LedgerRecord> findByEntityTypeOrderByRegisteredAtDesc(String entityType);

    List<LedgerRecord> findByEntityIdInOrderByRegisteredAtDesc(Collection<UUID> entityIds);
}
