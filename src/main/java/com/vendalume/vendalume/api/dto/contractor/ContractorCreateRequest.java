package com.vendalume.vendalume.api.dto.contractor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractorCreateRequest {

    private UUID tenantId;

    @NotBlank(message = "Razão social é obrigatória")
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String tradeName;

    @Size(max = 18)
    private String cnpj;

    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 20)
    private String phoneAlt;

    @Size(max = 255)
    private String addressStreet;

    @Size(max = 20)
    private String addressNumber;

    @Size(max = 100)
    private String addressComplement;

    @Size(max = 100)
    private String addressNeighborhood;

    @Size(max = 100)
    private String addressCity;

    @Size(max = 2)
    private String addressState;

    @Size(max = 10)
    private String addressZip;

    @Size(max = 100)
    private String bankName;

    @Size(max = 20)
    private String bankAgency;

    @Size(max = 30)
    private String bankAccount;

    @Size(max = 100)
    private String bankPix;

    private String notes;

    private Boolean active;
}
