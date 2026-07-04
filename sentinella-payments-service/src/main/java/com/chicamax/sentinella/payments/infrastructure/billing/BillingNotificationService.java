package com.chicamax.sentinella.payments.infrastructure.billing;

import com.chicamax.sentinella.shared.infrastructure.mail.PlainTextMailClient;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class BillingNotificationService {

    private static final Logger log = LoggerFactory.getLogger(BillingNotificationService.class);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", Locale.forLanguageTag("es"));

    private final ObjectProvider<PlainTextMailClient> mailClient;

    public BillingNotificationService(ObjectProvider<PlainTextMailClient> mailClient) {
        this.mailClient = mailClient;
    }

    public void sendPaymentReceipt(String toEmail, String companyLabel, BigDecimal amount, String currency, String invoiceUrl) {
        String amountLabel = formatMoney(amount, currency);
        String body = """
                Hola%s,

                Confirmamos el pago de su suscripción Sentinella por %s.

                Puede descargar su comprobante aquí: %s

                Gracias por confiar en Sentinella.
                """.formatted(companySuffix(companyLabel), amountLabel, invoiceUrl != null ? invoiceUrl : "(disponible en el portal de facturación)");

        dispatch(toEmail, "Recibo de pago — Sentinella", body);
    }

    public void sendRenewalReminder(String toEmail, String companyLabel, OffsetDateTime chargeDate, BigDecimal amount, String currency) {
        String body = """
                Hola%s,

                Le recordamos que el %s se realizará el cobro mensual automático de su suscripción Sentinella por %s.

                La tarjeta guardada en Stripe se utilizará para este cargo. Si necesita actualizar su método de pago, use el portal de facturación desde su perfil.

                Sentinella — monitoreo de relaves.
                """.formatted(
                companySuffix(companyLabel),
                chargeDate.format(DATE_FMT),
                formatMoney(amount, currency)
        );

        dispatch(toEmail, "Próximo cobro de suscripción — Sentinella", body);
    }

    public void sendPaymentFailed(String toEmail, String companyLabel, String reason, OffsetDateTime nextRetry) {
        String body = """
                Hola%s,

                No pudimos procesar el cobro mensual de su suscripción Sentinella.

                Motivo: %s

                Reintentaremos el cobro automáticamente el %s. Actualice su tarjeta en el portal de facturación si el problema persiste.

                Si el pago no se completa antes de la fecha de corte, el servicio será suspendido.
                """.formatted(
                companySuffix(companyLabel),
                reason != null && !reason.isBlank() ? reason : "Pago rechazado por el emisor de la tarjeta",
                nextRetry.format(DATE_FMT)
        );

        dispatch(toEmail, "Pago de suscripción fallido — Sentinella", body);
    }

    public void sendServiceSuspended(String toEmail, String companyLabel) {
        String body = """
                Hola%s,

                Su suscripción Sentinella ha sido suspendida porque no se pudo cobrar el período actual antes de la fecha de corte.

                Para reactivar el servicio, inicie sesión, elija un plan y complete el pago con un método válido.

                Si necesita ayuda, responda a este correo.
                """.formatted(companySuffix(companyLabel));

        dispatch(toEmail, "Servicio suspendido — Sentinella", body);
    }

    private void dispatch(String toEmail, String subject, String body) {
        PlainTextMailClient client = mailClient.getIfAvailable();
        if (client == null) {
            log.info("Correo no configurado (Resend/SendGrid); omitido [{}] → {}", subject, toEmail);
            return;
        }
        try {
            client.send(toEmail, subject, body);
        } catch (Exception ex) {
            log.warn("No se pudo enviar correo de facturación a {}: {}", toEmail, ex.getMessage());
        }
    }

    private static String companySuffix(String companyLabel) {
        if (companyLabel == null || companyLabel.isBlank()) {
            return "";
        }
        return " " + companyLabel.trim();
    }

    private static String formatMoney(BigDecimal amount, String currency) {
        if (amount == null) {
            return currency != null ? currency : "USD";
        }
        return amount.toPlainString() + " " + (currency != null ? currency.toUpperCase(Locale.ROOT) : "USD");
    }
}
