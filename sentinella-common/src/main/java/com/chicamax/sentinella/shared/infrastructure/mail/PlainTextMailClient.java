package com.chicamax.sentinella.shared.infrastructure.mail;

/** Cliente mínimo para correos transaccionales (facturación, reportes). */
public interface PlainTextMailClient {

    void send(String toEmail, String subject, String textBody);
}
