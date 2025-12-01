package com.astrokiddo.storage;

import com.astrokiddo.config.CloudflareR2Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class R2StorageService {

    private static final Logger log = LoggerFactory.getLogger(R2StorageService.class);

    private final S3Client s3Client;
    private final CloudflareR2Properties properties;

    public R2StorageService(S3Client s3Client, CloudflareR2Properties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    public String saveAudio(String objectKey, byte[] data) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));
        String url = properties.getPublicBaseUrl() + "/" + objectKey;
        log.info("Saved audio to R2: {}", url);
        return url;
    }
}
