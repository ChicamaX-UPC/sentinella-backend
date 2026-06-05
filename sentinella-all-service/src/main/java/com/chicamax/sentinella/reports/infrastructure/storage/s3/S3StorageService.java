package com.chicamax.sentinella.reports.infrastructure.storage.s3;

import com.chicamax.sentinella.reports.domain.services.StorageService;
import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@ConditionalOnProperty(name = "storage.s3.enabled", havingValue = "true")
public class S3StorageService implements StorageService {

    private final String bucket;
    private final String objectPrefix;
    private final long presignSeconds;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public S3StorageService(
            @Value("${storage.s3.region:us-east-1}") String region,
            @Value("${storage.s3.bucket:}") String bucket,
            @Value("${storage.s3.prefix:reports}") String objectPrefix,
            @Value("${storage.s3.access-key:}") String accessKey,
            @Value("${storage.s3.secret-key:}") String secretKey,
            @Value("${storage.s3.download-url-expires-seconds:3600}") long presignSeconds,
            @Value("${storage.s3.endpoint:}") String endpointOverride
    ) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("storage.s3.bucket es obligatorio con storage.s3.enabled=true");
        }
        this.bucket = bucket;
        this.objectPrefix = normalizePrefix(objectPrefix);
        this.presignSeconds = presignSeconds;
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
        var presignerBuilder = S3Presigner.builder()
                .region(awsRegion)
                .credentialsProvider(credentials)
                .serviceConfiguration(s3Config);
        if (customEndpoint) {
            URI ep = URI.create(endpointOverride);
            clientBuilder.endpointOverride(ep);
            presignerBuilder.endpointOverride(ep);
        }
        this.s3Client = clientBuilder.build();
        this.s3Presigner = presignerBuilder.build();
    }

    @Override
    public String saveReport(byte[] content, String filename) {
        String key = this.objectPrefix.isEmpty() ? filename : this.objectPrefix + "/" + filename;
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentLength((long) content.length)
                        .build(),
                RequestBody.fromBytes(content)
        );
        return key;
    }

    @Override
    public byte[] readReport(String storageKey) {
        return s3Client.getObject(
                GetObjectRequest.builder().bucket(bucket).key(storageKey).build(),
                (response, inputStream) -> inputStream.readAllBytes()
        );
    }

    @Override
    public URI getDownloadUri(String storageKey) {
        GetObjectRequest getObject = GetObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .build();
        GetObjectPresignRequest presign = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignSeconds))
                .getObjectRequest(getObject)
                .build();
        try {
            return s3Presigner.presignGetObject(presign).url().toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("URL de descarga invalida", e);
        }
    }

    @PreDestroy
    public void close() {
        s3Client.close();
        s3Presigner.close();
    }

    private static String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "";
        }
        return prefix.replace('\\', '/').replaceAll("/+$", "");
    }

    private static AwsCredentialsProvider resolveCredentials(String accessKey, String secretKey) {
        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        }
        return DefaultCredentialsProvider.create();
    }
}
