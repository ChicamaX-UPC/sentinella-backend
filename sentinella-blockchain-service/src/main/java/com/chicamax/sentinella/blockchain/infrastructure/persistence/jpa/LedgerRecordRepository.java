package com.chicamax.sentinella.blockchain.infrastructure.persistence.jpa;

import com.chicamax.sentinella.blockchain.domain.model.aggregates.LedgerRecord;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerRecordRepository extends JpaRepository<LedgerRecord, UUID> {

    List<LedgerRecord> findByEntityIdOrderByRegisteredAtDesc(UUID entityId);

    @Query("""
            SELECT r FROM LedgerRecord r
            WHERE r.entityId = :alertId OR r.relatedEntityId = :alertId
            ORDER BY r.registeredAt DESC
            """)
    List<LedgerRecord> findByEntityIdOrRelatedEntityIdOrderByRegisteredAtDesc(@Param("alertId") UUID alertId);

    List<LedgerRecord> findByEntityTypeOrderByRegisteredAtDesc(String entityType);

    List<LedgerRecord> findByEntityIdInOrderByRegisteredAtDesc(Collection<UUID> entityIds);

    List<LedgerRecord> findByNodeIdInOrderByRegisteredAtDesc(Collection<UUID> nodeIds);

    List<LedgerRecord> findAllByOrderByRegisteredAtDesc();
}
