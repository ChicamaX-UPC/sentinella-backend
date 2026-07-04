package com.chicamax.sentinella.shared.infrastructure.mail;

/** Cliente para correos transaccionales (facturación, reportes, alertas). */
public interface PlainTextMailClient {

    void sendPlain(String toEmail, String subject, String textBody);

    default void send(String toEmail, String subject, String textBody) {
        sendPlain(toEmail, subject, textBody);
    }

    default void sendStyled(
            String toEmail,
            String subject,
            String headline,
            String textBody,
            String ctaLabel,
            String ctaHref
    ) {
        String html = SentinellaEmailTemplate.render(
                headline,
                SentinellaEmailTemplate.paragraphs(textBody),
                ctaLabel,
                ctaHref
        );
        sendRich(toEmail, subject, textBody, html);
    }

    default void sendRich(String toEmail, String subject, String textBody, String htmlBody) {
        sendPlain(toEmail, subject, textBody);
    }
}
