package com.vendalume.vendalume.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vendalume.vendalume.api.dto.costcontrol.AccountPayableCreateRequest;
import com.vendalume.vendalume.api.dto.costcontrol.AccountPayableResponse;
import com.vendalume.vendalume.api.dto.sale.SaleCreateRequest;
import com.vendalume.vendalume.api.dto.sale.SaleItemRequest;
import com.vendalume.vendalume.api.dto.sale.SaleResponse;
import com.vendalume.vendalume.domain.entity.Product;
import com.vendalume.vendalume.domain.entity.Tenant;
import com.vendalume.vendalume.domain.enums.SaleStatus;
import com.vendalume.vendalume.domain.enums.SaleType;
import com.vendalume.vendalume.repository.ProductRepository;
import com.vendalume.vendalume.repository.TenantRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.XMLConstants;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleInvoiceImportService {

    private static final String IMPORT_PRODUCT_SKU = "IMPORT_NFE";

    private final SaleService saleService;
    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final Optional<GcsStorageService> gcsStorageService;
    private final com.vendalume.vendalume.integration.fiscalsimplify.FiscalSimplifyClient fiscalSimplifyClient;
    private final com.vendalume.vendalume.repository.SaleRepository saleRepository;
    private final CostControlService costControlService;

    @Transactional
    public Map<String, Object> importSaleFromInvoice(
            UUID tenantId,
            SaleType saleType,
            MultipartFile pdf,
            MultipartFile xml,
            MultipartFile json,
            String jsonText,
            String notes
    ) {
        UUID resolvedTenantId = resolveTenantId(tenantId);
        Tenant tenant = tenantRepository.findById(resolvedTenantId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada."));

        if (pdf != null && !pdf.isEmpty()) {
            throw new IllegalArgumentException("Importação por PDF não é suportada. Envie XML ou JSON.");
        }
        if ((xml == null || xml.isEmpty()) && (json == null || json.isEmpty()) && (jsonText == null || jsonText.isBlank())) {
            throw new IllegalArgumentException("Envie o XML ou o JSON da NF-e para importar a venda.");
        }

        InvoiceExtract extract = extractInvoice(xml, json, jsonText);
        if (extract.total == null || extract.total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Não foi possível identificar o total da nota (vNF). Envie XML/JSON válido.");
        }

        String tenantDoc = cleanDigits(tenant.getDocument());
        boolean hasXml = xml != null && !xml.isEmpty();
        String emitDoc = hasXml ? extractEmitCnpjFromXml(xml) : null;
        String destDoc = hasXml ? extractDestDocFromXml(xml) : null;
        boolean isIssuedByTenant = hasXml && tenantDoc != null && emitDoc != null && tenantDoc.equals(emitDoc);
        boolean isReceivedByTenant = hasXml && tenantDoc != null && destDoc != null && tenantDoc.equals(destDoc) && (emitDoc == null || !tenantDoc.equals(emitDoc));

        SaleResponse sale = null;
        AccountPayableResponse payable = null;
        Map<String, Object> nfeEmission = null;

        if (isReceivedByTenant) {
            String emitName = extractEmitNameFromXml(xml);
            LocalDate issueDate = extractIssueDateFromXml(xml);
            AccountPayableCreateRequest apReq = AccountPayableCreateRequest.builder()
                    .tenantId(SecurityUtils.isCurrentUserRoot() ? resolvedTenantId : null)
                    .description(buildPayableDescription(extract, emitName))
                    .reference(extract.chave != null ? extract.chave : extract.numero)
                    .category("NF-E")
                    .dueDate(issueDate != null ? issueDate : LocalDate.now())
                    .amount(extract.total)
                    .notes(buildNotes(notes, extract))
                    .build();
            payable = costControlService.createPayable(apReq);
        } else {
            // Default: cria venda (para XML emitida pelo tenant ou import por JSON).
            Product importProduct = ensureImportProduct(resolvedTenantId);

            // Garante que o total da venda bata com o total da nota no MVP (1 item).
            importProduct.setUnitPrice(extract.total);
            importProduct.setUpdatedBy(SecurityUtils.getCurrentUserId());
            productRepository.save(importProduct);

            SaleCreateRequest req = SaleCreateRequest.builder()
                    .tenantId(SecurityUtils.isCurrentUserRoot() ? resolvedTenantId : null)
                    .saleType(saleType != null ? saleType : SaleType.PDV)
                    .status(SaleStatus.OPEN) // evita exigir paymentMethod na importação
                    .items(java.util.List.of(SaleItemRequest.builder()
                            .productId(importProduct.getId())
                            .quantity(BigDecimal.ONE)
                            .build()))
                    .customerName(extract.customerName)
                    .customerDocument(extract.customerDocument)
                    .notes(buildNotes(notes, extract))
                    .build();

            sale = saleService.create(req);

            if (isIssuedByTenant) {
                nfeEmission = fiscalSimplifyClient.importarNfeXml(getBytes(xml));
                tryUpdateSaleInvoiceFields(sale.getId(), nfeEmission);
            } else if (hasXml) {
                log.info("Importação NF-e: XML não é emitida pelo tenant (emitente={} tenant={} destinatario={}). Criada como venda sem emissão.", emitDoc, tenantDoc, destDoc);
            }
        }

        Map<String, Object> files = new LinkedHashMap<>();
        if (gcsStorageService.isPresent()) {
            // Por enquanto, anexos só ficam vinculados à venda (quando existir).
            if (sale != null) {
                tryUpload(files, resolvedTenantId, sale.getId(), "pdf", pdf);
                tryUpload(files, resolvedTenantId, sale.getId(), "xml", xml);
                tryUpload(files, resolvedTenantId, sale.getId(), "json", json);
            }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sale", sale);
        out.put("payable", payable);
        out.put("createdType", payable != null ? "PAYABLE" : "SALE");
        out.put("invoice", Map.of(
                "total", extract.total,
                "numero", extract.numero,
                "chave", extract.chave,
                "customerName", extract.customerName,
                "customerDocument", extract.customerDocument
        ));
        if (nfeEmission != null) out.put("nfeEmission", nfeEmission);
        out.put("files", files);
        return out;
    }

    private byte[] getBytes(MultipartFile f) {
        try {
            return f.getBytes();
        } catch (Exception e) {
            throw new IllegalArgumentException("Falha ao ler arquivo: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void tryUpdateSaleInvoiceFields(UUID saleId, Map<String, Object> payload) {
        if (saleId == null || payload == null) return;
        Object emissionResult = payload.get("emissionResult");
        if (!(emissionResult instanceof Map<?, ?> m)) return;
        String docId = m.get("id") != null ? m.get("id").toString() : null;
        String chave = m.get("chave") != null ? m.get("chave").toString() : null;
        String numero = m.get("numero") != null ? m.get("numero").toString() : (m.get("nfe_numero") != null ? m.get("nfe_numero").toString() : null);
        saleRepository.findById(saleId).ifPresent(s -> {
            if (docId != null && !docId.isBlank()) s.setInvoiceDocumentId(docId);
            if (chave != null && !chave.isBlank()) s.setInvoiceKey(chave);
            if (numero != null && !numero.isBlank()) s.setInvoiceNumber(numero);
            s.setUpdatedBy(SecurityUtils.getCurrentUserId());
            saleRepository.save(s);
        });
    }

    private void tryUpload(Map<String, Object> files, UUID tenantId, UUID saleId, String kind, MultipartFile file) {
        if (file == null || file.isEmpty()) return;
        try {
            String url = gcsStorageService.get().uploadSaleImport(tenantId, saleId, kind, file);
            files.put(kind, Map.of("url", url, "originalName", file.getOriginalFilename(), "contentType", file.getContentType()));
        } catch (Exception e) {
            log.warn("Falha ao enviar {} ao GCS: {}", kind, e.getMessage());
        }
    }

    private UUID resolveTenantId(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            if (requestTenantId == null) {
                throw new IllegalArgumentException("tenantId é obrigatório para usuário root.");
            }
            return requestTenantId;
        }
        return SecurityUtils.requireTenantId();
    }

    private Product ensureImportProduct(UUID tenantId) {
        return productRepository.findByTenantIdAndSku(tenantId, IMPORT_PRODUCT_SKU)
                .orElseGet(() -> {
                    Product p = new Product();
                    p.setId(UUID.randomUUID());
                    p.setTenantId(tenantId);
                    p.setSku(IMPORT_PRODUCT_SKU);
                    p.setName("Venda importada de NF-e");
                    p.setUnitPrice(BigDecimal.ONE); // o preço real é resolvido via SaleService; mantemos válido
                    p.setActive(true);
                    p.setAvailableForSale(true);
                    p.setAvailableForDelivery(false);
                    p.setTrackStock(false);
                    p.setDeductStockOnSale(false);
                    p.setEmitsNfce(false);
                    p.setEmitsNfe(false);
                    p.setCreatedBy(SecurityUtils.getCurrentUserId());
                    p.setUpdatedBy(SecurityUtils.getCurrentUserId());
                    return productRepository.save(p);
                });
    }

    private String buildNotes(String notes, InvoiceExtract ex) {
        StringBuilder sb = new StringBuilder();
        if (notes != null && !notes.isBlank()) sb.append(notes.trim()).append("\n");
        sb.append("[IMPORTAÇÃO NF] ");
        if (ex.numero != null) sb.append("nNF=").append(ex.numero).append(" ");
        if (ex.chave != null) sb.append("chNFe=").append(ex.chave).append(" ");
        if (ex.total != null) sb.append("vNF=").append(ex.total).append(" ");
        return sb.toString().trim();
    }

    private InvoiceExtract extractInvoice(MultipartFile xml, MultipartFile json, String jsonText) {
        // prioridade: XML (mais padronizado)
        if (xml != null && !xml.isEmpty()) {
            try {
                Document doc = xmlDoc(xml);
                doc.getDocumentElement().normalize();
                String vnf = firstText(doc, "vNF");
                String nnf = firstText(doc, "nNF");
                String ch = firstText(doc, "chNFe");
                if (ch == null) {
                    // tenta atributo Id="NFe<chave>"
                    NodeList inf = doc.getElementsByTagNameNS("*", "infNFe");
                    if (inf.getLength() > 0 && inf.item(0).getAttributes() != null && inf.item(0).getAttributes().getNamedItem("Id") != null) {
                        String id = inf.item(0).getAttributes().getNamedItem("Id").getNodeValue();
                        if (id != null && id.startsWith("NFe")) ch = id.substring(3);
                    }
                }
                String customerName = firstText(doc, "xNome"); // dest/emit também tem xNome; ok para MVP
                String cnpj = firstText(doc, "CNPJ");
                String cpf = firstText(doc, "CPF");
                return new InvoiceExtract(
                        parseMoney(vnf),
                        nnf,
                        cleanDigits(ch),
                        customerName,
                        cleanDigits(cnpj != null ? cnpj : cpf)
                );
            } catch (Exception e) {
                log.warn("Falha ao extrair XML: {}", e.getMessage());
            }
        }

        String jsonRaw = null;
        try {
            if (json != null && !json.isEmpty()) {
                jsonRaw = new String(json.getBytes(), StandardCharsets.UTF_8);
            } else if (jsonText != null && !jsonText.isBlank()) {
                jsonRaw = jsonText;
            }
            if (jsonRaw != null && !jsonRaw.isBlank()) {
                Map<String, Object> m = objectMapper.readValue(jsonRaw, new TypeReference<>() {});
                Object totalObj = m.get("total");
                if (totalObj == null) totalObj = m.get("vNF");
                BigDecimal total = parseMoney(totalObj != null ? totalObj.toString() : null);
                String numero = Optional.ofNullable(m.get("numero")).map(Object::toString).orElse(null);
                String chave = Optional.ofNullable(m.get("chave")).map(Object::toString).orElse(null);
                String customerName = Optional.ofNullable(m.get("customerName")).map(Object::toString).orElse(null);
                String customerDocument = Optional.ofNullable(m.get("customerDocument")).map(Object::toString).orElse(null);
                return new InvoiceExtract(total, numero, cleanDigits(chave), customerName, cleanDigits(customerDocument));
            }
        } catch (Exception e) {
            log.warn("Falha ao extrair JSON: {}", e.getMessage());
        }

        return new InvoiceExtract(null, null, null, null, null);
    }

    private static String firstText(Document doc, String tag) {
        NodeList list = doc.getElementsByTagNameNS("*", tag);
        if (list.getLength() == 0) return null;
        String v = list.item(0).getTextContent();
        return v != null ? v.trim() : null;
    }

    private static Document xmlDoc(MultipartFile xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            try {
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (Exception ignored) {
                // ok
            }
            return factory.newDocumentBuilder().parse(xml.getInputStream());
        } catch (Exception e) {
            throw new IllegalArgumentException("Falha ao ler XML: " + e.getMessage(), e);
        }
    }

    private String extractDestDocFromXml(MultipartFile xml) {
        try {
            Document doc = xmlDoc(xml);
            NodeList destList = doc.getElementsByTagNameNS("*", "dest");
            if (destList.getLength() == 0) return null;
            if (!(destList.item(0) instanceof Element destEl)) return null;
            NodeList cnpjList = destEl.getElementsByTagNameNS("*", "CNPJ");
            if (cnpjList.getLength() > 0) return cleanDigits(cnpjList.item(0).getTextContent());
            NodeList cpfList = destEl.getElementsByTagNameNS("*", "CPF");
            if (cpfList.getLength() > 0) return cleanDigits(cpfList.item(0).getTextContent());
            return null;
        } catch (Exception e) {
            log.warn("Falha ao extrair dest/CNPJ|CPF do XML: {}", e.getMessage());
            return null;
        }
    }

    private String extractEmitCnpjFromXml(MultipartFile xml) {
        try {
            Document doc = xmlDoc(xml);
            NodeList emitList = doc.getElementsByTagNameNS("*", "emit");
            if (emitList.getLength() == 0) return null;
            if (!(emitList.item(0) instanceof Element emitEl)) return null;
            NodeList cnpjList = emitEl.getElementsByTagNameNS("*", "CNPJ");
            if (cnpjList.getLength() == 0) return null;
            String cnpj = cnpjList.item(0).getTextContent();
            return cleanDigits(cnpj);
        } catch (Exception e) {
            log.warn("Falha ao extrair emit/CNPJ do XML: {}", e.getMessage());
            return null;
        }
    }

    private String extractEmitNameFromXml(MultipartFile xml) {
        try {
            Document doc = xmlDoc(xml);
            NodeList emitList = doc.getElementsByTagNameNS("*", "emit");
            if (emitList.getLength() == 0) return null;
            if (!(emitList.item(0) instanceof Element emitEl)) return null;
            NodeList nameList = emitEl.getElementsByTagNameNS("*", "xNome");
            if (nameList.getLength() == 0) return null;
            String v = nameList.item(0).getTextContent();
            return v != null ? v.trim() : null;
        } catch (Exception e) {
            log.warn("Falha ao extrair emit/xNome do XML: {}", e.getMessage());
            return null;
        }
    }

    private LocalDate extractIssueDateFromXml(MultipartFile xml) {
        try {
            Document doc = xmlDoc(xml);
            String dhEmi = firstText(doc, "dhEmi");
            if (dhEmi == null || dhEmi.isBlank()) return null;
            // Formato esperado: 2026-03-30T11:09:22-03:00 -> pega apenas a data
            String datePart = dhEmi.trim();
            int t = datePart.indexOf('T');
            if (t > 0) datePart = datePart.substring(0, t);
            return LocalDate.parse(datePart);
        } catch (Exception e) {
            log.warn("Falha ao extrair ide/dhEmi do XML: {}", e.getMessage());
            return null;
        }
    }

    private String buildPayableDescription(InvoiceExtract ex, String emitName) {
        StringBuilder sb = new StringBuilder();
        sb.append("NF-e recebida");
        if (ex.numero != null && !ex.numero.isBlank()) sb.append(" nNF=").append(ex.numero);
        if (emitName != null && !emitName.isBlank()) sb.append(" - ").append(emitName.trim());
        return sb.toString();
    }

    private static BigDecimal parseMoney(String s) {
        if (s == null || s.isBlank()) return null;
        String v = s.trim().replace(",", ".");
        try {
            return new BigDecimal(v);
        } catch (Exception e) {
            return null;
        }
    }

    private static String cleanDigits(String s) {
        if (s == null) return null;
        String d = s.replaceAll("\\D", "");
        return d.isBlank() ? null : d;
    }

    private record InvoiceExtract(BigDecimal total, String numero, String chave, String customerName, String customerDocument) {}
}

