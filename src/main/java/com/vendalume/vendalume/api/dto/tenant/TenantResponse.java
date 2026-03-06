package com.vendalume.vendalume.api.dto.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resposta com dados do tenant.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {

    private UUID id;
    private String name;
    private String tradeName;
    private String document;
    private String email;
    private String phone;
    private String logoUrl;
    private Boolean active;
    private String addressStreet;
    private String addressNumber;
    private String addressComplement;
    private String addressNeighborhood;
    private String addressCity;
    private String addressState;
    private String addressZip;
    private String stateRegistration;
    private String municipalRegistration;
    private String codigoMunicipio;
    private Integer crt;
    private Integer idCsc;
    private String csc;
    private String ambienteFiscal;
    private String certificadoPfxUrl;
    private java.time.Instant certificadoUploadedAt;
    private Integer crtNfe;
    private String ambienteNfe;
    private String ecfSeries;
    private String ecfModel;
    private Boolean emitsFiscalReceipt;
    private Boolean emitsSimpleReceipt;
    private Integer maxInstallments;
    private Integer maxInstallmentsNoInterest;
    private java.math.BigDecimal interestRatePercent;
    private String cardFeeType;
    private java.math.BigDecimal cardFeeValue;
    private Instant createdAt;
    private Instant updatedAt;
}
