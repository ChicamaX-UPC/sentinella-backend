package com.chicamax.sentinella.iam.infrastructure.hashing.bcrypt;

import com.chicamax.sentinella.iam.domain.services.HashingService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BCryptHashingService implements HashingService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String rawValue) {
        return encoder.encode(rawValue);
    }

    @Override
    public boolean matches(String rawValue, String hashedValue) {
        return encoder.matches(rawValue, hashedValue);
    }
}
