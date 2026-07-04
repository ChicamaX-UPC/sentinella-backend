package com.chicamax.sentinella.shared.infrastructure.mail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Primary
@ConditionalOnProperty(name = "sentinella.resend.api-key")
public class ResendMailClient implements PlainTextMailClient {

    private final RestClient restClient;
    private final String fromEmail;

    public ResendMailClient(
            RestClient.Builder restClientBuilder,
            @Value("${sentinella.resend.api-key}") String apiKey,
            @Value("${sentinella.resend.from-email:Sentinella <sentinella@excusasjeans.com>}") String fromEmail
    ) {
        this.fromEmail = fromEmail;
        this.restClient = restClientBuilder
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public void sendPlain(String toEmail, String subject, String textBody) {
        dispatch(toEmail, subject, textBody, null);
    }

    @Override
    public void sendRich(String toEmail, String subject, String textBody, String htmlBody) {
        dispatch(toEmail, subject, textBody, htmlBody);
    }

    private void dispatch(String toEmail, String subject, String textBody, String htmlBody) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("from", fromEmail);
        payload.put("to", List.of(toEmail));
        payload.put("subject", subject);
        payload.put("text", textBody);
        if (htmlBody != null && !htmlBody.isBlank()) {
            payload.put("html", htmlBody);
        }
        restClient.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}
