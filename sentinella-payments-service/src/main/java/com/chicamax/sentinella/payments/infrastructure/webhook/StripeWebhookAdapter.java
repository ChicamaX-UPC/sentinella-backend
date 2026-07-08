package com.chicamax.sentinella.payments.infrastructure.webhook;

import com.chicamax.sentinella.payments.domain.services.PaymentCommandService;
import com.chicamax.sentinella.payments.infrastructure.billing.StripeBillingWebhookService;
import com.chicamax.sentinella.payments.infrastructure.stripe.StripeProperties;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payments/webhooks/stripe")
public class StripeWebhookAdapter {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookAdapter.class);

    private final PaymentCommandService paymentCommandService;
    private final StripeProperties stripeProperties;
    private final StripeBillingWebhookService stripeBillingWebhookService;
    private final boolean demoConfirmEnabled;

    public StripeWebhookAdapter(
            PaymentCommandService paymentCommandService,
            StripeProperties stripeProperties,
            StripeBillingWebhookService stripeBillingWebhookService,
            @Value("${stripe.demo-confirm-enabled:false}") boolean demoConfirmEnabled
    ) {
        this.paymentCommandService = paymentCommandService;
        this.stripeProperties = stripeProperties;
        this.stripeBillingWebhookService = stripeBillingWebhookService;
        this.demoConfirmEnabled = demoConfirmEnabled;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> receive(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature
    ) {
        if (!stripeProperties.isConfigured()) {
            return ResponseEntity.ok(Map.of("status", "ignored", "mode", "stub"));
        }
        if (signature == null || stripeProperties.webhookSecret() == null || stripeProperties.webhookSecret().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("status", "missing_signature"));
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, stripeProperties.webhookSecret());
        } catch (SignatureVerificationException ex) {
            log.warn("Firma de webhook Stripe inválida");
            return ResponseEntity.badRequest().body(Map.of("status", "invalid_signature"));
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutCompleted(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            case "customer.subscription.updated" -> stripeBillingWebhookService.handleSubscriptionUpdated(
                    deserialize(event, com.stripe.model.Subscription.class)
            );
            case "invoice.paid" -> stripeBillingWebhookService.handleInvoicePaid(event, deserialize(event, Invoice.class));
            case "invoice.payment_failed" -> stripeBillingWebhookService.handleInvoicePaymentFailed(
                    event, deserialize(event, Invoice.class)
            );
            default -> log.debug("Evento Stripe ignorado: {}", event.getType());
        }

        return ResponseEntity.ok(Map.of("status", "received"));
    }

    @PostMapping("/confirm/{paymentId}")
    public ResponseEntity<Map<String, Object>> confirmDemo(@PathVariable UUID paymentId) {
        // Gateado unicamente por STRIPE_DEMO_CONFIRM_ENABLED (false por defecto, solo se activa
        // explicitamente en dev local). El chequeo de IP localhost se quito: en Docker la IP que
        // ve este contenedor es la del puente/gateway, nunca 127.0.0.1, asi que ese chequeo
        // bloqueaba el endpoint incluso en el propio entorno de desarrollo para el que existe.
        if (!demoConfirmEnabled) {
            return ResponseEntity.status(404).body(Map.of("status", "disabled"));
        }
        var payment = paymentCommandService.confirmPayment(paymentId, null);
        return ResponseEntity.ok(Map.of(
                "paymentId", payment.getId(),
                "status", payment.getStatus().name()
        ));
    }

    private void handleCheckoutCompleted(Event event) {
        Session session = deserialize(event, Session.class);
        if (session == null) {
            return;
        }
        String paymentIdRaw = session.getClientReferenceId();
        if (paymentIdRaw == null && session.getMetadata() != null) {
            paymentIdRaw = session.getMetadata().get("paymentId");
        }
        if (paymentIdRaw == null) {
            log.warn("checkout.session.completed sin paymentId");
            return;
        }
        paymentCommandService.confirmPayment(UUID.fromString(paymentIdRaw), session.getSubscription());
    }

    private void handleSubscriptionDeleted(Event event) {
        com.stripe.model.Subscription subscription = deserialize(event, com.stripe.model.Subscription.class);
        if (subscription == null) {
            return;
        }
        paymentCommandService.cancelSubscriptionByStripeId(subscription.getId());
    }

    private <T extends StripeObject> T deserialize(Event event, Class<T> type) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent() && type.isInstance(deserializer.getObject().get())) {
            return type.cast(deserializer.getObject().get());
        }
        log.warn("No se pudo deserializar evento Stripe {} como {}", event.getId(), type.getSimpleName());
        return null;
    }
}
