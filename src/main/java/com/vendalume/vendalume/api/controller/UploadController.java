package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.security.SecurityUtils;
import com.vendalume.vendalume.service.GcsStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller de upload de imagens para Google Cloud Storage.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@DefaultApiResponses
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@Tag(name = "Upload", description = "Upload de imagens para Google Cloud Storage")
public class UploadController {

    private final Optional<GcsStorageService> gcsStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Enviar imagem", description = "Envia uma imagem para o GCS. Para produto: use type=product, productId e productName para organização no bucket.")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "productId", required = false) UUID productId,
            @RequestParam(value = "productName", required = false) String productName) {
        if (gcsStorageService.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Upload não configurado. Habilite o Google Cloud Storage (vendalume.gcs.enabled=true)."));
        }
        try {
            String effectiveFolder = resolveFolder(folder, type, productId, productName);
            String url = gcsStorageService.get().uploadImage(file, effectiveFolder);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Falha no upload: " + e.getMessage()));
        }
    }

    private String resolveFolder(String folder, String type, UUID productId, String productName) {
        if (folder != null && !folder.isBlank()) {
            return folder;
        }
        if ("product".equalsIgnoreCase(type)) {
            UUID tenantId = SecurityUtils.requireTenantId();
            String base = "tenants/" + tenantId + "/products";
            if (productId != null && productName != null && !productName.isBlank()) {
                String slug = toSlug(productName);
                return slug.isBlank() ? base + "/" + productId : base + "/" + productId + "-" + slug;
            }
            return base + "/novo";
        }
        return "uploads";
    }

    private static String toSlug(String name) {
        if (name == null || name.isBlank()) return "";
        String slug = name.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\sáàâãéêíóôõúç-]", "")
                .replaceAll("[áàâã]", "a").replaceAll("[éê]", "e").replaceAll("í", "i")
                .replaceAll("[óôõ]", "o").replaceAll("[ú]", "u").replaceAll("ç", "c")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return slug.length() > 80 ? slug.substring(0, 80) : slug;
    }
}
