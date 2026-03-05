package com.vendalume.vendalume.api.dto.table;

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
public class TableSectionUpdateRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private Integer displayOrder;
}
