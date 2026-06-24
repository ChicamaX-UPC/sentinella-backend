package com.chicamax.sentinella.iam.infrastructure.persistence.jpa;

import com.chicamax.sentinella.iam.domain.model.aggregates.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByOrganizationIdOrderByFullNameAsc(UUID organizationId);
}
