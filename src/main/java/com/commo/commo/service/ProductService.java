package com.commo.commo.service;

import com.commo.commo.api.dto.product.PageResponse;
import com.commo.commo.api.dto.product.ProductCreateRequest;
import com.commo.commo.api.dto.product.ProductFilterRequest;
import com.commo.commo.api.dto.product.ProductResponse;
import com.commo.commo.api.dto.product.ProductUpdateRequest;
import com.commo.commo.api.exception.ResourceNotFoundException;
import com.commo.commo.domain.entity.Product;
import com.commo.commo.repository.ProductRepository;
import com.commo.commo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Serviço de gestão de produtos.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("name", "sku", "unitPrice", "displayOrder", "createdAt");

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        UUID userId = SecurityUtils.getCurrentUserId();

        validateSkuUnique(tenantId, null, request.getSku());
        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            validateBarcodeUnique(tenantId, null, request.getBarcode());
        }

        Product product = toEntity(request, tenantId);
        product.setCreatedBy(userId);
        product.setUpdatedBy(userId);
        product = productRepository.save(product);

        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", id));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse findBySku(String sku) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Product product = productRepository.findByTenantIdAndSku(tenantId, sku)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", sku));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse findByBarcode(String barcode) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Product product = productRepository.findByTenantIdAndBarcode(tenantId, barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", barcode));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(ProductFilterRequest filter) {
        UUID tenantId = SecurityUtils.requireTenantId();

        String sortField = isValidSortField(filter.getSortBy()) ? filter.getSortBy() : "name";
        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortDirection())
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(
                filter.getPage() != null ? filter.getPage() : 0,
                Math.min(filter.getSize() != null ? filter.getSize() : 20, 100),
                Sort.by(direction, sortField)
        );

        Page<Product> page = productRepository.searchByTenant(
                tenantId,
                filter.getActive(),
                filter.getCategoryId(),
                filter.getAvailableForSale(),
                filter.getAvailableForDelivery(),
                filter.getFeatured(),
                filter.getLowStock(),
                filter.getSearch() != null ? filter.getSearch().trim() : null,
                pageable
        );

        return PageResponse.<ProductResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listActive() {
        UUID tenantId = SecurityUtils.requireTenantId();
        return productRepository.findByTenantIdAndActiveOrderByDisplayOrderAscNameAsc(tenantId, true)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listAvailableForSale() {
        UUID tenantId = SecurityUtils.requireTenantId();
        return productRepository.findByTenantIdAndActiveAndAvailableForSaleTrueOrderByDisplayOrderAscNameAsc(tenantId, true)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listAvailableForDelivery() {
        UUID tenantId = SecurityUtils.requireTenantId();
        return productRepository.findByTenantIdAndActiveAndAvailableForDeliveryTrueOrderByDisplayOrderAscNameAsc(tenantId, true)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listFeatured() {
        UUID tenantId = SecurityUtils.requireTenantId();
        return productRepository.findByTenantIdAndActiveAndFeaturedTrueOrderByDisplayOrderAscNameAsc(tenantId, true)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listLowStock() {
        UUID tenantId = SecurityUtils.requireTenantId();
        return productRepository.findLowStockProducts(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse update(UUID id, ProductUpdateRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        UUID userId = SecurityUtils.getCurrentUserId();

        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", id));

        validateSkuUnique(tenantId, id, request.getSku());
        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            validateBarcodeUnique(tenantId, id, request.getBarcode());
        }

        updateEntity(product, request);
        product.setUpdatedBy(userId);
        product = productRepository.save(product);

        return toResponse(product);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", id));
        productRepository.delete(product);
    }

    @Transactional
    public ProductResponse updateStock(UUID id, java.math.BigDecimal quantity) {
        UUID tenantId = SecurityUtils.requireTenantId();

        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", id));

        if (!product.getTrackStock()) {
            throw new IllegalArgumentException("Produto não possui controle de estoque");
        }

        java.math.BigDecimal newStock = (product.getStockQuantity() != null ? product.getStockQuantity() : java.math.BigDecimal.ZERO)
                .add(quantity);
        if (newStock.compareTo(java.math.BigDecimal.ZERO) < 0 && !product.getAllowNegativeStock()) {
            throw new IllegalArgumentException("Quantidade insuficiente em estoque");
        }

        product.setStockQuantity(newStock);
        product = productRepository.save(product);

        return toResponse(product);
    }

    private void validateSkuUnique(UUID tenantId, UUID excludeId, String sku) {
        boolean exists = excludeId != null
                ? productRepository.existsByTenantIdAndSkuAndIdNot(tenantId, sku, excludeId)
                : productRepository.existsByTenantIdAndSku(tenantId, sku);
        if (exists) {
            throw new IllegalArgumentException("SKU já cadastrado: " + sku);
        }
    }

    private void validateBarcodeUnique(UUID tenantId, UUID excludeId, String barcode) {
        boolean exists = excludeId != null
                ? productRepository.existsByTenantIdAndBarcodeAndIdNot(tenantId, barcode, excludeId)
                : productRepository.existsByTenantIdAndBarcode(tenantId, barcode);
        if (exists) {
            throw new IllegalArgumentException("Código de barras já cadastrado: " + barcode);
        }
    }

    private boolean isValidSortField(String field) {
        return field != null && ALLOWED_SORT_FIELDS.contains(field);
    }

    private Product toEntity(ProductCreateRequest req, UUID tenantId) {
        return Product.builder()
                .tenantId(tenantId)
                .sku(req.getSku().trim())
                .barcode(req.getBarcode() != null ? req.getBarcode().trim() : null)
                .internalCode(req.getInternalCode() != null ? req.getInternalCode().trim() : null)
                .name(req.getName().trim())
                .shortDescription(req.getShortDescription() != null ? req.getShortDescription().trim() : null)
                .description(req.getDescription())
                .unitPrice(req.getUnitPrice())
                .costPrice(req.getCostPrice())
                .discountPrice(req.getDiscountPrice())
                .discountStartAt(req.getDiscountStartAt())
                .discountEndAt(req.getDiscountEndAt())
                .taxRate(req.getTaxRate())
                .unitOfMeasure(req.getUnitOfMeasure())
                .sellByWeight(req.getSellByWeight() != null ? req.getSellByWeight() : false)
                .trackStock(req.getTrackStock() != null ? req.getTrackStock() : false)
                .stockQuantity(req.getStockQuantity())
                .minStock(req.getMinStock())
                .allowNegativeStock(req.getAllowNegativeStock() != null ? req.getAllowNegativeStock() : false)
                .categoryId(req.getCategoryId())
                .brand(req.getBrand() != null ? req.getBrand().trim() : null)
                .ncm(req.getNcm())
                .cest(req.getCest())
                .weight(req.getWeight())
                .width(req.getWidth())
                .height(req.getHeight())
                .depth(req.getDepth())
                .preparationTimeMinutes(req.getPreparationTimeMinutes())
                .serveSize(req.getServeSize())
                .calories(req.getCalories())
                .ingredients(req.getIngredients())
                .allergens(req.getAllergens())
                .nutritionalInfo(req.getNutritionalInfo())
                .minOrderQuantity(req.getMinOrderQuantity())
                .maxOrderQuantity(req.getMaxOrderQuantity())
                .sellMultiple(req.getSellMultiple())
                .active(req.getActive() != null ? req.getActive() : true)
                .availableForSale(req.getAvailableForSale() != null ? req.getAvailableForSale() : true)
                .availableForDelivery(req.getAvailableForDelivery() != null ? req.getAvailableForDelivery() : true)
                .featured(req.getFeatured() != null ? req.getFeatured() : false)
                .isComposite(req.getIsComposite() != null ? req.getIsComposite() : false)
                .displayOrder(req.getDisplayOrder())
                .imageUrl(req.getImageUrl())
                .imageUrls(req.getImageUrls())
                .videoUrl(req.getVideoUrl())
                .build();
    }

    private void updateEntity(Product product, ProductUpdateRequest req) {
        product.setSku(req.getSku().trim());
        product.setBarcode(req.getBarcode() != null ? req.getBarcode().trim() : null);
        product.setInternalCode(req.getInternalCode() != null ? req.getInternalCode().trim() : null);
        product.setName(req.getName().trim());
        product.setShortDescription(req.getShortDescription() != null ? req.getShortDescription().trim() : null);
        product.setDescription(req.getDescription());
        product.setUnitPrice(req.getUnitPrice());
        product.setCostPrice(req.getCostPrice());
        product.setDiscountPrice(req.getDiscountPrice());
        product.setDiscountStartAt(req.getDiscountStartAt());
        product.setDiscountEndAt(req.getDiscountEndAt());
        product.setTaxRate(req.getTaxRate());
        product.setUnitOfMeasure(req.getUnitOfMeasure());
        product.setSellByWeight(req.getSellByWeight() != null ? req.getSellByWeight() : false);
        product.setTrackStock(req.getTrackStock() != null ? req.getTrackStock() : false);
        product.setStockQuantity(req.getStockQuantity());
        product.setMinStock(req.getMinStock());
        product.setAllowNegativeStock(req.getAllowNegativeStock() != null ? req.getAllowNegativeStock() : false);
        product.setCategoryId(req.getCategoryId());
        product.setBrand(req.getBrand() != null ? req.getBrand().trim() : null);
        product.setNcm(req.getNcm());
        product.setCest(req.getCest());
        product.setWeight(req.getWeight());
        product.setWidth(req.getWidth());
        product.setHeight(req.getHeight());
        product.setDepth(req.getDepth());
        product.setPreparationTimeMinutes(req.getPreparationTimeMinutes());
        product.setServeSize(req.getServeSize());
        product.setCalories(req.getCalories());
        product.setIngredients(req.getIngredients());
        product.setAllergens(req.getAllergens());
        product.setNutritionalInfo(req.getNutritionalInfo());
        product.setMinOrderQuantity(req.getMinOrderQuantity());
        product.setMaxOrderQuantity(req.getMaxOrderQuantity());
        product.setSellMultiple(req.getSellMultiple());
        product.setActive(req.getActive() != null ? req.getActive() : true);
        product.setAvailableForSale(req.getAvailableForSale() != null ? req.getAvailableForSale() : true);
        product.setAvailableForDelivery(req.getAvailableForDelivery() != null ? req.getAvailableForDelivery() : true);
        product.setFeatured(req.getFeatured() != null ? req.getFeatured() : false);
        product.setIsComposite(req.getIsComposite() != null ? req.getIsComposite() : false);
        product.setDisplayOrder(req.getDisplayOrder());
        product.setImageUrl(req.getImageUrl());
        product.setImageUrls(req.getImageUrls());
        product.setVideoUrl(req.getVideoUrl());
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .tenantId(p.getTenantId())
                .sku(p.getSku())
                .barcode(p.getBarcode())
                .internalCode(p.getInternalCode())
                .name(p.getName())
                .shortDescription(p.getShortDescription())
                .description(p.getDescription())
                .unitPrice(p.getUnitPrice())
                .costPrice(p.getCostPrice())
                .discountPrice(p.getDiscountPrice())
                .discountStartAt(p.getDiscountStartAt())
                .discountEndAt(p.getDiscountEndAt())
                .taxRate(p.getTaxRate())
                .unitOfMeasure(p.getUnitOfMeasure())
                .sellByWeight(p.getSellByWeight())
                .trackStock(p.getTrackStock())
                .stockQuantity(p.getStockQuantity())
                .minStock(p.getMinStock())
                .allowNegativeStock(p.getAllowNegativeStock())
                .categoryId(p.getCategoryId())
                .brand(p.getBrand())
                .ncm(p.getNcm())
                .cest(p.getCest())
                .weight(p.getWeight())
                .width(p.getWidth())
                .height(p.getHeight())
                .depth(p.getDepth())
                .preparationTimeMinutes(p.getPreparationTimeMinutes())
                .serveSize(p.getServeSize())
                .calories(p.getCalories())
                .ingredients(p.getIngredients())
                .allergens(p.getAllergens())
                .nutritionalInfo(p.getNutritionalInfo())
                .minOrderQuantity(p.getMinOrderQuantity())
                .maxOrderQuantity(p.getMaxOrderQuantity())
                .sellMultiple(p.getSellMultiple())
                .active(p.getActive())
                .availableForSale(p.getAvailableForSale())
                .availableForDelivery(p.getAvailableForDelivery())
                .featured(p.getFeatured())
                .isComposite(p.getIsComposite())
                .displayOrder(p.getDisplayOrder())
                .imageUrl(p.getImageUrl())
                .imageUrls(p.getImageUrls())
                .videoUrl(p.getVideoUrl())
                .version(p.getVersion())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
