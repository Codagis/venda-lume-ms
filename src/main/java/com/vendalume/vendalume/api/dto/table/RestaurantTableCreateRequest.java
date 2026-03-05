package com.vendalume.vendalume.api.dto.table;

import com.vendalume.vendalume.domain.enums.TableStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RestaurantTableCreateRequest {
    private UUID tenantId;

    @NotNull(message = "Seção é obrigatória")
    private UUID sectionId;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 50)
    private String name;

    @NotNull(message = "Capacidade é obrigatória")
    @Min(value = 1, message = "Capacidade deve ser pelo menos 1")
    private Integer capacity;

    private TableStatus status;
    private Boolean active;
    private Integer positionX;
    private Integer positionY;
}
