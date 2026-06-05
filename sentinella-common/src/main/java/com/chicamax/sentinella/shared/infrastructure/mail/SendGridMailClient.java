package com.chicamax.sentinella.shared.infrastructure.mail;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "sentinella.sendgrid.api-key")
public class SendGridMailClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String fromEmail;

    public SendGridMailClient(
            RestClient.Builder restClientBuilder,
            @Value("${sentinella.sendgrid.api-key}") String apiKey,
            @Value("${sentinella.sendgrid.from-email:alerts@sentinella.demo}") String fromEmail
    ) {
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
    }

    public void send(String toEmail, String subject, String textBody) {
        Map<String, Object> payload = Map.of(
                "personalizations", new Object[] {
                        Map.of("to", new Object[] {Map.of("email", toEmail)})
                },
                "from", Map.of("email", fromEmail),
                "subject", subject,
                "content", new Object[] {
                        Map.of("type", "text/plain", "value", textBody)
                }
        );
        restClient.post()
                .uri("https://api.sendgrid.com/v3/mail/send")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}
