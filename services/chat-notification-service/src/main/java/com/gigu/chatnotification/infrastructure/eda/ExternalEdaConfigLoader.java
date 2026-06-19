package com.gigu.chatnotification.infrastructure.eda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ExternalEdaConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(ExternalEdaConfigLoader.class);

    private final Storage storage;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    public ExternalEdaConfigLoader(Storage storage, ObjectMapper objectMapper, Environment environment) {
        this.storage = storage;
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    public ExternalEdaConfig load() {
        String bucket = trimmed(environment.getProperty("EDA_CONFIG_BUCKET"));
        String objectPath = trimmed(environment.getProperty("EDA_CONFIG_OBJECT"));

        if (!hasText(bucket) && !hasText(objectPath)) {
            return ExternalEdaConfig.fromEnvironment(environment);
        }

        if (!hasText(bucket) || !hasText(objectPath)) {
            throw new IllegalStateException("Both EDA_CONFIG_BUCKET and EDA_CONFIG_OBJECT must be provided when external EDA config is enabled");
        }

        return loadFromStorage(bucket, objectPath);
    }

    private ExternalEdaConfig loadFromStorage(String bucket, String objectPath) {
        String uri = "gs://" + bucket + "/" + objectPath;
        try {
            Blob blob = storage.get(BlobId.of(bucket, objectPath));
            if (blob == null) {
                throw new IllegalStateException("External EDA config not found at " + uri);
            }
            byte[] content = blob.getContent();
            ExternalEdaConfig config = objectMapper.readValue(new String(content, StandardCharsets.UTF_8), ExternalEdaConfig.class);
            config.validateStrict();
            log.info("Loaded external EDA config from {}", uri);
            return config;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load external EDA config from " + uri, e);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String trimmed(String value) {
        return value == null ? null : value.trim();
    }
}
