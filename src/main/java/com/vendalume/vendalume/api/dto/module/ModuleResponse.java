package com.vendalume.vendalume.api.dto.module;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de resposta com dados do módulo.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private String route;
    private String component;
    private Integer displayOrder;
    private String viewPermissionCode;
    private Boolean active;
}
