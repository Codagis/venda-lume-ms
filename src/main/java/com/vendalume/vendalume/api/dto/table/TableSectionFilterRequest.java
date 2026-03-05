package com.vendalume.vendalume.api.dto.table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableSectionFilterRequest {
    private UUID tenantId;
    private String search;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sortBy = "displayOrder";

    @Builder.Default
    private String sortDirection = "asc";
}
