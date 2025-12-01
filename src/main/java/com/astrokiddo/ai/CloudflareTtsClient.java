package com.astrokiddo.ai;

import com.astrokiddo.config.CloudflareAiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class CloudflareTtsClient {

    private static final Logger log = LoggerFactory.getLogger(CloudflareTtsClient.class);

    private final WebClient client;
    private final CloudflareAiProperties properties;

    public CloudflareTtsClient(WebClient cloudflareAiWebClient, CloudflareAiProperties properties) {
        this.client = cloudflareAiWebClient;
        this.properties = properties;
    }

    public Mono<byte[]> synthesize(String text, String speaker) {
        if (!properties.isTtsConfigured() || !properties.isEnabled()) {
            return Mono.error(new IllegalStateException("Cloudflare AI TTS is not configured"));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("text", text);
        body.put("encoding", "mp3");
        if (speaker != null && !speaker.isBlank()) {
            body.put("speaker", speaker);
        }

        return client.post()
                .uri(b -> b.path("/client/v4/accounts/{accountId}/ai/run/")
                        .pathSegment("{cfAiProvider}", "{cfTtsVendor}", "{cfTtsModel}")
                        .build(
                                properties.getAccountId(),
                                properties.getCfAiProvider(),
                                properties.getCfTtsVendor(),
                                properties.getCfTtsModel()
                        )
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(byte[].class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    String response = ex.getResponseBodyAsString(StandardCharsets.UTF_8);
                    String message = "Cloudflare TTS call failed: " + ex.getStatusCode() + " " + response;
                    log.warn(message);
                    return Mono.error(new IllegalStateException(message, ex));
                });
    }
}