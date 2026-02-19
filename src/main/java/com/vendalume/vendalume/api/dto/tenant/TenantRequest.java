package com.vendalume.vendalume.api.dto.tenant;

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
public class TenantRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String tradeName;

    @Size(max = 20)
    private String document;

    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    private Boolean active;
}
