package com.vendalume.vendalume.api.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resposta com dados do cliente.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String document;
    private String email;
    private String phone;
    private String phoneAlt;
    private String addressStreet;
    private String addressNumber;
    private String addressComplement;
    private String addressNeighborhood;
    private String addressCity;
    private String addressState;
    private String addressZip;
    private String notes;
    private Boolean active;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
