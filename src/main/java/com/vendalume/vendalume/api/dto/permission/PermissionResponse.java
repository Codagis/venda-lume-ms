package com.vendalume.vendalume.api.dto.permission;

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
public class PermissionResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private String module;
    private Instant createdAt;
    private Instant updatedAt;
}
