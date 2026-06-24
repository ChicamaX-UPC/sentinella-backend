package com.chicamax.sentinella.iam.domain.model.entities;

import com.chicamax.sentinella.shared.infrastructure.encryption.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "device_tokens", schema = "iam")
public class DeviceToken {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, length = 1024)
    private String token;

    @Column(nullable = false, length = 16)
    private String platform;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_seen_at", nullable = false)
    private OffsetDateTime lastSeenAt;

    protected DeviceToken() {
    }

    public DeviceToken(UUID id, UUID userId, String token, String platform) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.platform = platform;
        this.createdAt = OffsetDateTime.now();
        this.lastSeenAt = this.createdAt;
    }

    public void touch() {
        this.lastSeenAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public String getPlatform() {
        return platform;
    }
}
