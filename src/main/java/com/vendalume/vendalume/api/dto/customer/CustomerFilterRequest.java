package com.vendalume.vendalume.api.dto.customer;

import lombok.*;

import java.util.UUID;

/**
 * DTO de requisição para filtrar clientes.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFilterRequest {

    private UUID tenantId;
    private String search;
    private Boolean active;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sortBy = "name";

    @Builder.Default
    private String sortDirection = "asc";
}
