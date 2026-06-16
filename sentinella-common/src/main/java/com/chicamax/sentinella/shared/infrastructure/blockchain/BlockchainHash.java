package com.chicamax.sentinella.shared.infrastructure.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class BlockchainHash {

    private BlockchainHash() {
    }

    public static String sha256(String canonicalPayload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonicalPayload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo calcular hash SHA-256", ex);
        }
    }
}
