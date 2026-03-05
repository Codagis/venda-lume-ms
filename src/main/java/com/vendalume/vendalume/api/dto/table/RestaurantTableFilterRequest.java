package com.vendalume.vendalume.api.dto.table;

import com.vendalume.vendalume.domain.enums.TableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTableFilterRequest {
    private UUID tenantId;
    private UUID sectionId;
    private TableStatus status;
    private Boolean active;
    private String search;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sortBy = "name";

    @Builder.Default
    private String sortDirection = "asc";
}
