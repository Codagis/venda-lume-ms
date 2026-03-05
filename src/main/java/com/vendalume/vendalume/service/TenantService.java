package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.tenant.TenantRequest;
import com.vendalume.vendalume.api.dto.tenant.TenantResponse;
import com.vendalume.vendalume.domain.entity.Tenant;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.repository.TenantRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final FiscalSimplifyService fiscalSimplifyService;
    private final Optional<GcsStorageService> gcsStorageService;

    private void requireRoot() {
        if (!SecurityUtils.isCurrentUserRoot()) {
            throw new IllegalStateException("Acesso negado. Apenas usuário root pode gerenciar empresas.");
        }
    }

    @Transactional(readOnly = true)
    public List<TenantResponse> listAll() {
        requireRoot();
        return tenantRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TenantResponse findById(UUID id) {
        requireRoot();
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", id));
        return toResponse(tenant);
    }

    @Transactional(readOnly = true)
    public TenantResponse getCurrentUserTenant() {
        java.util.UUID tenantId = SecurityUtils.requireTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", tenantId));
        return toResponse(tenant);
    }

    @Transactional
    public TenantResponse create(TenantRequest request) {
        requireRoot();
        if (request.getDocument() != null && !request.getDocument().isBlank()
                && tenantRepository.existsByDocument(request.getDocument().trim())) {
            throw new IllegalArgumentException("Documento já cadastrado: " + request.getDocument());
        }
        Tenant tenant = Tenant.builder()
                .name(request.getName().trim())
                .tradeName(request.getTradeName() != null ? request.getTradeName().trim() : null)
                .document(request.getDocument() != null ? request.getDocument().trim() : null)
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .logoUrl(request.getLogoUrl() != null ? request.getLogoUrl().trim() : null)
                .active(request.getActive() != null ? request.getActive() : true)
                .addressStreet(request.getAddressStreet() != null ? request.getAddressStreet().trim() : null)
                .addressNumber(request.getAddressNumber() != null ? request.getAddressNumber().trim() : null)
                .addressComplement(request.getAddressComplement() != null ? request.getAddressComplement().trim() : null)
                .addressNeighborhood(request.getAddressNeighborhood() != null ? request.getAddressNeighborhood().trim() : null)
                .addressCity(request.getAddressCity() != null ? request.getAddressCity().trim() : null)
                .addressState(request.getAddressState() != null ? request.getAddressState().trim().toUpperCase() : null)
                .addressZip(request.getAddressZip() != null ? request.getAddressZip().trim() : null)
                .stateRegistration(request.getStateRegistration() != null ? request.getStateRegistration().trim() : null)
                .municipalRegistration(request.getMunicipalRegistration() != null ? request.getMunicipalRegistration().trim() : null)
                .codigoMunicipio(request.getCodigoMunicipio() != null && !request.getCodigoMunicipio().isBlank()
                ? request.getCodigoMunicipio().trim().replaceAll("\\D", "") : null)
                .crt(request.getCrt())
                .idCsc(request.getIdCsc() != null ? request.getIdCsc() : 0)
                .csc(request.getCsc() != null ? request.getCsc().trim() : null)
                .ambienteFiscal(request.getAmbienteFiscal() != null ? request.getAmbienteFiscal().trim() : "homologacao")
                .crtNfe(request.getCrtNfe())
                .ambienteNfe(request.getAmbienteNfe() != null && !request.getAmbienteNfe().isBlank() ? request.getAmbienteNfe().trim() : null)
                .ecfSeries(request.getEcfSeries() != null ? request.getEcfSeries().trim() : null)
                .ecfModel(request.getEcfModel() != null ? request.getEcfModel().trim() : null)
                .emitsFiscalReceipt(Boolean.TRUE.equals(request.getEmitsFiscalReceipt()))
                .emitsSimpleReceipt(request.getEmitsSimpleReceipt() != null ? request.getEmitsSimpleReceipt() : true)
                .maxInstallments(request.getMaxInstallments() != null ? request.getMaxInstallments() : 12)
                .maxInstallmentsNoInterest(request.getMaxInstallmentsNoInterest() != null ? request.getMaxInstallmentsNoInterest() : 1)
                .interestRatePercent(request.getInterestRatePercent() != null ? request.getInterestRatePercent() : java.math.BigDecimal.ZERO)
                .cardFeeType(request.getCardFeeType() != null && !request.getCardFeeType().isBlank() ? request.getCardFeeType().trim().toUpperCase() : null)
                .cardFeeValue(request.getCardFeeValue())
                .build();
        tenant = tenantRepository.save(tenant);
        uploadCertificadoSeEnviado(tenant, request.getCertificadoPfxBase64());
        try {
            fiscalSimplifyService.syncTenantToFiscalSimplify(tenant,
                    request.getCertificadoPfxBase64(), request.getCertificadoPassword());
        } catch (Exception e) {
            // Log mas não falha o cadastro - empresa salva no Venda Lume
        }
        return toResponse(tenant);
    }

    @Transactional
    public TenantResponse update(UUID id, TenantRequest request) {
        requireRoot();
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", id));
        if (request.getDocument() != null && !request.getDocument().isBlank()
                && !request.getDocument().trim().equals(tenant.getDocument())
                && tenantRepository.existsByDocument(request.getDocument().trim())) {
            throw new IllegalArgumentException("Documento já cadastrado: " + request.getDocument());
        }
        tenant.setName(request.getName().trim());
        tenant.setTradeName(request.getTradeName() != null ? request.getTradeName().trim() : null);
        tenant.setDocument(request.getDocument() != null ? request.getDocument().trim() : null);
        tenant.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        tenant.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        tenant.setLogoUrl(request.getLogoUrl() != null ? request.getLogoUrl().trim() : null);
        tenant.setAddressStreet(request.getAddressStreet() != null ? request.getAddressStreet().trim() : null);
        tenant.setAddressNumber(request.getAddressNumber() != null ? request.getAddressNumber().trim() : null);
        tenant.setAddressComplement(request.getAddressComplement() != null ? request.getAddressComplement().trim() : null);
        tenant.setAddressNeighborhood(request.getAddressNeighborhood() != null ? request.getAddressNeighborhood().trim() : null);
        tenant.setAddressCity(request.getAddressCity() != null ? request.getAddressCity().trim() : null);
        tenant.setAddressState(request.getAddressState() != null ? request.getAddressState().trim().toUpperCase() : null);
        tenant.setAddressZip(request.getAddressZip() != null ? request.getAddressZip().trim() : null);
        tenant.setStateRegistration(request.getStateRegistration() != null ? request.getStateRegistration().trim() : null);
        tenant.setMunicipalRegistration(request.getMunicipalRegistration() != null ? request.getMunicipalRegistration().trim() : null);
        if (request.getCodigoMunicipio() != null) {
            tenant.setCodigoMunicipio(request.getCodigoMunicipio().trim().replaceAll("\\D", ""));
        }
        if (request.getCrt() != null) tenant.setCrt(request.getCrt());
        if (request.getIdCsc() != null) tenant.setIdCsc(request.getIdCsc());
        if (request.getCsc() != null && !request.getCsc().isBlank()) tenant.setCsc(request.getCsc().trim());
        if (request.getAmbienteFiscal() != null) tenant.setAmbienteFiscal(request.getAmbienteFiscal().trim());
        tenant.setCrtNfe(request.getCrtNfe());
        tenant.setAmbienteNfe(request.getAmbienteNfe() != null && !request.getAmbienteNfe().isBlank() ? request.getAmbienteNfe().trim() : null);
        tenant.setEcfSeries(request.getEcfSeries() != null ? request.getEcfSeries().trim() : null);
        tenant.setEcfModel(request.getEcfModel() != null ? request.getEcfModel().trim() : null);
        tenant.setEmitsFiscalReceipt(Boolean.TRUE.equals(request.getEmitsFiscalReceipt()));
        if (request.getEmitsSimpleReceipt() != null) tenant.setEmitsSimpleReceipt(request.getEmitsSimpleReceipt());
        if (request.getMaxInstallments() != null) tenant.setMaxInstallments(request.getMaxInstallments());
        if (request.getMaxInstallmentsNoInterest() != null) tenant.setMaxInstallmentsNoInterest(request.getMaxInstallmentsNoInterest());
        if (request.getInterestRatePercent() != null) tenant.setInterestRatePercent(request.getInterestRatePercent());
        if (request.getCardFeeType() != null) {
            String v = request.getCardFeeType().trim();
            tenant.setCardFeeType(v.isEmpty() ? null : v.toUpperCase());
            if (v.isEmpty()) tenant.setCardFeeValue(null);
        }
        if (request.getCardFeeValue() != null && tenant.getCardFeeType() != null) tenant.setCardFeeValue(request.getCardFeeValue());
        if (request.getActive() != null) {
            tenant.setActive(request.getActive());
        }
        tenant = tenantRepository.save(tenant);
        uploadCertificadoSeEnviado(tenant, request.getCertificadoPfxBase64());
        try {
            fiscalSimplifyService.syncTenantToFiscalSimplify(tenant,
                    request.getCertificadoPfxBase64(), request.getCertificadoPassword());
        } catch (Exception e) {
            // Log mas não falha a atualização
        }
        return toResponse(tenant);
    }

    private void uploadCertificadoSeEnviado(Tenant tenant, String certificadoPfxBase64) {
        if (certificadoPfxBase64 == null || certificadoPfxBase64.isBlank() || gcsStorageService.isEmpty()) {
            return;
        }
        try {
            String base64Limpo = certificadoPfxBase64.replaceAll("^data:[^;]+;base64,", "").trim();
            byte[] bytes = Base64.getDecoder().decode(base64Limpo.getBytes(StandardCharsets.UTF_8));
            if (bytes == null || bytes.length == 0) return;
            String url = gcsStorageService.get().uploadCertificate(tenant.getId(), bytes);
            tenant.setCertificadoPfxUrl(url);
            tenant.setCertificadoUploadedAt(Instant.now());
            tenantRepository.save(tenant);
        } catch (Exception e) {
            // Log mas não falha o cadastro/edição
            org.slf4j.LoggerFactory.getLogger(TenantService.class).warn("Falha ao enviar certificado ao Google Cloud: {}", e.getMessage());
        }
    }

    @Transactional
    public void delete(UUID id) {
        requireRoot();
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", id));
        tenantRepository.delete(tenant);
    }

    private TenantResponse toResponse(Tenant t) {
        return TenantResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .tradeName(t.getTradeName())
                .document(t.getDocument())
                .email(t.getEmail())
                .phone(t.getPhone())
                .logoUrl(t.getLogoUrl())
                .active(t.getActive())
                .addressStreet(t.getAddressStreet())
                .addressNumber(t.getAddressNumber())
                .addressComplement(t.getAddressComplement())
                .addressNeighborhood(t.getAddressNeighborhood())
                .addressCity(t.getAddressCity())
                .addressState(t.getAddressState())
                .addressZip(t.getAddressZip())
                .stateRegistration(t.getStateRegistration())
                .municipalRegistration(t.getMunicipalRegistration())
                .codigoMunicipio(t.getCodigoMunicipio())
                .crt(t.getCrt())
                .idCsc(t.getIdCsc())
                .csc(t.getCsc())
                .ambienteFiscal(t.getAmbienteFiscal())
                .certificadoPfxUrl(t.getCertificadoPfxUrl())
                .certificadoUploadedAt(t.getCertificadoUploadedAt())
                .crtNfe(t.getCrtNfe())
                .ambienteNfe(t.getAmbienteNfe())
                .ecfSeries(t.getEcfSeries())
                .ecfModel(t.getEcfModel())
                .emitsFiscalReceipt(t.getEmitsFiscalReceipt())
                .emitsSimpleReceipt(t.getEmitsSimpleReceipt())
                .maxInstallments(t.getMaxInstallments())
                .maxInstallmentsNoInterest(t.getMaxInstallmentsNoInterest())
                .interestRatePercent(t.getInterestRatePercent())
                .cardFeeType(t.getCardFeeType())
                .cardFeeValue(t.getCardFeeValue())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
