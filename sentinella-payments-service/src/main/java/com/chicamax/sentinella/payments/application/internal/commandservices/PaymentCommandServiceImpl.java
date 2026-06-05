package com.chicamax.sentinella.payments.application.internal.commandservices;

import com.chicamax.sentinella.payments.domain.model.aggregates.Payment;
import com.chicamax.sentinella.payments.domain.model.aggregates.Plan;
import com.chicamax.sentinella.payments.domain.model.commands.CreateCheckoutCommand;
import com.chicamax.sentinella.payments.domain.model.events.PaymentCompletedEvent;
import com.chicamax.sentinella.payments.domain.services.PaymentCommandService;
import com.chicamax.sentinella.payments.infrastructure.messaging.PaymentCompletedRabbitPublisher;
import com.chicamax.sentinella.payments.infrastructure.persistence.jpa.PaymentRepository;
import com.chicamax.sentinella.payments.infrastructure.persistence.jpa.PlanRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentCommandServiceImpl implements PaymentCommandService {

    private final PlanRepository planRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentCompletedRabbitPublisher paymentCompletedRabbitPublisher;

    public PaymentCommandServiceImpl(
            PlanRepository planRepository,
            PaymentRepository paymentRepository,
            PaymentCompletedRabbitPublisher paymentCompletedRabbitPublisher
    ) {
        this.planRepository = planRepository;
        this.paymentRepository = paymentRepository;
        this.paymentCompletedRabbitPublisher = paymentCompletedRabbitPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plan> listPlans() {
        return planRepository.findAll();
    }

    @Override
    @Transactional
    public Payment createCheckout(CreateCheckoutCommand command) {
        Plan plan = planRepository.findById(command.planId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan no encontrado"));
        return paymentRepository.save(Payment.createPending(
                UUID.randomUUID(),
                command.userId(),
                plan.getId(),
                plan.getPriceCents(),
                plan.getCurrency()
        ));
    }

    @Override
    @Transactional
    public Payment confirmPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));
        payment.complete();
        Payment saved = paymentRepository.save(payment);
        paymentCompletedRabbitPublisher.publish(new PaymentCompletedEvent(
                saved.getId(),
                saved.getUserId(),
                saved.getPlanId(),
                java.math.BigDecimal.valueOf(saved.getAmountCents()).movePointLeft(2),
                saved.getCurrency()
        ));
        return saved;
    }
}
