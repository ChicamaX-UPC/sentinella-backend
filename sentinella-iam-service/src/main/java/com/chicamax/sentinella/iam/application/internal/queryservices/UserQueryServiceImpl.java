package com.chicamax.sentinella.iam.application.internal.queryservices;

import com.chicamax.sentinella.iam.domain.model.aggregates.User;
import com.chicamax.sentinella.iam.domain.model.queries.GetAllUsersQuery;
import com.chicamax.sentinella.iam.domain.model.queries.GetUserByEmailQuery;
import com.chicamax.sentinella.iam.domain.model.queries.GetUserByIdQuery;
import com.chicamax.sentinella.iam.domain.services.UserQueryService;
import com.chicamax.sentinella.iam.infrastructure.persistence.jpa.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> handle(GetAllUsersQuery query) {
        if (query.systemWide()) {
            return userRepository.findAll();
        }
        if (query.organizationId() == null) {
            return List.of();
        }
        return userRepository.findByOrganizationIdOrderByFullNameAsc(query.organizationId());
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId());
    }

    @Override
    public Optional<User> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmail(query.email());
    }
}
