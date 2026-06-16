package com.chicamax.sentinella.fieldoperations.infrastructure.storage.local;

import com.chicamax.sentinella.fieldoperations.domain.services.ChecklistPhotoStorage;
import com.chicamax.sentinella.fieldoperations.domain.services.StoredChecklistPhoto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "storage.s3.enabled", havingValue = "false", matchIfMissing = true)
public class LocalChecklistPhotoStorage implements ChecklistPhotoStorage {

    private final Path basePath;

    public LocalChecklistPhotoStorage(
            @Value("${storage.local.base-path:${java.io.tmpdir}/sentinella-checklist-photos}") String basePath
    ) {
        this.basePath = Path.of(basePath);
    }

    @Override
    public String store(UUID roundId, UUID itemId, byte[] content, String contentType, String originalFilename) {
        String safeName = sanitizeFilename(originalFilename);
        String key = "checklist/" + roundId + "/" + itemId + "/" + UUID.randomUUID() + "-" + safeName;
        Path target = basePath.resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return key;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar la foto del checklist", ex);
        }
    }

    @Override
    public Optional<StoredChecklistPhoto> load(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return Optional.empty();
        }
        Path target = basePath.resolve(storageKey);
        if (!Files.exists(target)) {
            return Optional.empty();
        }
        try {
            byte[] bytes = Files.readAllBytes(target);
            String contentType = Files.probeContentType(target);
            return Optional.of(new StoredChecklistPhoto(
                    bytes,
                    contentType != null && !contentType.isBlank() ? contentType : "image/jpeg"
            ));
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer la foto del checklist", ex);
        }
    }

    private static String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "photo.jpg";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
