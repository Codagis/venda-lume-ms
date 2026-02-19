package com.vendalume.vendalume.api.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de filtros para listagem e busca de produtos.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filtros para busca de produtos")
public class ProductFilterRequest {

    @Schema(description = "Busca por nome, SKU, código de barras ou marca")
    private String search;

    @Schema(description = "Filtrar apenas produtos ativos")
    private Boolean active;

    @Schema(description = "ID da categoria")
    private UUID categoryId;

    @Schema(description = "Filtrar apenas disponíveis para venda no PDV")
    private Boolean availableForSale;

    @Schema(description = "Filtrar apenas disponíveis para delivery")
    private Boolean availableForDelivery;

    @Schema(description = "Filtrar apenas produtos em destaque")
    private Boolean featured;

    @Schema(description = "Filtrar apenas produtos com estoque baixo (abaixo do mínimo)")
    private Boolean lowStock;

    @Schema(description = "Página (0-based)", example = "0")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "Quantidade por página", example = "20")
    @Builder.Default
    private Integer size = 20;

    @Schema(description = "Campo para ordenação", example = "name", allowableValues = {"name", "sku", "unitPrice", "displayOrder", "createdAt"})
    @Builder.Default
    private String sortBy = "name";

    @Schema(description = "Direção da ordenação", example = "asc", allowableValues = {"asc", "desc"})
    @Builder.Default
    private String sortDirection = "asc";
}
