package com.chicamax.sentinella.payments.infrastructure.stripe;

import com.chicamax.sentinella.payments.domain.model.aggregates.Payment;
import com.chicamax.sentinella.payments.domain.services.PaymentCommandService;
import com.chicamax.sentinella.payments.infrastructure.billing.BillingNotificationService;
import com.chicamax.sentinella.payments.infrastructure.persistence.jpa.BillingCustomerRepository;
import com.chicamax.sentinella.payments.infrastructure.persistence.jpa.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StripeCheckoutConfirmationService {

    private static final Logger log = LoggerFactory.getLogger(StripeCheckoutConfirmationService.class);

    private final StripeProperties stripeProperties;
    private final PaymentCommandService paymentCommandService;
    private final PaymentRepository paymentRepository;
    private final BillingNotificationService billingNotificationService;
    private final BillingCustomerRepository billingCustomerRepository;

    public StripeCheckoutConfirmationService(
            StripeProperties stripeProperties,
            PaymentCommandService paymentCommandService,
            PaymentRepository paymentRepository,
            BillingNotificationService billingNotificationService,
            BillingCustomerRepository billingCustomerRepository
    ) {
        this.stripeProperties = stripeProperties;
        this.paymentCommandService = paymentCommandService;
        this.paymentRepository = paymentRepository;
        this.billingNotificationService = billingNotificationService;
        this.billingCustomerRepository = billingCustomerRepository;
    }

    public Payment confirmSession(UUID userId, String sessionId) {
        if (!stripeProperties.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Stripe no configurado");
        }
        if (sessionId == null || sessionId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessionId requerido");
        }

        try {
            Session session = Session.retrieve(sessionId);
            UUID paymentId = resolvePaymentId(session);
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));

            if (!payment.getUserId().equals(userId)) {
                log.warn(
                        "Confirmación rechazada: pago {} pertenece a {} y el JWT es {}",
                        paymentId,
                        payment.getUserId(),
                        userId
                );
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sesión de pago no válida para este usuario");
            }

            if (payment.isCompleted()) {
                log.info("Pago {} ya confirmado (p. ej. vía webhook Stripe)", paymentId);
                return payment;
            }

            if (!"complete".equalsIgnoreCase(session.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pago aún no está completado");
            }

            Payment confirmed = paymentCommandService.confirmPayment(paymentId, session.getSubscription());
            sendReceiptIfPossible(userId, session, confirmed);
            return confirmed;
        } catch (StripeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo verificar la sesión de Stripe", ex);
        }
    }

    private static UUID resolvePaymentId(Session session) {
        String paymentIdRaw = session.getClientReferenceId();
        if (paymentIdRaw == null && session.getMetadata() != null) {
            paymentIdRaw = session.getMetadata().get("paymentId");
        }
        if (paymentIdRaw == null || paymentIdRaw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sesión sin referencia de pago");
        }
        return UUID.fromString(paymentIdRaw);
    }

    private void sendReceiptIfPossible(UUID userId, Session session, Payment payment) {
        if (!payment.isCompleted()) {
            return;
        }
        String email = session.getCustomerDetails() != null ? session.getCustomerDetails().getEmail() : null;
        if (email == null || email.isBlank()) {
            email = billingCustomerRepository.findByUserId(userId)
                    .map(customer -> customer.getEmail())
                    .orElse(null);
        }
        if (email == null || email.isBlank()) {
            log.info("Pago {} confirmado sin email para recibo", payment.getId());
            return;
        }
        String company = session.getCustomerDetails() != null ? session.getCustomerDetails().getName() : null;
        billingNotificationService.sendPaymentReceipt(
                email,
                company,
                BigDecimal.valueOf(payment.getAmountCents()).movePointLeft(2),
                payment.getCurrency(),
                resolveHostedInvoiceUrl(session.getSubscription())
        );
    }

    private static String resolveHostedInvoiceUrl(String stripeSubscriptionId) {
        if (stripeSubscriptionId == null || stripeSubscriptionId.isBlank()) {
            return null;
        }
        try {
            com.stripe.model.Subscription subscription =
                    com.stripe.model.Subscription.retrieve(stripeSubscriptionId);
            String invoiceId = subscription.getLatestInvoice();
            if (invoiceId == null || invoiceId.isBlank()) {
                return null;
            }
            Invoice invoice = Invoice.retrieve(invoiceId);
            return invoice.getHostedInvoiceUrl();
        } catch (StripeException ex) {
            log.debug("No se pudo obtener comprobante Stripe: {}", ex.getMessage());
            return null;
        }
    }
}
