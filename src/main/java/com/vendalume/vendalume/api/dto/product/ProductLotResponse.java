package com.vendalume.vendalume.api.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductLotResponse {

    private UUID id;
    private String lotCode;
    private LocalDate expiresAt;
    private BigDecimal quantity;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}

