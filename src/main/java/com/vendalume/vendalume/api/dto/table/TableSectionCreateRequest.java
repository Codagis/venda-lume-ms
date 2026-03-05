package com.vendalume.vendalume.api.dto.table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableSectionCreateRequest {
    private UUID tenantId;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private Integer displayOrder;
}
