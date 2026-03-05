package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.dto.product.ProductFilterRequest;
import com.vendalume.vendalume.api.dto.product.ProductResponse;
import com.vendalume.vendalume.api.dto.stock.StockFilterRequest;
import com.vendalume.vendalume.api.dto.stock.StockMovementRequest;
import com.vendalume.vendalume.api.dto.stock.StockMovementResponse;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.domain.entity.Product;
import com.vendalume.vendalume.domain.entity.StockMovement;
import com.vendalume.vendalume.domain.entity.User;
import com.vendalume.vendalume.domain.enums.StockMovementType;
import com.vendalume.vendalume.repository.ProductRepository;
import com.vendalume.vendalume.repository.StockMovementRepository;
import com.vendalume.vendalume.repository.UserRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;

    @Transactional
    public StockMovementResponse registerMovement(UUID tenantIdParam, StockMovementRequest request) {
        UUID tenantId = resolveTenantId(tenantIdParam);
        Product product = productRepository.findByIdAndTenantId(request.getProductId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", request.getProductId()));

        if (!Boolean.TRUE.equals(product.getTrackStock())) {
            throw new IllegalArgumentException("Produto não possui controle de estoque ativo.");
        }

        BigDecimal qty = request.getQuantity();
        if (qty == null || qty.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Quantidade deve ser diferente de zero.");
        }

        BigDecimal delta;
        StockMovementType type = request.getMovementType();
        switch (type) {
            case MANUAL_ENTRY -> delta = qty.abs();
            case MANUAL_EXIT, SALE -> delta = qty.abs().negate();
            case ADJUSTMENT -> delta = qty;
            default -> delta = qty;
        }
        BigDecimal before = product.getStockQuantity() != null ? product.getStockQuantity() : BigDecimal.ZERO;
        BigDecimal after = before.add(delta);

        if (after.compareTo(BigDecimal.ZERO) < 0 && !Boolean.TRUE.equals(product.getAllowNegativeStock())) {
            throw new IllegalArgumentException("Estoque não pode ficar negativo. Quantidade atual: " + before);
        }

        product.setStockQuantity(after);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .id(UUID.randomUUID())
                .productId(product.getId())
                .tenantId(tenantId)
                .movementType(type)
                .quantityDelta(delta)
                .quantityBefore(before)
                .quantityAfter(after)
                .notes(request.getNotes())
                .build();
        movement.setCreatedBy(SecurityUtils.getCurrentUserId());
        movement.setUpdatedBy(SecurityUtils.getCurrentUserId());
        movement = stockMovementRepository.save(movement);

        return toMovementResponse(movement, product.getName(), product.getSku());
    }

    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> listMovements(UUID tenantIdParam, StockFilterRequest filter) {
        UUID tenantId = resolveTenantId(tenantIdParam);
        Pageable pageable = PageRequest.of(
                filter.getPage() != null ? filter.getPage() : 0,
                Math.min(filter.getSize() != null ? filter.getSize() : 20, 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<StockMovement> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (filter.getProductId() != null) {
                predicates.add(cb.equal(root.get("productId"), filter.getProductId()));
            }
            if (filter.getMovementType() != null) {
                predicates.add(cb.equal(root.get("movementType"), filter.getMovementType()));
            }
            if (filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate().atZone(java.time.ZoneId.systemDefault()).toInstant()));
            }
            if (filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate().toLocalDate().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<StockMovement> page = stockMovementRepository.findAll(spec, pageable);
        List<StockMovementResponse> content = page.getContent().stream()
                .map(m -> {
                    Product p = productRepository.findById(m.getProductId()).orElse(null);
                    return toMovementResponse(m, p != null ? p.getName() : null, p != null ? p.getSku() : null);
                })
                .toList();

        return PageResponse.<StockMovementResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> listMovementsByProduct(UUID tenantId, UUID productId, int limit) {
        Pageable pageable = PageRequest.of(0, Math.min(limit, 50), Sort.by(Sort.Direction.DESC, "createdAt"));
        List<StockMovement> list = stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
        Product product = productRepository.findById(productId).orElse(null);
        String name = product != null ? product.getName() : null;
        String sku = product != null ? product.getSku() : null;
        return list.stream().map(m -> toMovementResponse(m, name, sku)).toList();
    }

    public void recordSaleMovement(UUID tenantId, UUID productId, BigDecimal quantityDelta, UUID saleId, String saleNumber) {
        Product product = productRepository.findByIdAndTenantId(productId, tenantId).orElse(null);
        if (product == null || !Boolean.TRUE.equals(product.getTrackStock())) return;

        BigDecimal before = product.getStockQuantity() != null ? product.getStockQuantity() : BigDecimal.ZERO;
        BigDecimal after = before.add(quantityDelta);

        product.setStockQuantity(after);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .tenantId(tenantId)
                .movementType(StockMovementType.SALE)
                .quantityDelta(quantityDelta)
                .quantityBefore(before)
                .quantityAfter(after)
                .saleId(saleId)
                .saleNumber(saleNumber)
                .build();
        movement.setCreatedBy(SecurityUtils.getCurrentUserId());
        movement.setUpdatedBy(SecurityUtils.getCurrentUserId());
        movement.setCreatedAt(Instant.now());
        movement.setUpdatedAt(Instant.now());
        movement.setVersion(0L);
        stockMovementRepository.save(movement);
    }

    private StockMovementResponse toMovementResponse(StockMovement m, String productName, String productSku) {
        String createdByName = null;
        if (m.getCreatedBy() != null) {
            createdByName = userRepository.findById(m.getCreatedBy()).map(User::getFullName).orElse(null);
        }
        return StockMovementResponse.builder()
                .id(m.getId())
                .productId(m.getProductId())
                .productName(productName)
                .productSku(productSku)
                .movementType(m.getMovementType())
                .movementTypeLabel(m.getMovementType().getLabel())
                .quantityDelta(m.getQuantityDelta())
                .quantityBefore(m.getQuantityBefore())
                .quantityAfter(m.getQuantityAfter())
                .saleId(m.getSaleId())
                .saleNumber(m.getSaleNumber())
                .notes(m.getNotes())
                .createdByName(createdByName)
                .createdAt(m.getCreatedAt())
                .build();
    }

    private UUID resolveTenantId(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot() && requestTenantId != null) {
            return requestTenantId;
        }
        return SecurityUtils.requireTenantId();
    }
}
