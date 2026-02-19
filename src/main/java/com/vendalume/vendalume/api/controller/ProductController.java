package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.dto.product.ProductCreateRequest;
import com.vendalume.vendalume.api.dto.product.ProductFilterRequest;
import com.vendalume.vendalume.api.dto.product.ProductResponse;
import com.vendalume.vendalume.api.dto.product.ProductUpdateRequest;
import com.vendalume.vendalume.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Controller de gestão de produtos.
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
    public ResponseEntity<PageResponse<ProductResponse>> search(@Valid @RequestBody ProductFilterRequest filter) {
        return ResponseEntity.ok(productService.search(filter));
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
}
