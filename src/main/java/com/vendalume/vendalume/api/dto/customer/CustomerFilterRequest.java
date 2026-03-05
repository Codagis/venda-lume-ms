package com.vendalume.vendalume.api.dto.customer;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFilterRequest {

    private UUID tenantId;
    private String search;
    private Boolean active;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sortBy = "name";

    @Builder.Default
    private String sortDirection = "asc";
}
