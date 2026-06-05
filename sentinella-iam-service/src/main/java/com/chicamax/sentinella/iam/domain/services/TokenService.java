package com.chicamax.sentinella.iam.domain.services;

import com.chicamax.sentinella.iam.domain.model.aggregates.User;
import com.chicamax.sentinella.iam.domain.model.valueobjects.AuthTokens;
import com.chicamax.sentinella.iam.domain.model.valueobjects.DecodedToken;

public interface TokenService {
    AuthTokens issueTokens(User user);

    DecodedToken decode(String token);
}
