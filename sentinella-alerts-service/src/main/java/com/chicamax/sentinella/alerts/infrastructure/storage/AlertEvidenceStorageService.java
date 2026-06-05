package com.chicamax.sentinella.alerts.infrastructure.storage;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AlertEvidenceStorageService {

    private final String basePath;

    public AlertEvidenceStorageService(@Value("${sentinella.alerts.evidence-base-path:/tmp/sentinella-evidence}") String basePath) {
        this.basePath = basePath;
    }

    public String store(UUID alertId, byte[] content, String filename) {
        java.nio.file.Path dir = java.nio.file.Path.of(basePath, alertId.toString());
        try {
            java.nio.file.Files.createDirectories(dir);
            java.nio.file.Path target = dir.resolve(filename);
            java.nio.file.Files.write(target, content);
            return alertId + "/" + filename;
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("No se pudo guardar evidencia", ex);
        }
    }
}
