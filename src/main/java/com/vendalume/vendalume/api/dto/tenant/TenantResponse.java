package com.vendalume.vendalume.api.dto.tenant;

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
public class TenantResponse {

    private UUID id;
    private String name;
    private String tradeName;
    private String document;
    private String email;
    private String phone;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
