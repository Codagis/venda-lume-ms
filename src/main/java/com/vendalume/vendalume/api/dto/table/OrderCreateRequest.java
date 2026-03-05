package com.vendalume.vendalume.api.dto.table;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    /** Obrigatório para root ao operar em outra empresa, ignorado para não-root. */
    private UUID tenantId;

    @NotNull(message = "ID da mesa é obrigatório")
    private UUID tableId;
}
