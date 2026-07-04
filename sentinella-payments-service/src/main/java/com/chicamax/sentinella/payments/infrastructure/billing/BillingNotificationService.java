package com.chicamax.sentinella.payments.infrastructure.billing;

import com.chicamax.sentinella.shared.infrastructure.mail.PlainTextMailClient;
import com.chicamax.sentinella.shared.infrastructure.mail.SentinellaEmailTemplate;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingNotificationService {

    private static final Logger log = LoggerFactory.getLogger(BillingNotificationService.class);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", Locale.forLanguageTag("es"));

    private final ObjectProvider<PlainTextMailClient> mailClient;
    private final String appUrl;

    public BillingNotificationService(
            ObjectProvider<PlainTextMailClient> mailClient,
            @Value("${sentinella.app-url:https://sentinella-frontend.vercel.app}") String appUrl
    ) {
        this.mailClient = mailClient;
        this.appUrl = appUrl == null ? "https://sentinella-frontend.vercel.app" : appUrl.replaceAll("/$", "");
    }

    public void sendPaymentReceipt(String toEmail, String companyLabel, BigDecimal amount, String currency, String invoiceUrl) {
        String amountLabel = formatMoney(amount, currency);
        String text = """
                Hola%s,

                Confirmamos el pago de su suscripción Sentinella por %s.

                Puede descargar su comprobante desde el enlace de este correo o desde su perfil.

                Gracias por confiar en Sentinella.
                """.formatted(companySuffix(companyLabel), amountLabel);

        String htmlBody = SentinellaEmailTemplate.paragraphs(text)
                + SentinellaEmailTemplate.detailRow("Importe", amountLabel);

        dispatchStyled(
                toEmail,
                "Recibo de pago — Sentinella",
                "Pago confirmado",
                text,
                htmlBody,
                invoiceUrl != null && !invoiceUrl.isBlank() ? "Ver comprobante" : "Ir al panel",
                invoiceUrl != null && !invoiceUrl.isBlank() ? invoiceUrl : appUrl + "/dashboard"
        );
    }

    public void sendRenewalReminder(String toEmail, String companyLabel, OffsetDateTime chargeDate, BigDecimal amount, String currency) {
        String amountLabel = formatMoney(amount, currency);
        String text = """
                Hola%s,

                El %s realizaremos el cobro mensual automático de su suscripción Sentinella por %s.

                La tarjeta guardada en Stripe se utilizará para este cargo. Si necesita actualizar su método de pago, use el portal de facturación desde su perfil.
                """.formatted(companySuffix(companyLabel), chargeDate.format(DATE_FMT), amountLabel);

        String htmlBody = SentinellaEmailTemplate.paragraphs(text)
                + SentinellaEmailTemplate.detailRow("Próximo cobro", chargeDate.format(DATE_FMT))
                + SentinellaEmailTemplate.detailRow("Importe", amountLabel);

        dispatchStyled(
                toEmail,
                "Próximo cobro de suscripción — Sentinella",
                "Recordatorio de renovación",
                text,
                htmlBody,
                "Gestionar método de pago",
                appUrl + "/profile?tab=subscriptions"
        );
    }

    public void sendPaymentFailed(String toEmail, String companyLabel, String reason, OffsetDateTime nextRetry) {
        String text = """
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

        String htmlBody = SentinellaEmailTemplate.paragraphs(text)
                + SentinellaEmailTemplate.detailRow("Próximo reintento", nextRetry.format(DATE_FMT));

        dispatchStyled(
                toEmail,
                "Pago de suscripción fallido — Sentinella",
                "No pudimos cobrar su suscripción",
                text,
                htmlBody,
                "Actualizar método de pago",
                appUrl + "/profile?tab=subscriptions"
        );
    }

    public void sendServiceSuspended(String toEmail, String companyLabel) {
        String text = """
                Hola%s,

                Su suscripción Sentinella ha sido suspendida porque no se pudo cobrar el período actual antes de la fecha de corte.

                Para reactivar el servicio, inicie sesión, elija un plan y complete el pago con un método válido.
                """.formatted(companySuffix(companyLabel));

        dispatchStyled(
                toEmail,
                "Servicio suspendido — Sentinella",
                "Suscripción suspendida",
                text,
                SentinellaEmailTemplate.paragraphs(text),
                "Reactivar suscripción",
                appUrl + "/subscribe"
        );
    }

    private void dispatchStyled(
            String toEmail,
            String subject,
            String headline,
            String textBody,
            String htmlBody,
            String ctaLabel,
            String ctaHref
    ) {
        PlainTextMailClient client = mailClient.getIfAvailable();
        if (client == null) {
            log.info("Correo no configurado (Resend/SendGrid); omitido [{}] → {}", subject, toEmail);
            return;
        }
        try {
            String html = SentinellaEmailTemplate.render(headline, htmlBody, ctaLabel, ctaHref);
            client.sendRich(toEmail, subject, textBody, html);
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
