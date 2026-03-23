package com.vendalume.vendalume.api.dto.employee;

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
public class EmployeeResponse {

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
    private String role;
    private String cbo;
    private BigDecimal salary;
    private Integer paymentDay;
    private String bankName;
    private String bankAgency;
    private String bankAccount;
    private String bankPix;
    private LocalDate hireDate;
    private String notes;
    private BigDecimal hazardousPayPercent;
    private BigDecimal overtimeHours;
    private BigDecimal overtimeValue;
    private BigDecimal dsrValue;
    private BigDecimal healthPlanDeduction;
    private BigDecimal inssPercent;
    private BigDecimal irrfValue;
    private Integer dependentes;
    private Boolean active;
    private String contractType;
    private UUID contractorId;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
