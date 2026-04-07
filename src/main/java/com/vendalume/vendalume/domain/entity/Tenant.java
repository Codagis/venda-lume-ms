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

import java.util.UUID;

/**
 * Entidade que representa Tenant no sistema VendaLume.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenant_active", columnList = "active"),
        @Index(name = "idx_tenant_document", columnList = "document"),
        @Index(name = "idx_tenant_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

public class Tenant extends BaseAuditableEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Comment("Razão social")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Comment("Nome fantasia")
    @Column(name = "trade_name", length = 255)
    private String tradeName;

    @Comment("CNPJ ou documento da empresa")
    @Column(name = "document", length = 20)
    private String document;

    @Comment("E-mail de contato")
    @Column(name = "email", length = 255)
    private String email;

    @Comment("Telefone de contato")
    @Column(name = "phone", length = 20)
    private String phone;

    @Comment("Empresa ativa no sistema")
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Comment("URL da logo da empresa (ex.: Google Cloud Storage)")
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Comment("Logradouro do endereço")
    @Column(name = "address_street", length = 255)
    private String addressStreet;

    @Comment("Número do endereço")
    @Column(name = "address_number", length = 20)
    private String addressNumber;

    @Comment("Complemento do endereço")
    @Column(name = "address_complement", length = 100)
    private String addressComplement;

    @Comment("Bairro")
    @Column(name = "address_neighborhood", length = 100)
    private String addressNeighborhood;

    @Comment("Cidade")
    @Column(name = "address_city", length = 100)
    private String addressCity;

    @Comment("Estado/UF")
    @Column(name = "address_state", length = 2)
    private String addressState;

    @Comment("CEP")
    @Column(name = "address_zip", length = 10)
    private String addressZip;

    @Comment("Inscrição estadual (IE)")
    @Column(name = "state_registration", length = 20)
    private String stateRegistration;

    @Comment("Inscrição municipal (IM)")
    @Column(name = "municipal_registration", length = 20)
    private String municipalRegistration;

    @Comment("Código do município IBGE (7 dígitos) - ex: 2304400 Fortaleza/CE - necessário para emissão NFC-e via Fiscal Simplify")
    @Column(name = "codigo_municipio", length = 7)
    private String codigoMunicipio;

    @Comment("CRT - Código de Regime Tributário (1=Simples Nacional, 2=Excesso sublimite, 3=Regime Normal, 4=MEI)")
    @Column(name = "crt")
    private Integer crt;

    @Comment("ID do CSC (Código Segurança Contribuinte) - geralmente 0")
    @Column(name = "id_csc")
    private Integer idCsc;

    @Comment("CSC - Código obtido na SEFAZ do estado para emissão NFC-e")
    @Column(name = "csc", length = 100)
    private String csc;

    @Comment("Ambiente Fiscal Simplify: homologacao ou producao")
    @Column(name = "ambiente_fiscal", length = 20)
    private String ambienteFiscal;

    @Comment("CRT para NF-e na Nuvem Fiscal (1 a 4). Se null, usa o mesmo da NFC-e.")
    @Column(name = "crt_nfe")
    private Integer crtNfe;

    @Comment("Ambiente NF-e na Nuvem Fiscal: homologacao ou producao. Se null, usa o mesmo da NFC-e.")
    @Column(name = "ambiente_nfe", length = 20)
    private String ambienteNfe;

    @Comment("URL do certificado PFX no Google Cloud Storage (quando enviado)")
    @Column(name = "certificado_pfx_url", length = 500)
    private String certificadoPfxUrl;

    @Comment("Data/hora do último upload do certificado PFX")
    @Column(name = "certificado_uploaded_at")
    private java.time.Instant certificadoUploadedAt;

    @Comment("Número série do equipamento fiscal (ECF)")
    @Column(name = "ecf_series", length = 50)
    private String ecfSeries;

    @Comment("Modelo do equipamento fiscal")
    @Column(name = "ecf_model", length = 100)
    private String ecfModel;

    @Comment("Emite cupom fiscal (requer IE ou IM preenchido)")
    @Column(name = "emits_fiscal_receipt", nullable = false)
    private Boolean emitsFiscalReceipt = false;

    @Comment("Emite comprovante simples de venda")
    @Column(name = "emits_simple_receipt", nullable = false)
    private Boolean emitsSimpleReceipt = true;

    @Comment("Quantidade máxima de parcelas no cartão de crédito")
    @Column(name = "max_installments", nullable = false)
    private Integer maxInstallments = 12;

    @Comment("Quantidade máxima de parcelas sem juros")
    @Column(name = "max_installments_no_interest", nullable = false)
    private Integer maxInstallmentsNoInterest = 1;

    @Comment("Percentual de juros por parcela (ex: 2.99)")
    @Column(name = "interest_rate_percent", nullable = false, precision = 5, scale = 2)
    private java.math.BigDecimal interestRatePercent = java.math.BigDecimal.ZERO;

    @Comment("PERCENTAGE ou FIXED_AMOUNT - tipo da taxa da maquininha")
    @Column(name = "card_fee_type", length = 20)
    private String cardFeeType;

    @Comment("Valor da taxa: percentual (ex: 2.5) ou reais (ex: 0.50)")
    @Column(name = "card_fee_value", precision = 10, scale = 4)
    private java.math.BigDecimal cardFeeValue;

    @jakarta.persistence.PrePersist
    protected void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
