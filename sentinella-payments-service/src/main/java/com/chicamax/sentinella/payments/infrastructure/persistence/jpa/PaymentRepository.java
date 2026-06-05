package com.chicamax.sentinella.payments.infrastructure.persistence.jpa;

import com.chicamax.sentinella.payments.domain.model.aggregates.Payment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
