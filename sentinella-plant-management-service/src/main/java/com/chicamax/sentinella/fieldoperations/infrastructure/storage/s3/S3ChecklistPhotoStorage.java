package com.chicamax.sentinella.fieldoperations.infrastructure.storage.s3;

import com.chicamax.sentinella.fieldoperations.domain.services.ChecklistPhotoStorage;
import com.chicamax.sentinella.fieldoperations.domain.services.StoredChecklistPhoto;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@ConditionalOnProperty(name = "storage.s3.enabled", havingValue = "true")
public class S3ChecklistPhotoStorage implements ChecklistPhotoStorage {

    private final String bucket;
    private final String objectPrefix;
    private final S3Client s3Client;

    public S3ChecklistPhotoStorage(
            @Value("${storage.s3.region:us-east-1}") String region,
            @Value("${storage.s3.bucket:}") String bucket,
            @Value("${storage.s3.prefix:fieldops}") String objectPrefix,
            @Value("${storage.s3.access-key:}") String accessKey,
            @Value("${storage.s3.secret-key:}") String secretKey,
            @Value("${storage.s3.endpoint:}") String endpointOverride
    ) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("storage.s3.bucket es obligatorio con storage.s3.enabled=true");
        }
        this.bucket = bucket;
        this.objectPrefix = normalizePrefix(objectPrefix);
        Region awsRegion = Region.of(region);
        AwsCredentialsProvider credentials = resolveCredentials(accessKey, secretKey);
        boolean customEndpoint = endpointOverride != null && !endpointOverride.isBlank();
        S3Configuration s3Config = S3Configuration.builder()
                .pathStyleAccessEnabled(customEndpoint)
                .build();
        var clientBuilder = S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(credentials)
                .serviceConfiguration(s3Config);
        if (customEndpoint) {
            clientBuilder.endpointOverride(URI.create(endpointOverride));
        }
        this.s3Client = clientBuilder.build();
    }

    @Override
    public String store(UUID roundId, UUID itemId, byte[] content, String contentType, String originalFilename) {
        String safeName = sanitizeFilename(originalFilename);
        String relative = "checklist/" + roundId + "/" + itemId + "/" + UUID.randomUUID() + "-" + safeName;
        String key = objectPrefix.isEmpty() ? relative : objectPrefix + "/" + relative;
        var requestBuilder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentLength((long) content.length);
        if (contentType != null && !contentType.isBlank()) {
            requestBuilder.contentType(contentType);
        }
        s3Client.putObject(requestBuilder.build(), RequestBody.fromBytes(content));
        return key;
    }

    @Override
    public Optional<StoredChecklistPhoto> load(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return Optional.empty();
        }
        try {
            var response = s3Client.getObject(
                    GetObjectRequest.builder().bucket(bucket).key(storageKey).build()
            );
            byte[] bytes = response.readAllBytes();
            String contentType = response.response().contentType();
            return Optional.of(new StoredChecklistPhoto(
                    bytes,
                    contentType != null && !contentType.isBlank() ? contentType : "image/jpeg"
            ));
        } catch (NoSuchKeyException ex) {
            return Optional.empty();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer la foto del checklist", ex);
        }
    }

    @PreDestroy
    public void close() {
        s3Client.close();
    }

    private static String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "";
        }
        return prefix.replace('\\', '/').replaceAll("/+$", "");
    }

    private static String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "photo.jpg";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static AwsCredentialsProvider resolveCredentials(String accessKey, String secretKey) {
        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        }
        return DefaultCredentialsProvider.create();
    }
}
