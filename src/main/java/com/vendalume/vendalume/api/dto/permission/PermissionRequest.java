package com.vendalume.vendalume.api.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequest {

    @NotBlank(message = "Código é obrigatório")
    @Size(max = 80)
    private String code;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150)
    private String name;

    @Size(max = 500)
    private String description;

    @Size(max = 50)
    private String module;
}
