package com.vendalume.vendalume.api.dto.module;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
