package com.chicamax.sentinella.shared.infrastructure.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "sentinella.sendgrid.api-key")
@ConditionalOnMissingBean(ResendMailClient.class)
public class SendGridMailClient implements PlainTextMailClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String fromEmail;

    public SendGridMailClient(
            RestClient.Builder restClientBuilder,
            @Value("${sentinella.sendgrid.api-key}") String apiKey,
            @Value("${sentinella.sendgrid.from-email:sentinella@excusasjeans.com}") String fromEmail
    ) {
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
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
        List<Map<String, String>> content = new ArrayList<>();
        content.add(Map.of("type", "text/plain", "value", textBody));
        if (htmlBody != null && !htmlBody.isBlank()) {
            content.add(Map.of("type", "text/html", "value", htmlBody));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("personalizations", List.of(Map.of("to", List.of(Map.of("email", toEmail)))));
        payload.put("from", Map.of("email", fromEmail, "name", "Sentinella"));
        payload.put("subject", subject);
        payload.put("content", content);

        restClient.post()
                .uri("https://api.sendgrid.com/v3/mail/send")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}
