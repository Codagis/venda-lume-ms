package com.vendalume.vendalume.api.dto.costcontrol;

import com.vendalume.vendalume.domain.enums.CostCategoryKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Objeto de transferência (DTO) CostAccountCategoryRequest.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostAccountCategoryRequest {

    /** Obrigatório para usuário root ao criar. */
    private UUID tenantId;

    @NotNull(message = "Tipo é obrigatório")
    private CostCategoryKind kind;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120)
    private String name;

    @Size(max = 2000)
    private String description;

    private Boolean active;

    private Integer displayOrder;
}
