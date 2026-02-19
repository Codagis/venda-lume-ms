package com.vendalume.vendalume.api.dto.profile;

import com.vendalume.vendalume.api.dto.permission.PermissionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private Set<PermissionResponse> permissions;
    private Instant createdAt;
    private Instant updatedAt;
}
