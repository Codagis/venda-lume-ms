package com.commo.commo.api.dto.product;

import com.commo.commo.domain.enums.UnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de resposta com dados do produto.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private UUID id;
    private UUID tenantId;
    private String sku;
    private String barcode;
    private String internalCode;
    private String name;
    private String shortDescription;
    private String description;
    private BigDecimal unitPrice;
    private BigDecimal costPrice;
    private BigDecimal discountPrice;
    private LocalDateTime discountStartAt;
    private LocalDateTime discountEndAt;
    private BigDecimal taxRate;
    private UnitOfMeasure unitOfMeasure;
    private Boolean sellByWeight;
    private Boolean trackStock;
    private BigDecimal stockQuantity;
    private BigDecimal minStock;
    private Boolean allowNegativeStock;
    private UUID categoryId;
    private String brand;
    private String ncm;
    private String cest;
    private BigDecimal weight;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal depth;
    private Integer preparationTimeMinutes;
    private String serveSize;
    private Integer calories;
    private String ingredients;
    private String allergens;
    private String nutritionalInfo;
    private BigDecimal minOrderQuantity;
    private BigDecimal maxOrderQuantity;
    private BigDecimal sellMultiple;
    private Boolean active;
    private Boolean availableForSale;
    private Boolean availableForDelivery;
    private Boolean featured;
    private Boolean isComposite;
    private Integer displayOrder;
    private String imageUrl;
    private String imageUrls;
    private String videoUrl;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
