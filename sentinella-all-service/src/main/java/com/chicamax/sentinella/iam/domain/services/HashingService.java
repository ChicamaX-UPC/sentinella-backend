package com.chicamax.sentinella.iam.domain.services;

public interface HashingService {
    String hash(String rawValue);

    boolean matches(String rawValue, String hashedValue);
}
