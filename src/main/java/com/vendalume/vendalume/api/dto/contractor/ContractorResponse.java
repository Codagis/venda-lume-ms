package com.vendalume.vendalume.api.dto.contractor;

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
public class ContractorResponse {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String tradeName;
    private String cnpj;
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
    private String bankName;
    private String bankAgency;
    private String bankAccount;
    private String bankPix;
    private String notes;
    private Boolean active;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
