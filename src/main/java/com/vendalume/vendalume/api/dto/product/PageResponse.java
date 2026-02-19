package com.vendalume.vendalume.api.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de resposta paginada genérico.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta paginada")
public class PageResponse<T> {

    @Schema(description = "Lista de itens da página")
    private List<T> content;

    @Schema(description = "Página atual (0-based)")
    private int page;

    @Schema(description = "Tamanho da página")
    private int size;

    @Schema(description = "Total de elementos")
    private long totalElements;

    @Schema(description = "Total de páginas")
    private int totalPages;

    @Schema(description = "Indica se é a primeira página")
    private boolean first;

    @Schema(description = "Indica se é a última página")
    private boolean last;
}
