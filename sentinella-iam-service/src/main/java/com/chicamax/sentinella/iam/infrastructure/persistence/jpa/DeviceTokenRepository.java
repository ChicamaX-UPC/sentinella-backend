package com.chicamax.sentinella.iam.infrastructure.persistence.jpa;

import com.chicamax.sentinella.iam.domain.model.entities.DeviceToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {

    Optional<DeviceToken> findByUserIdAndToken(UUID userId, String token);

    void deleteByUserIdAndToken(UUID userId, String token);

    @Query(value = """
            SELECT dt.* FROM iam.device_tokens dt
            INNER JOIN iam.users u ON u.id = dt.user_id
            WHERE u.is_active = TRUE
              AND u.role = 'FIELD_OPERATOR'
              AND (:tailingDamId = ANY(u.tailing_dam_ids))
            """, nativeQuery = true)
    List<DeviceToken> findActiveFieldOperatorTokensForDam(@Param("tailingDamId") UUID tailingDamId);
}
