package com.vendalume.vendalume.api.dto.module;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleRequest {

    @NotBlank(message = "Código é obrigatório")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    @Size(max = 50)
    private String icon;

    @NotBlank(message = "Rota é obrigatória")
    @Size(max = 100)
    private String route;

    @NotBlank(message = "Componente é obrigatório")
    @Size(max = 80)
    private String component;

    @NotNull
    private Integer displayOrder;

    @NotBlank(message = "Permissão de visualização é obrigatória")
    @Size(max = 80)
    private String viewPermissionCode;

    private Boolean active;
}
