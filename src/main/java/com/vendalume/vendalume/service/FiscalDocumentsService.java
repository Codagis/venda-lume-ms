package com.vendalume.vendalume.service;

import com.vendalume.vendalume.domain.entity.Tenant;
import com.vendalume.vendalume.integration.fiscalsimplify.FiscalSimplifyClient;
import com.vendalume.vendalume.repository.TenantRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Serviço de negócio FiscalDocumentsService.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class FiscalDocumentsService {

    private final TenantRepository tenantRepository;
    private final FiscalSimplifyClient fiscalSimplifyClient;

    @Transactional(readOnly = true)
    public Map<String, Object> listarNfeEmitidas(
            UUID tenantId,
            Integer top,
            Integer skip,
            Boolean inlinecount,
            String referencia,
            String chave,
            String serie
    ) {
        Tenant tenant = resolveTenant(tenantId);
        String cnpj = requireTenantCnpj(tenant);
        String ambiente = resolveAmbienteNfe(tenant);
        return fiscalSimplifyClient.listarNfeEmitidas(cnpj, ambiente, top, skip, inlinecount, referencia, chave, serie);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listarNfeRecebidas(
            UUID tenantId,
            Integer top,
            Integer skip,
            Boolean inlinecount,
            Integer distNsu,
            String formaDistribuicao,
            String chaveAcesso
    ) {
        Tenant tenant = resolveTenant(tenantId);
        String cnpj = requireTenantCnpj(tenant);
        String ambiente = resolveAmbienteNfe(tenant);
        log.info("Fiscal: listar NF-e recebidas tenantId={} cnpj={} ambiente={} top={} skip={}",
                tenant.getId(), cnpj, ambiente, top, skip);
        return fiscalSimplifyClient.listarNfeRecebidas(cnpj, ambiente, top, skip, inlinecount, distNsu, formaDistribuicao, chaveAcesso);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detalharNfeRecebida(UUID tenantId, String docId) {
        Tenant tenant = resolveTenant(tenantId);
        requireTenantCnpj(tenant); // valida tenant
        return fiscalSimplifyClient.getNfeReceivedById(docId);
    }

    @Transactional(readOnly = true)
    public byte[] baixarPdfNfeRecebida(UUID tenantId, String docId) {
        Tenant tenant = resolveTenant(tenantId);
        requireTenantCnpj(tenant);
        return fiscalSimplifyClient.getNfeReceivedPdf(docId);
    }

    @Transactional(readOnly = true)
    public byte[] baixarXmlNfeRecebida(UUID tenantId, String docId) {
        Tenant tenant = resolveTenant(tenantId);
        requireTenantCnpj(tenant);
        return fiscalSimplifyClient.getNfeReceivedXml(docId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detalharNfeEmitida(UUID tenantId, String id) {
        Tenant tenant = resolveTenant(tenantId);
        requireTenantCnpj(tenant);
        return fiscalSimplifyClient.getNfeById(id);
    }

    @Transactional(readOnly = true)
    public byte[] baixarXmlNfeEmitida(UUID tenantId, String id) {
        Tenant tenant = resolveTenant(tenantId);
        requireTenantCnpj(tenant);
        return fiscalSimplifyClient.getNfeXml(id);
    }

    @Transactional(readOnly = true)
    public byte[] baixarPdfNfeEmitida(UUID tenantId, String id) {
        Tenant tenant = resolveTenant(tenantId);
        requireTenantCnpj(tenant);
        return fiscalSimplifyClient.getNfePdf(id);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detalharNfceEmitida(UUID tenantId, String id) {
        Tenant tenant = resolveTenant(tenantId);
        requireTenantCnpj(tenant);
        return fiscalSimplifyClient.getNfceById(id);
    }

    @Transactional(readOnly = true)
    public byte[] baixarXmlNfceEmitida(UUID tenantId, String id) {
        Tenant tenant = resolveTenant(tenantId);
        requireTenantCnpj(tenant);
        return fiscalSimplifyClient.getNfceXml(id);
    }

    @Transactional(readOnly = true)
    public byte[] baixarPdfNfceEmitida(UUID tenantId, String id) {
        Tenant tenant = resolveTenant(tenantId);
        requireTenantCnpj(tenant);
        return fiscalSimplifyClient.getNfcePdf(id);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listarNfceEmitidas(
            UUID tenantId,
            Integer top,
            Integer skip,
            Boolean inlinecount,
            String referencia,
            String chave,
            String serie
    ) {
        Tenant tenant = resolveTenant(tenantId);
        String cnpj = requireTenantCnpj(tenant);
        String ambiente = resolveAmbienteFiscal(tenant);
        return fiscalSimplifyClient.listarNfceEmitidas(cnpj, ambiente, top, skip, inlinecount, referencia, chave, serie);
    }

    /**
     * Retorna um "mix" de NF-e emitidas (empresa como emitente) e recebidas (empresa como destinatária/interessada).
     * Observação: os datasets vêm de endpoints diferentes na Nuvem Fiscal, então a paginação é aproximada:
     * buscamos ambos e retornamos um slice (skip/top) sobre a lista combinada.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> listarNfeTodas(
            UUID tenantId,
            Integer top,
            Integer skip,
            Boolean inlinecount,
            String referencia,
            String chave,
            String serie,
            Integer distNsu,
            String formaDistribuicao,
            String chaveAcesso
    ) {
        int effectiveTop = (top != null && top > 0 && top <= 100) ? top : 20;
        int effectiveSkip = (skip != null && skip >= 0) ? skip : 0;

        Map<String, Object> issued = listarNfeEmitidas(tenantId, 100, 0, inlinecount, referencia, chave, serie);
        Map<String, Object> received = listarNfeRecebidas(tenantId, 100, 0, inlinecount, distNsu, formaDistribuicao, chaveAcesso);

        List<Map<String, Object>> combined = new ArrayList<>();
        combined.addAll(normalizeList(issued, "ISSUED"));
        combined.addAll(normalizeList(received, "RECEIVED"));

        int from = Math.min(effectiveSkip, combined.size());
        int to = Math.min(from + effectiveTop, combined.size());
        List<Map<String, Object>> page = combined.subList(from, to);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("data", page);
        if (Boolean.TRUE.equals(inlinecount)) {
            out.put("@count", combined.size());
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> normalizeList(Map<String, Object> raw, String direction) {
        if (raw == null) return List.of();
        Object dataObj = raw.get("data");
        if (!(dataObj instanceof List<?> dataList)) return List.of();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object o : dataList) {
            if (o instanceof Map<?, ?> m) {
                Map<String, Object> copy = new LinkedHashMap<>();
                for (Map.Entry<?, ?> e : m.entrySet()) {
                    if (e.getKey() != null) copy.put(e.getKey().toString(), e.getValue());
                }
                copy.put("direction", direction);
                out.add(copy);
            }
        }
        return out;
    }

    private Tenant resolveTenant(UUID tenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            UUID id = tenantId != null ? tenantId : SecurityUtils.requireTenantId();
            return tenantRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada."));
        }
        UUID currentTenantId = SecurityUtils.requireTenantId();
        return tenantRepository.findById(currentTenantId).orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada."));
    }

    private String requireTenantCnpj(Tenant tenant) {
        if (tenant == null) {
            throw new IllegalArgumentException("Empresa não encontrada.");
        }
        if (tenant.getDocument() == null || tenant.getDocument().isBlank()) {
            throw new IllegalArgumentException("Empresa sem CNPJ cadastrado (tenantId=" + tenant.getId() + ").");
        }
        String cnpj = tenant.getDocument().replaceAll("\\D", "");
        if (cnpj.length() != 14) throw new IllegalArgumentException("CNPJ da empresa inválido.");
        return cnpj;
    }

    private String resolveAmbienteNfe(Tenant tenant) {
        if (tenant == null) return "homologacao";
        String amb = tenant.getAmbienteNfe();
        if (amb == null || amb.isBlank()) amb = tenant.getAmbienteFiscal();
        return (amb != null && amb.equalsIgnoreCase("producao")) ? "producao" : "homologacao";
    }

    private String resolveAmbienteFiscal(Tenant tenant) {
        if (tenant == null) return "homologacao";
        String amb = tenant.getAmbienteFiscal();
        return (amb != null && amb.equalsIgnoreCase("producao")) ? "producao" : "homologacao";
    }
}

