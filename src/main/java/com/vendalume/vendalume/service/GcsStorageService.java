package com.vendalume.vendalume.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.vendalume.vendalume.config.GcsEnabledCondition;
import com.vendalume.vendalume.config.GcsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Serviço de upload de arquivos para o Google Cloud Storage.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Conditional(GcsEnabledCondition.class)
public class GcsStorageService {

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    private final Storage storage;
    private final GcsProperties gcsProperties;

    /**
     * Faz upload de um arquivo de imagem para o bucket GCS.
     *
     * @param file   arquivo (ex.: multipart do request)
     * @param folder pasta lógica no bucket (ex.: "tenants", "products")
     * @return URL pública do arquivo (requer bucket/objeto com leitura pública)
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode ser vazio.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Tipo de arquivo não permitido. Use: JPEG, PNG, GIF ou WebP.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Arquivo muito grande. Máximo: 5 MB.");
        }
        String bucketName = gcsProperties.getBucketName();
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException("Bucket GCS não configurado.");
        }
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.'))
                : contentTypeToExt(contentType);
        String objectName = (folder != null && !folder.isBlank() ? folder + "/" : "")
                + UUID.randomUUID().toString() + ext;

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();
        storage.create(blobInfo, file.getBytes());
        String url = "https://storage.googleapis.com/" + bucketName + "/" + objectName;
        log.debug("Upload GCS: {} -> {}", objectName, url);
        return url;
    }

    /**
     * Faz upload do certificado PFX para o bucket GCS (pasta tenants/certificados).
     *
     * @param tenantId ID do tenant (empresa)
     * @param pfxBytes conteúdo binário do arquivo .pfx/.p12
     * @return URL do objeto no GCS (bucket deve permitir leitura conforme política)
     */
    public String uploadCertificate(UUID tenantId, byte[] pfxBytes) throws IOException {
        if (tenantId == null || pfxBytes == null || pfxBytes.length == 0) {
            throw new IllegalArgumentException("Tenant e conteúdo do certificado são obrigatórios.");
        }
        String bucketName = gcsProperties.getBucketName();
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException("Bucket GCS não configurado.");
        }
        String objectName = "tenants/certificados/" + tenantId + ".pfx";
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("application/x-pkcs12")
                .build();
        storage.create(blobInfo, pfxBytes);
        String url = "https://storage.googleapis.com/" + bucketName + "/" + objectName;
        log.debug("Certificado PFX enviado ao GCS: {} -> {}", objectName, url);
        return url;
    }

    private static String contentTypeToExt(String contentType) {
        if (contentType == null) return ".bin";
        return switch (contentType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".bin";
        };
    }
}
