package com.vendalume.vendalume.api.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de requisição para criar fornecedor.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierCreateRequest {
    private UUID tenantId;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255)
    private String name;

    @Size(max = 20)
    private String document;

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

    private String notes;

    private Boolean active;

    @Size(max = 255)
    private String tradeName;

    @Size(max = 50)
    private String stateRegistration;

    @Size(max = 50)
    private String municipalRegistration;

    @Size(max = 255)
    private String contactName;

    @Size(max = 20)
    private String contactPhone;

    @Size(max = 255)
    private String contactEmail;

    @Size(max = 100)
    private String bankName;

    @Size(max = 20)
    private String bankAgency;

    @Size(max = 30)
    private String bankAccount;

    @Size(max = 100)
    private String bankPix;

    @Size(max = 255)
    private String paymentTerms;
}
