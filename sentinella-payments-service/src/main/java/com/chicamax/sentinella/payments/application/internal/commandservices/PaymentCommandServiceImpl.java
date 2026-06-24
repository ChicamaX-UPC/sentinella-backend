package com.chicamax.sentinella.payments.application.internal.commandservices;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.payments.domain.model.aggregates.Payment;
import com.chicamax.sentinella.payments.domain.model.aggregates.Plan;
import com.chicamax.sentinella.payments.domain.model.commands.CreateCheckoutCommand;
import com.chicamax.sentinella.payments.domain.model.events.PaymentCompletedEvent;
import com.chicamax.sentinella.payments.domain.model.valueobjects.CheckoutResult;
import com.chicamax.sentinella.payments.domain.services.PaymentCommandService;
import com.chicamax.sentinella.payments.infrastructure.messaging.PaymentCompletedRabbitPublisher;
import com.chicamax.sentinella.payments.infrastructure.persistence.jpa.PaymentRepository;
import com.chicamax.sentinella.payments.infrastructure.persistence.jpa.PlanRepository;
import com.chicamax.sentinella.payments.infrastructure.stripe.StripeCheckoutService;
import com.chicamax.sentinella.payments.infrastructure.stripe.StripeProperties;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionCancelledMessage;
import com.stripe.model.checkout.Session;
import java.util.List;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentCommandServiceImpl implements PaymentCommandService {

    private final PlanRepository planRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentCompletedRabbitPublisher paymentCompletedRabbitPublisher;
    private final StripeCheckoutService stripeCheckoutService;
    private final StripeProperties stripeProperties;
    private final RabbitTemplate rabbitTemplate;

    public PaymentCommandServiceImpl(
            PlanRepository planRepository,
            PaymentRepository paymentRepository,
            PaymentCompletedRabbitPublisher paymentCompletedRabbitPublisher,
            StripeCheckoutService stripeCheckoutService,
            StripeProperties stripeProperties,
            RabbitTemplate rabbitTemplate
    ) {
        this.planRepository = planRepository;
        this.paymentRepository = paymentRepository;
        this.paymentCompletedRabbitPublisher = paymentCompletedRabbitPublisher;
        this.stripeCheckoutService = stripeCheckoutService;
        this.stripeProperties = stripeProperties;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plan> listPlans() {
        return planRepository.findByActiveTrueOrderByPriceCentsAsc();
    }

    @Override
    @Transactional
    public CheckoutResult createCheckout(CreateCheckoutCommand command) {
        Plan plan = planRepository.findById(command.planId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan no encontrado"));
        if (!plan.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan no disponible");
        }
        if (command.customerEmail() == null || command.customerEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo del cliente requerido");
        }

        Payment payment = paymentRepository.save(Payment.createPending(
                UUID.randomUUID(),
                command.userId(),
                plan.getId(),
                plan.getPriceCents() + plan.getSetupPriceCents(),
                plan.getCurrency()
        ));

        if (!stripeProperties.isConfigured()) {
            return new CheckoutResult(payment, null);
        }

        Session session = stripeCheckoutService.createSubscriptionCheckout(payment, plan, command.customerEmail());
        payment.attachStripeSessionId(session.getId());
        paymentRepository.save(payment);
        return new CheckoutResult(payment, session.getUrl());
    }

    @Override
    @Transactional
    public Payment confirmPayment(UUID paymentId, String stripeSubscriptionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));
        if (payment.isCompleted()) {
            return payment;
        }
        payment.complete();
        Payment saved = paymentRepository.save(payment);
        paymentCompletedRabbitPublisher.publish(new PaymentCompletedEvent(
                saved.getId(),
                saved.getUserId(),
                saved.getPlanId(),
                java.math.BigDecimal.valueOf(saved.getAmountCents()).movePointLeft(2),
                saved.getCurrency(),
                stripeSubscriptionId
        ));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public String createPortalSession(UUID userId) {
        return stripeCheckoutService.createPortalSession(userId);
    }

    @Override
    public void cancelSubscriptionByStripeId(String stripeSubscriptionId) {
        if (stripeSubscriptionId == null || stripeSubscriptionId.isBlank()) {
            return;
        }
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.SUBSCRIPTION_STRIPE_CANCELLED_ROUTING,
                new SubscriptionCancelledMessage(null, null, stripeSubscriptionId)
        );
    }
}
