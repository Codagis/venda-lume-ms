package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.dto.product.ProductCreateRequest;
import com.vendalume.vendalume.api.dto.product.ProductFilterRequest;
import com.vendalume.vendalume.api.dto.product.ProductResponse;
import com.vendalume.vendalume.api.dto.product.ProductUpdateRequest;
import com.vendalume.vendalume.repository.TenantRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import com.vendalume.vendalume.service.GcsStorageService;
import com.vendalume.vendalume.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller de gestão de produtos.
 * Inclui endpoint de upload de imagem de produto (organização: empresa/produtos/{id}-{nome}).
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController implements ProductControllerApi {

    private final ProductService productService;
    private final Optional<GcsStorageService> gcsStorageService;
    private final TenantRepository tenantRepository;

    @Override
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        ProductResponse response = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @Override
    public ResponseEntity<ProductResponse> findBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.findBySku(sku));
    }

    @Override
    public ResponseEntity<ProductResponse> findByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(productService.findByBarcode(barcode));
    }

    @Override
    public ResponseEntity<PageResponse<ProductResponse>> search(
            @RequestParam(required = false) UUID tenantId,
            @Valid @RequestBody ProductFilterRequest filter) {
        return ResponseEntity.ok(productService.search(tenantId, filter));
    }

    @Override
    public ResponseEntity<List<ProductResponse>> listActive() {
        return ResponseEntity.ok(productService.listActive());
    }

    @Override
    public ResponseEntity<List<ProductResponse>> listAvailableForSale() {
        return ResponseEntity.ok(productService.listAvailableForSale());
    }

    @Override
    public ResponseEntity<List<ProductResponse>> listAvailableForDelivery() {
        return ResponseEntity.ok(productService.listAvailableForDelivery());
    }

    @Override
    public ResponseEntity<List<ProductResponse>> listFeatured() {
        return ResponseEntity.ok(productService.listFeatured());
    }

    @Override
    public ResponseEntity<List<ProductResponse>> listLowStock() {
        return ResponseEntity.ok(productService.listLowStock());
    }

    @Override
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id, @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @Override
    public ResponseEntity<ProductResponse> updateStock(@PathVariable UUID id, @RequestParam BigDecimal quantity) {
        return ResponseEntity.ok(productService.updateStock(id, quantity));
    }

    @Override
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "productId", required = false) UUID productId,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "tenantId", required = false) UUID requestTenantId) {
        if (gcsStorageService.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Upload não configurado. Habilite o Google Cloud Storage."));
        }
        try {
            String folder = resolveProductFolder(productId, productName, requestTenantId);
            String url = gcsStorageService.get().uploadImage(file, folder);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Falha no upload: " + e.getMessage()));
        }
    }

    private String resolveProductFolder(UUID productId, String productName, UUID requestTenantId) {
        UUID tenantId = (SecurityUtils.isCurrentUserRoot() && requestTenantId != null)
                ? requestTenantId
                : SecurityUtils.requireTenantId();
        String tenantSlug = tenantRepository.findById(tenantId)
                .map(t -> toSlug(t.getName()))
                .filter(s -> !s.isBlank())
                .orElseGet(() -> tenantId.toString());
        String base = "tenants/" + tenantSlug + "/products";
        if (productId != null && productName != null && !productName.isBlank()) {
            String slug = toSlug(productName);
            return slug.isBlank() ? base + "/" + productId : base + "/" + productId + "-" + slug;
        }
        return base + "/novo";
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
