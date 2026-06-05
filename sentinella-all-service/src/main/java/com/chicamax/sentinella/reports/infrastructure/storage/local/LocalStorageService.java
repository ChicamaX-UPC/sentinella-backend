package com.chicamax.sentinella.reports.infrastructure.storage.local;

import com.chicamax.sentinella.reports.domain.services.StorageService;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "storage.s3.enabled", havingValue = "false", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path basePath;

    public LocalStorageService(@Value("${storage.local.base-path:}") String basePath) {
        String resolved = basePath == null || basePath.isBlank()
                ? Path.of(System.getProperty("java.io.tmpdir"), "sentinella-reports").toString()
                : basePath;
        this.basePath = Path.of(resolved).toAbsolutePath().normalize();
    }

    @Override
    public String saveReport(byte[] content, String filename) {
        try {
            Files.createDirectories(basePath);
            Path filePath = basePath.resolve(filename);
            Files.write(filePath, content);
            return filePath.toString();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el reporte", e);
        }
    }

    @Override
    public URI getDownloadUri(String storageKey) {
        return URI.create("file:///" + storageKey.replace("\\", "/"));
    }

    @Override
    public byte[] readReport(String storageKey) {
        try {
            return Files.readAllBytes(Path.of(storageKey));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el reporte", e);
        }
    }
}
