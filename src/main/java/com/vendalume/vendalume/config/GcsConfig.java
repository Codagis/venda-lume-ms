package com.vendalume.vendalume.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Configura o cliente do Google Cloud Storage quando GCS está habilitado.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GcsConfig {

    private final GcsProperties gcsProperties;

    @Bean
    @Conditional(GcsEnabledCondition.class)
    public Storage googleCloudStorage() throws IOException {
        String bucket = gcsProperties.getBucketName();
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("vendalume.gcs.bucket-name é obrigatório quando GCS está habilitado.");
        }
        StorageOptions.Builder builder = StorageOptions.newBuilder();
        String projectId = Optional.ofNullable(gcsProperties.getProjectId()).filter(s -> !s.isBlank()).orElse(null);
        if (projectId != null) {
            builder.setProjectId(projectId);
        }
        String credentialsJson = gcsProperties.getCredentialsJson();
        if (credentialsJson != null && !credentialsJson.isBlank()) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))) {
                builder.setCredentials(GoogleCredentials.fromStream(bis));
            }
            log.debug("Credenciais GCS carregadas a partir do JSON da configuração.");
        } else if (gcsProperties.getCredentialsPath() != null && !gcsProperties.getCredentialsPath().isBlank()) {
            try (FileInputStream fis = new FileInputStream(gcsProperties.getCredentialsPath())) {
                builder.setCredentials(GoogleCredentials.fromStream(fis));
            }
        }
        Storage storage = builder.build().getService();
        log.info("Google Cloud Storage configurado. Bucket: {}", bucket);
        return storage;
    }
}
