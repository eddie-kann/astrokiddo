package com.astrokiddo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.cloudflare.r2")
@Getter
@Setter
public class CloudflareR2Properties {
    private String accountId;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String publicBaseUrl;

    public String getEndpoint() {
        return "https://" + accountId + ".r2.cloudflarestorage.com";
    }
}
