package com.chicamax.sentinella.iam.infrastructure.tokens.jwt;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtKeyProvider {

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    public JwtKeyProvider(
            @Value("${jwt.public-key}") String publicKeyPem,
            @Value("${jwt.private-key}") String privateKeyPem
    ) {
        publicKeyPem = normalizePem(publicKeyPem);
        privateKeyPem = normalizePem(privateKeyPem);
        if (isBlank(publicKeyPem) || isBlank(privateKeyPem)) {
            KeyPair keyPair = generateKeyPair();
            this.publicKey = (RSAPublicKey) keyPair.getPublic();
            this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return;
        }
        this.publicKey = parsePublicKey(publicKeyPem);
        this.privateKey = parsePrivateKey(privateKeyPem);
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    private RSAPublicKey parsePublicKey(String pem) {
        try {
            String clean = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] bytes = Base64.getDecoder().decode(clean);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("JWT public key invalida", e);
        }
    }

    private RSAPrivateKey parsePrivateKey(String pem) {
        try {
            String clean = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] bytes = Base64.getDecoder().decode(clean);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("JWT private key invalida", e);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Permite PEM en una sola línea en variables de entorno (\\n). */
    private static String normalizePem(String pem) {
        if (pem == null) {
            return "";
        }
        return pem.replace("\\n", "\n").trim();
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el par de claves JWT", e);
        }
    }
}
