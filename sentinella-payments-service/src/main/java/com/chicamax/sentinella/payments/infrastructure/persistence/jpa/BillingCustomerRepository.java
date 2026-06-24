package com.chicamax.sentinella.payments.infrastructure.persistence.jpa;

import com.chicamax.sentinella.payments.domain.model.aggregates.BillingCustomer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingCustomerRepository extends JpaRepository<BillingCustomer, UUID> {
    Optional<BillingCustomer> findByUserId(UUID userId);
}
