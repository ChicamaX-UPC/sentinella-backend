package com.chicamax.sentinella.payments.infrastructure.billing;

import com.chicamax.sentinella.shared.infrastructure.messaging.events.BillingDunningRetryMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.BillingRenewalReminderMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.BillingServiceSuspendedMessage;
import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BillingEmailHandlers {

    private static final Logger log = LoggerFactory.getLogger(BillingEmailHandlers.class);

    private final BillingNotificationService billingNotificationService;

    public BillingEmailHandlers(BillingNotificationService billingNotificationService) {
        this.billingNotificationService = billingNotificationService;
    }

    @RabbitListener(queues = "billing.renewal.reminder.queue")
    public void onRenewalReminder(BillingRenewalReminderMessage message) {
        billingNotificationService.sendRenewalReminder(
                message.billingEmail(),
                message.companyLabel(),
                message.chargeDate(),
                null,
                "usd"
        );
    }

    @RabbitListener(queues = "billing.service.suspended.queue")
    public void onServiceSuspended(BillingServiceSuspendedMessage message) {
        billingNotificationService.sendServiceSuspended(message.billingEmail(), message.companyLabel());
    }

    @RabbitListener(queues = "billing.dunning.retry.queue")
    public void onDunningRetry(BillingDunningRetryMessage message) {
        if (message.stripeInvoiceId() == null || message.stripeInvoiceId().isBlank()) {
            return;
        }
        try {
            Invoice invoice = Invoice.retrieve(message.stripeInvoiceId());
            invoice.pay();
            log.info("Reintento de cobro Stripe enviado para factura {}", message.stripeInvoiceId());
        } catch (StripeException ex) {
            log.warn("Reintento de cobro fallido para {}: {}", message.stripeInvoiceId(), ex.getMessage());
        }
    }
}
