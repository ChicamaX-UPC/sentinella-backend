package com.chicamax.sentinella.shared.infrastructure.mail;

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
            @Value("${sentinella.resend.from-email:onboarding@resend.dev}") String fromEmail
    ) {
        this.fromEmail = fromEmail;
        this.restClient = restClientBuilder
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public void send(String toEmail, String subject, String textBody) {
        Map<String, Object> payload = Map.of(
                "from", fromEmail,
                "to", List.of(toEmail),
                "subject", subject,
                "text", textBody
        );
        restClient.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}
