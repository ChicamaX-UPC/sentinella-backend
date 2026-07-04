package com.chicamax.sentinella.blockchain.infrastructure.ledger;

import com.chicamax.sentinella.blockchain.domain.services.LedgerPort;
import com.chicamax.sentinella.blockchain.domain.services.LedgerRegistrationResult;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.X509Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/** Adaptador Hyperledger Fabric Gateway (US17 produccion). */
@Component
@ConditionalOnProperty(name = "sentinella.blockchain.fabric.enabled", havingValue = "true")
public class FabricGatewayLedgerAdapter implements LedgerPort {

    private static final Logger log = LoggerFactory.getLogger(FabricGatewayLedgerAdapter.class);

    private final Path connectionProfilePath;
    private final Path certPath;
    private final Path keyPath;
    private final String mspId;
    private final String channelName;
    private final String chaincodeName;

    public FabricGatewayLedgerAdapter(
            @Value("${sentinella.blockchain.fabric.connection-profile:/fabric/config/connection-org1.yaml}") String connectionProfile,
            @Value("${sentinella.blockchain.fabric.cert-path:/fabric/crypto/user/cert.pem}") String certPath,
            @Value("${sentinella.blockchain.fabric.key-path:/fabric/crypto/user/key.pem}") String keyPath,
            @Value("${sentinella.blockchain.fabric.msp-id:Org1MSP}") String mspId,
            @Value("${sentinella.blockchain.fabric.channel:sentinellachannel}") String channelName,
            @Value("${sentinella.blockchain.fabric.chaincode:sentinella-ledger}") String chaincodeName
    ) {
        this.connectionProfilePath = Path.of(connectionProfile);
        this.certPath = Path.of(certPath);
        this.keyPath = Path.of(keyPath);
        this.mspId = mspId;
        this.channelName = channelName;
        this.chaincodeName = chaincodeName;
    }

    @Override
    @Retryable(
            retryFor = Exception.class,
            maxAttemptsExpression = "${sentinella.blockchain.fabric.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${sentinella.blockchain.fabric.retry.delay-ms:2000}",
                    multiplierExpression = "${sentinella.blockchain.fabric.retry.multiplier:2}"
            )
    )
    public LedgerRegistrationResult register(
            UUID recordId,
            String entityType,
            UUID entityId,
            UUID nodeId,
            String contentHash
    ) {
        try (Gateway gateway = openGateway()) {
            Network network = gateway.getNetwork(channelName);
            Contract contract = network.getContract(chaincodeName);
            byte[] result = contract.submitTransaction(
                    "RegisterHash",
                    entityType,
                    entityId.toString(),
                    nodeId == null ? "" : nodeId.toString(),
                    contentHash,
                    recordId.toString()
            );
            String txId = new String(result, StandardCharsets.UTF_8).trim();
            return new LedgerRegistrationResult(txId, true);
        } catch (Exception ex) {
            log.error("Fallo al registrar hash en Fabric entityType={} entityId={}", entityType, entityId, ex);
            throw new IllegalStateException("No se pudo registrar en Hyperledger Fabric", ex);
        }
    }

    @Override
    public boolean verifyOnChain(String entityType, UUID entityId, String contentHash) {
        try (Gateway gateway = openGateway()) {
            Network network = gateway.getNetwork(channelName);
            Contract contract = network.getContract(chaincodeName);
            byte[] stored = contract.evaluateTransaction("GetHash", entityType, entityId.toString());
            return contentHash.equals(new String(stored, StandardCharsets.UTF_8).trim());
        } catch (Exception ex) {
            log.warn("No se pudo verificar hash en Fabric entityType={} entityId={}", entityType, entityId, ex);
            return false;
        }
    }

    private Gateway openGateway() throws IOException, CertificateException, InvalidKeyException {
        X509Certificate certificate = readCertificate(certPath);
        PrivateKey privateKey = readPrivateKey(keyPath);
        X509Identity identity = Identities.newX509Identity(mspId, certificate, privateKey);

        return Gateway.createBuilder()
                .identity(identity)
                .networkConfig(connectionProfilePath)
                .discovery(false)
                .commitTimeout(60, TimeUnit.SECONDS)
                .connect();
    }

    private static X509Certificate readCertificate(Path path) throws IOException, CertificateException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return Identities.readX509Certificate(reader);
        }
    }

    private static PrivateKey readPrivateKey(Path path) throws IOException, InvalidKeyException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return Identities.readPrivateKey(reader);
        }
    }
}
