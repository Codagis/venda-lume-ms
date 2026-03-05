package com.vendalume.vendalume.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Entidade que representa um fornecedor no sistema VendaLume.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-18
 */
@Entity
@Table(name = "suppliers", indexes = {
    @Index(name = "idx_supplier_tenant_active", columnList = "tenant_id, active"),
    @Index(name = "idx_supplier_tenant_name", columnList = "tenant_id, name"),
    @Index(name = "idx_supplier_tenant_document", columnList = "tenant_id, document")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Supplier extends BaseAuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    private UUID tenantId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "trade_name", length = 255)
    private String tradeName;

    @Column(name = "document", length = 20)
    private String document;

    @Column(name = "state_registration", length = 50)
    private String stateRegistration;

    @Column(name = "municipal_registration", length = 50)
    private String municipalRegistration;

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

    @Column(name = "contact_name", length = 255)
    private String contactName;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_agency", length = 20)
    private String bankAgency;

    @Column(name = "bank_account", length = 30)
    private String bankAccount;

    @Column(name = "bank_pix", length = 100)
    private String bankPix;

    @Column(name = "payment_terms", length = 255)
    private String paymentTerms;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @PrePersist
    protected void generateId() {
        if (id == null) id = UUID.randomUUID();
    }
}
