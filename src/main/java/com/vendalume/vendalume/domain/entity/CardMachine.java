package com.vendalume.vendalume.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidade que representa CardMachine no sistema VendaLume.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Entity
@Table(name = "card_machines", indexes = {
        @Index(name = "idx_card_machine_tenant", columnList = "tenant_id"),
        @Index(name = "idx_card_machine_tenant_active", columnList = "tenant_id, active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

public class CardMachine extends BaseAuditableEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("ID da empresa")
    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Comment("Nome da maquininha (ex: Cielo, Rede, Mercado Pago)")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Comment("PERCENTAGE ou FIXED_AMOUNT")
    @Column(name = "fee_type", nullable = false, length = 20)
    private String feeType;

    @Comment("Valor da taxa: percentual ou reais")
    @Column(name = "fee_value", nullable = false, precision = 10, scale = 4)
    private BigDecimal feeValue;

    @Comment("CNPJ da adquirente (instituição de pagamento) para NFC-e quando pagamento for cartão")
    @Column(name = "acquirer_cnpj", length = 14)
    private String acquirerCnpj;

    @Comment("Maquininha padrão")
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Comment("Ativa")
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Comment("Quantidade máxima de parcelas para cartão de crédito")
    @Column(name = "max_installments")
    private Integer maxInstallments;

    @Comment("Quantidade de parcelas sem juros")
    @Column(name = "max_installments_no_interest")
    private Integer maxInstallmentsNoInterest;

    @Comment("Taxa de juros ao mês (%) para parcelas com juros")
    @Column(name = "interest_rate_percent", precision = 8, scale = 4)
    private BigDecimal interestRatePercent;

    @jakarta.persistence.PrePersist
    protected void generateId() {
        if (id == null) id = UUID.randomUUID();
    }
}
