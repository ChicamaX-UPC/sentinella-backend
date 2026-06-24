package com.chicamax.sentinella.payments.infrastructure.billing;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionPaymentFailedMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionPeriodSyncMessage;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class StripeBillingWebhookService {

    private static final Logger log = LoggerFactory.getLogger(StripeBillingWebhookService.class);

    private final BillingNotificationService billingNotificationService;
    private final RabbitTemplate rabbitTemplate;

    public StripeBillingWebhookService(
            BillingNotificationService billingNotificationService,
            RabbitTemplate rabbitTemplate
    ) {
        this.billingNotificationService = billingNotificationService;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void handleInvoicePaid(Event event, Invoice invoice) {
        if (invoice == null) {
            return;
        }
        String email = invoice.getCustomerEmail();
        if (email == null || email.isBlank()) {
            email = invoice.getCustomerName();
        }
        BigDecimal amount = invoice.getAmountPaid() != null
                ? BigDecimal.valueOf(invoice.getAmountPaid()).movePointLeft(2)
                : null;
        billingNotificationService.sendPaymentReceipt(
                email,
                invoice.getCustomerName(),
                amount,
                invoice.getCurrency(),
                invoice.getHostedInvoiceUrl()
        );
        publishPeriodSync(invoice.getSubscription(), email, invoice.getPeriodEnd(), true);
    }

    public void handleInvoicePaymentFailed(Event event, Invoice invoice) {
        if (invoice == null) {
            return;
        }
        String email = invoice.getCustomerEmail();
        String reason = invoice.getLastFinalizationError() != null
                ? invoice.getLastFinalizationError().getMessage()
                : "Pago rechazado";
        OffsetDateTime attemptedAt = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime nextRetry = attemptedAt.plusDays(2);

        billingNotificationService.sendPaymentFailed(email, invoice.getCustomerName(), reason, nextRetry);

        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.SUBSCRIPTION_PAYMENT_FAILED_ROUTING,
                new SubscriptionPaymentFailedMessage(
                        invoice.getSubscription(),
                        invoice.getId(),
                        email,
                        reason,
                        attemptedAt,
                        nextRetry
                )
        );
    }

    public void handleSubscriptionUpdated(Subscription subscription) {
        if (subscription == null) {
            return;
        }
        publishPeriodSync(
                subscription.getId(),
                null,
                subscription.getCurrentPeriodEnd(),
                false
        );
    }

    private void publishPeriodSync(String stripeSubscriptionId, String billingEmail, Long periodEndEpoch, boolean paymentRecovered) {
        if (stripeSubscriptionId == null || stripeSubscriptionId.isBlank() || periodEndEpoch == null) {
            return;
        }
        OffsetDateTime periodEnd = OffsetDateTime.ofInstant(Instant.ofEpochSecond(periodEndEpoch), ZoneOffset.UTC);
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.SUBSCRIPTION_PERIOD_SYNC_ROUTING,
                new SubscriptionPeriodSyncMessage(stripeSubscriptionId, billingEmail, periodEnd, paymentRecovered)
        );
    }
}
