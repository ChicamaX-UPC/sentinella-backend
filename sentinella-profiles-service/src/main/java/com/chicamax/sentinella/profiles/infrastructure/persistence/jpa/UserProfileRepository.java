package com.chicamax.sentinella.profiles.infrastructure.persistence.jpa;

import com.chicamax.sentinella.profiles.domain.model.aggregates.UserProfile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    boolean existsByUserId(UUID userId);
}
