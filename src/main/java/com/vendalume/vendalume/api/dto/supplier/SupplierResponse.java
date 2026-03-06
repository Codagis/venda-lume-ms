package com.vendalume.vendalume.api.dto.supplier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resposta com dados do fornecedor.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String tradeName;
    private String document;
    private String stateRegistration;
    private String municipalRegistration;
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
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String bankName;
    private String bankAgency;
    private String bankAccount;
    private String bankPix;
    private String paymentTerms;
    private String notes;
    private Boolean active;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
