package com.chicamax.sentinella.fieldoperations.domain.services;

import java.util.Optional;
import java.util.UUID;

public interface ChecklistPhotoStorage {

    /** Devuelve la clave de almacenamiento (S3 key o ruta local relativa). */
    String store(UUID roundId, UUID itemId, byte[] content, String contentType, String originalFilename);

    Optional<StoredChecklistPhoto> load(String storageKey);
}
