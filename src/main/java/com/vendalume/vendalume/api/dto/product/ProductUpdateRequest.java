package com.vendalume.vendalume.api.dto.product;

import com.vendalume.vendalume.domain.enums.UnitOfMeasure;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de requisição para atualização de produto.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    @NotBlank(message = "SKU é obrigatório")
    @Size(max = 50)
    private String sku;

    @Size(max = 20)
    private String barcode;

    @Size(max = 50)
    private String internalCode;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255)
    private String name;

    @Size(max = 500)
    private String shortDescription;

    private String description;

    @NotNull(message = "Preço unitário é obrigatório")
    @DecimalMin(value = "0", inclusive = false, message = "Preço deve ser maior que zero")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal unitPrice;

    @DecimalMin(value = "0", message = "Preço de custo não pode ser negativo")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal costPrice;

    @DecimalMin(value = "0", message = "Preço promocional não pode ser negativo")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal discountPrice;

    private LocalDateTime discountStartAt;
    private LocalDateTime discountEndAt;

    @DecimalMin(value = "0", message = "Alíquota não pode ser negativa")
    @DecimalMax(value = "100", message = "Alíquota não pode ser maior que 100")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal taxRate;

    @NotNull(message = "Unidade de medida é obrigatória")
    private UnitOfMeasure unitOfMeasure;

    @Builder.Default
    private Boolean sellByWeight = false;

    @Builder.Default
    private Boolean trackStock = false;

    @DecimalMin(value = "0", message = "Quantidade em estoque não pode ser negativa")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal stockQuantity;

    @DecimalMin(value = "0", message = "Estoque mínimo não pode ser negativo")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal minStock;

    @Builder.Default
    private Boolean allowNegativeStock = false;

    private UUID categoryId;

    @Size(max = 100)
    private String brand;

    @Size(max = 10)
    private String ncm;

    @Size(max = 9)
    private String cest;

    @DecimalMin(value = "0", message = "Peso não pode ser negativo")
    @Digits(integer = 6, fraction = 4)
    private BigDecimal weight;

    @DecimalMin(value = "0", message = "Largura não pode ser negativa")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal width;

    @DecimalMin(value = "0", message = "Altura não pode ser negativa")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal height;

    @DecimalMin(value = "0", message = "Profundidade não pode ser negativa")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal depth;

    @Min(0)
    private Integer preparationTimeMinutes;

    @Size(max = 50)
    private String serveSize;

    @Min(0)
    private Integer calories;

    private String ingredients;

    @Size(max = 500)
    private String allergens;

    private String nutritionalInfo;

    @DecimalMin(value = "0", message = "Quantidade mínima não pode ser negativa")
    @Digits(integer = 6, fraction = 4)
    private BigDecimal minOrderQuantity;

    @DecimalMin(value = "0", message = "Quantidade máxima não pode ser negativa")
    @Digits(integer = 6, fraction = 4)
    private BigDecimal maxOrderQuantity;

    @DecimalMin(value = "0", message = "Múltiplo de venda não pode ser negativo")
    @Digits(integer = 6, fraction = 4)
    private BigDecimal sellMultiple;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Boolean availableForSale = true;

    @Builder.Default
    private Boolean availableForDelivery = true;

    @Builder.Default
    private Boolean featured = false;

    @Builder.Default
    private Boolean isComposite = false;

    @Min(0)
    private Integer displayOrder;

    @Size(max = 500)
    private String imageUrl;

    private String imageUrls;

    @Size(max = 500)
    private String videoUrl;
}
