package com.vendalume.vendalume.api.dto.customer;

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
public class CustomerCreateRequest {
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
}
