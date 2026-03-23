package com.vendalume.vendalume.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Funcionário do tenant. Possui salário e dia de vencimento para geração
 * de contas a pagar recorrentes mensais (folha de pagamento).
 */
@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_employee_tenant_active", columnList = "tenant_id, active"),
    @Index(name = "idx_employee_tenant_name", columnList = "tenant_id, name"),
    @Index(name = "idx_employee_tenant_document", columnList = "tenant_id, document")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Employee extends BaseAuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("ID do tenant")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "document", length = 20)
    private String document;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "phone_alt", length = 20)
    private String phoneAlt;

    @Column(name = "address_street", length = 255)
    private String addressStreet;

    @Column(name = "address_number", length = 20)
    private String addressNumber;

    @Column(name = "address_complement", length = 100)
    private String addressComplement;

    @Column(name = "address_neighborhood", length = 100)
    private String addressNeighborhood;

    @Column(name = "address_city", length = 100)
    private String addressCity;

    @Column(name = "address_state", length = 2)
    private String addressState;

    @Column(name = "address_zip", length = 10)
    private String addressZip;

    @Column(name = "role", length = 100)
    private String role;

    @Comment("Salário mensal para geração de conta a pagar")
    @Column(name = "salary", nullable = false, precision = 19, scale = 4)
    private BigDecimal salary = BigDecimal.ZERO;

    @Comment("Dia do mês (1-28) para vencimento da conta a pagar")
    @Column(name = "payment_day", nullable = false)
    private Integer paymentDay = 5;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_agency", length = 20)
    private String bankAgency;

    @Column(name = "bank_account", length = 30)
    private String bankAccount;

    @Column(name = "bank_pix", length = 100)
    private String bankPix;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Comment("CBO - Classificação Brasileira de Ocupações")
    @Column(name = "cbo", length = 20)
    private String cbo;

    @Comment("Adicional de periculosidade (%)")
    @Column(name = "hazardous_pay_percent", precision = 5, scale = 2)
    private BigDecimal hazardousPayPercent;

    @Comment("Horas extraordinárias (50%)")
    @Column(name = "overtime_hours", precision = 8, scale = 2)
    private BigDecimal overtimeHours;

    @Column(name = "overtime_value", precision = 19, scale = 4)
    private BigDecimal overtimeValue;

    @Column(name = "dsr_value", precision = 19, scale = 4)
    private BigDecimal dsrValue;

    @Column(name = "health_plan_deduction", precision = 19, scale = 4)
    private BigDecimal healthPlanDeduction;

    @Column(name = "inss_percent", precision = 5, scale = 2)
    private BigDecimal inssPercent;

    @Column(name = "irrf_value", precision = 19, scale = 4)
    private BigDecimal irrfValue;

    @Comment("Número de dependentes para dedução na base de cálculo do IRRF")
    @Column(name = "dependentes", nullable = false)
    private Integer dependentes = 0;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Comment("Modalidade de contratação: CLT ou PJ")
    @Column(name = "contract_type", nullable = false, length = 10)
    private String contractType = "CLT";

    @Comment("Prestador PJ vinculado (quando contractType = PJ)")
    @Column(name = "contractor_id", columnDefinition = "UUID")
    private UUID contractorId;

    @PrePersist
    protected void generateId() {
        if (id == null) id = UUID.randomUUID();
    }
}
