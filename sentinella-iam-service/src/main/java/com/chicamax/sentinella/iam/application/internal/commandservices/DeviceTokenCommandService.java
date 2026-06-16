package com.chicamax.sentinella.iam.application.internal.commandservices;

import com.chicamax.sentinella.iam.domain.model.entities.DeviceToken;
import com.chicamax.sentinella.iam.infrastructure.persistence.jpa.DeviceTokenRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceTokenCommandService {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceTokenCommandService(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Transactional
    public DeviceToken register(UUID userId, String token, String platform) {
        return deviceTokenRepository.findByUserIdAndToken(userId, token)
                .map(existing -> {
                    existing.touch();
                    return deviceTokenRepository.save(existing);
                })
                .orElseGet(() -> deviceTokenRepository.save(
                        new DeviceToken(UUID.randomUUID(), userId, token, platform)
                ));
    }

    @Transactional
    public void unregister(UUID userId, String token) {
        deviceTokenRepository.deleteByUserIdAndToken(userId, token);
    }

    @Transactional(readOnly = true)
    public List<DeviceToken> findPushTargets(UUID tailingDamId) {
        return deviceTokenRepository.findActiveFieldOperatorTokensForDam(tailingDamId);
    }
}
