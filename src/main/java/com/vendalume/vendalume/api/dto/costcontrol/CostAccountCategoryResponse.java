package com.vendalume.vendalume.api.dto.costcontrol;

import com.vendalume.vendalume.domain.enums.CostCategoryKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostAccountCategoryResponse {

    private UUID id;
    private UUID tenantId;
    private CostCategoryKind kind;
    private String name;
    private String description;
    private Boolean active;
    private Integer displayOrder;
    private Instant createdAt;
    private Instant updatedAt;
}
