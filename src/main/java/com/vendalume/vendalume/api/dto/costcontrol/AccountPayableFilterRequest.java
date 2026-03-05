package com.vendalume.vendalume.api.dto.costcontrol;

import com.vendalume.vendalume.domain.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountPayableFilterRequest {

    private UUID tenantId;
    private String search;
    private AccountStatus status;
    private UUID supplierId;
    private LocalDate dueDateFrom;
    private LocalDate dueDateTo;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sortBy = "dueDate";

    @Builder.Default
    private String sortDirection = "asc";
}
