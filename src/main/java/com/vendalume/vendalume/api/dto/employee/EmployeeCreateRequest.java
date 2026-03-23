package com.vendalume.vendalume.api.dto.employee;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateRequest {

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

    @Size(max = 100)
    private String role;

    @Size(max = 20)
    private String cbo;

    @NotNull(message = "Salário é obrigatório")
    @DecimalMin(value = "0", message = "Salário não pode ser negativo")
    private BigDecimal salary;

    @NotNull(message = "Dia de vencimento é obrigatório")
    @Min(value = 1, message = "Dia deve ser entre 1 e 28")
    @Max(value = 28, message = "Dia deve ser entre 1 e 28")
    private Integer paymentDay;

    @Size(max = 100)
    private String bankName;

    @Size(max = 20)
    private String bankAgency;

    @Size(max = 30)
    private String bankAccount;

    @Size(max = 100)
    private String bankPix;

    private LocalDate hireDate;

    private String notes;

    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    private BigDecimal hazardousPayPercent;

    private BigDecimal overtimeHours;
    private BigDecimal overtimeValue;
    private BigDecimal dsrValue;
    private BigDecimal healthPlanDeduction;
    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    private BigDecimal inssPercent;
    private BigDecimal irrfValue;

    @Min(0)
    private Integer dependentes;

    private Boolean active;

    @Size(max = 10)
    private String contractType;

    private UUID contractorId;
}
