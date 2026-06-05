package com.chicamax.sentinella.blockchain.infrastructure.persistence.jpa;

import com.chicamax.sentinella.blockchain.domain.model.aggregates.LedgerRecord;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRecordRepository extends JpaRepository<LedgerRecord, UUID> {
}
