package com.vendalume.vendalume.api.dto.tenant;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String tradeName;

    @Size(max = 20)
    private String document;

    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 500)
    private String logoUrl;

    private Boolean active;

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

    @Size(max = 20)
    private String stateRegistration;

    @Size(max = 20)
    private String municipalRegistration;

    /** Código IBGE do município (7 dígitos) - ex: 2304400 Fortaleza/CE - obrigatório para emissão NFC-e Fiscal Simplify */
    @Size(max = 7)
    private String codigoMunicipio;

    /** CRT - Código de Regime Tributário: 1=Simples Nacional, 2=Excesso sublimite, 3=Regime Normal, 4=MEI */
    private Integer crt;

    /** ID do CSC (geralmente 0) */
    private Integer idCsc;

    /** CSC - Código Segurança Contribuinte obtido na SEFAZ do estado */
    @Size(max = 100)
    private String csc;

    /** Ambiente: homologacao ou producao */
    @Size(max = 20)
    private String ambienteFiscal;

    /** CRT para NF-e na Nuvem Fiscal (1 a 4). Se não informado, usa o mesmo da NFC-e. */
    private Integer crtNfe;

    /** Ambiente NF-e na Nuvem Fiscal: homologacao ou producao. Se não informado, usa o mesmo da NFC-e. */
    @Size(max = 20)
    private String ambienteNfe;

    /** Certificado PFX em Base64 - enviado ao Fiscal Simplify, não armazenado */
    private String certificadoPfxBase64;

    /** Senha do certificado PFX */
    @Size(max = 100)
    private String certificadoPassword;

    @Size(max = 50)
    private String ecfSeries;

    @Size(max = 100)
    private String ecfModel;

    private Boolean emitsFiscalReceipt;

    private Boolean emitsSimpleReceipt;

    private Integer maxInstallments;

    private Integer maxInstallmentsNoInterest;

    private java.math.BigDecimal interestRatePercent;

    /** PERCENTAGE ou FIXED_AMOUNT */
    @Size(max = 20)
    private String cardFeeType;

    /** Valor da taxa: % ou R$ conforme cardFeeType */
    private java.math.BigDecimal cardFeeValue;

    @AssertTrue(message = "Quando emite cupom fiscal, informe pelo menos IE ou IM (o outro pode ser ISENTO)")
    public boolean isFiscalReceiptIeImValid() {
        if (!Boolean.TRUE.equals(emitsFiscalReceipt)) return true;
        boolean ieOk = stateRegistration != null && !stateRegistration.isBlank();
        boolean imOk = municipalRegistration != null && !municipalRegistration.isBlank();
        return ieOk || imOk;
    }
}
