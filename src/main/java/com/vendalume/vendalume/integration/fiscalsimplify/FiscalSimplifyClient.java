package com.vendalume.vendalume.integration.fiscalsimplify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;

import java.util.Map;

/**
 * Cliente HTTP para a API Fiscal Simplify: empresas (emitentes), NFC-e, NF-e, certificados
 * e obtenção de PDF do cupom/documento fiscal.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FiscalSimplifyClient {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final WebClient.Builder webClientBuilder;

    @Value("${vendalume.fiscal-simplify.base-url:http://localhost:8081}")
    private String baseUrl;

    @Value("${vendalume.fiscal-simplify.enabled:true}")
    private boolean enabled;

    @Value("${vendalume.fiscal-simplify.api-key:}")
    private String apiKey;

    private WebClient client() {
        var builder = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader(API_KEY_HEADER, apiKey.trim());
        }
        return builder.build();
    }

    public boolean companyExistsByCnpj(String cnpj) {
        if (!enabled) return false;
        String cnpjLimpo = cnpj != null ? cnpj.replaceAll("\\D", "") : "";
        if (cnpjLimpo.length() != 14) return false;
        try {
            client().get()
                    .uri("/companies/cnpj/{cnpj}", cnpjLimpo)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (WebClientResponseException.NotFound e) {
            return false;
        } catch (Exception e) {
            log.warn("Erro ao verificar empresa no Fiscal Simplify: {}", e.getMessage());
            return false;
        }
    }

    public String createCompany(Map<String, Object> companyRequest) {
        if (!enabled) {
            log.debug("Fiscal Simplify desabilitado - não cadastrando empresa");
            return null;
        }
        if (baseUrl == null || baseUrl.isBlank() || "http://localhost:8081".equalsIgnoreCase(baseUrl.trim())) {
            throw new IllegalStateException(
                    "FISCAL_SIMPLIFY_BASE_URL não configurado para o ambiente atual. " +
                    "Valor atual: '" + baseUrl + "'. Configure a URL pública do FiscalSimplify.");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "FISCAL_SIMPLIFY_API_KEY não configurado. A API FiscalSimplify exige X-API-Key.");
        }
        try {
            Map<?, ?> result = client().post()
                    .uri("/companies")
                    .bodyValue(companyRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (result != null && result.containsKey("id")) {
                Object id = result.get("id");
                return id != null ? id.toString() : "created";
            }
            return "created";
        } catch (WebClientResponseException.BadRequest | WebClientResponseException.Conflict e) {
            log.warn("Fiscal Simplify: empresa já existe ou dados inválidos - {}", e.getResponseBodyAsString());
            return null;
        } catch (WebClientResponseException e) {
            log.error("Erro HTTP ao cadastrar empresa no Fiscal Simplify: status={} body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(
                    "Falha ao integrar empresa com Fiscal Simplify. HTTP " + e.getStatusCode() +
                    " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao cadastrar empresa no Fiscal Simplify", e);
            Throwable root = Exceptions.unwrap(e);
            String rootMessage = root != null && root.getMessage() != null ? root.getMessage() : e.getMessage();
            throw new RuntimeException("Falha ao integrar empresa com Fiscal Simplify: " + rootMessage, e);
        }
    }

    public void configurarNfce(String cnpj, Map<String, Object> nfcConfig) {
        if (!enabled) return;
        String cnpjLimpo = cnpj != null ? cnpj.replaceAll("\\D", "") : "";
        if (cnpjLimpo.length() != 14) return;
        try {
            client().put()
                    .uri("/companies/{cnpj}/nfce/config", cnpjLimpo)
                    .bodyValue(nfcConfig)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("NFC-e configurada no Fiscal Simplify: CNPJ {}", cnpjLimpo);
        } catch (Exception e) {
            log.warn("Erro ao configurar NFC-e no Fiscal Simplify: {}", e.getMessage());
            throw new RuntimeException("Falha ao configurar NFC-e: " + e.getMessage(), e);
        }
    }

    public void configurarNfe(String cnpj, Map<String, Object> nfeConfig) {
        if (!enabled) return;
        String cnpjLimpo = cnpj != null ? cnpj.replaceAll("\\D", "") : "";
        if (cnpjLimpo.length() != 14) return;
        try {
            client().put()
                    .uri("/companies/{cnpj}/nfe/config", cnpjLimpo)
                    .bodyValue(nfeConfig)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("NF-e configurada no Fiscal Simplify: CNPJ {}", cnpjLimpo);
        } catch (Exception e) {
            log.warn("Erro ao configurar NF-e no Fiscal Simplify: {}", e.getMessage());
            throw new RuntimeException("Falha ao configurar NF-e: " + e.getMessage(), e);
        }
    }

    public void cadastrarCertificado(String cnpj, String certificadoBase64, String password) {
        if (!enabled) return;
        String cnpjLimpo = cnpj != null ? cnpj.replaceAll("\\D", "") : "";
        if (cnpjLimpo.length() != 14) return;
        try {
            Map<String, Object> body = Map.of(
                    "certificado", certificadoBase64,
                    "password", password
            );
            client().put()
                    .uri("/companies/{cnpj}/certificado", cnpjLimpo)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Certificado cadastrado no Fiscal Simplify: CNPJ {}", cnpjLimpo);
        } catch (Exception e) {
            log.error("Erro ao cadastrar certificado no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao cadastrar certificado: " + e.getMessage(), e);
        }
    }

    public void updateCompany(String cnpj, Map<String, Object> updateRequest) {
        if (!enabled) return;
        String cnpjLimpo = cnpj != null ? cnpj.replaceAll("\\D", "") : "";
        if (cnpjLimpo.length() != 14) return;
        try {
            client().patch()
                    .uri("/companies/{cnpj}", cnpjLimpo)
                    .bodyValue(updateRequest)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.warn("Erro ao atualizar empresa no Fiscal Simplify: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> emitirNfce(Map<String, Object> nfceRequest) {
        if (!enabled) {
            throw new IllegalStateException("Fiscal Simplify está desabilitado.");
        }
        try {
            Map<String, Object> result = client().post()
                    .uri("/nfce")
                    .bodyValue(nfceRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return result != null ? result : Map.of();
        } catch (WebClientResponseException e) {
            log.error("Fiscal Simplify erro ao emitir NFC-e: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao emitir cupom fiscal: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao emitir NFC-e no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao emitir cupom fiscal: " + e.getMessage(), e);
        }
    }

    public byte[] getNfcePdf(String nfceId) {
        if (!enabled) {
            throw new IllegalStateException("Fiscal Simplify está desabilitado.");
        }
        try {
            byte[] pdf = client().get()
                    .uri("/nfce/{id}/pdf?download=false", nfceId)
                    .accept(MediaType.APPLICATION_PDF)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
            return pdf != null ? pdf : new byte[0];
        } catch (WebClientResponseException e) {
            log.error("Fiscal Simplify erro ao obter PDF NFC-e: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao obter PDF do cupom fiscal: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao obter PDF NFC-e no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao obter PDF do cupom fiscal: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> emitirNfe(Map<String, Object> nfeRequest) {
        if (!enabled) {
            throw new IllegalStateException("Fiscal Simplify está desabilitado.");
        }
        try {
            Map<String, Object> result = client().post()
                    .uri("/nfe")
                    .bodyValue(nfeRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return result != null ? result : Map.of();
        } catch (WebClientResponseException e) {
            log.error("Fiscal Simplify erro ao emitir NF-e: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao emitir NF-e: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao emitir NF-e no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao emitir NF-e: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> importarNfeXml(byte[] xmlBytes) {
        if (!enabled) throw new IllegalStateException("Fiscal Simplify está desabilitado.");
        if (xmlBytes == null || xmlBytes.length == 0) throw new IllegalArgumentException("XML vazio.");
        try {
            org.springframework.http.client.MultipartBodyBuilder mb = new org.springframework.http.client.MultipartBodyBuilder();
            mb.part("xml", xmlBytes)
                    .filename("nfe.xml")
                    .contentType(MediaType.APPLICATION_XML);
            Map<String, Object> result = client().post()
                    .uri("/nfe/import-xml")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(mb.build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return result != null ? result : Map.of();
        } catch (WebClientResponseException e) {
            log.error("Fiscal Simplify erro ao importar XML NF-e: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao importar XML da NF-e: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao importar XML NF-e no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao importar XML da NF-e: " + e.getMessage(), e);
        }
    }

    public byte[] getNfePdf(String nfeId) {
        if (!enabled) {
            throw new IllegalStateException("Fiscal Simplify está desabilitado.");
        }
        try {
            byte[] pdf = client().get()
                    .uri("/nfe/{id}/pdf?download=false", nfeId)
                    .accept(MediaType.APPLICATION_PDF)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
            return pdf != null ? pdf : new byte[0];
        } catch (WebClientResponseException e) {
            log.error("Fiscal Simplify erro ao obter PDF NF-e: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao obter PDF da NF-e: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao obter PDF NF-e no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao obter PDF da NF-e: " + e.getMessage(), e);
        }
    }

    public byte[] getNfeXml(String nfeId) {
        if (!enabled) throw new IllegalStateException("Fiscal Simplify está desabilitado.");
        try {
            byte[] xml = client().get()
                    .uri("/nfe/{id}/xml?download=false", nfeId)
                    .accept(MediaType.APPLICATION_XML)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
            return xml != null ? xml : new byte[0];
        } catch (WebClientResponseException e) {
            log.error("Fiscal Simplify erro ao obter XML NF-e: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao obter XML da NF-e: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao obter XML NF-e no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao obter XML da NF-e: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getNfeById(String nfeId) {
        if (!enabled) throw new IllegalStateException("Fiscal Simplify está desabilitado.");
        try {
            Map<String, Object> result = client().get()
                    .uri("/nfe/{id}", nfeId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return result != null ? result : Map.of();
        } catch (WebClientResponseException e) {
            log.error("Fiscal Simplify erro ao detalhar NF-e: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao detalhar NF-e: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao detalhar NF-e no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao detalhar NF-e: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> listarNfeEmitidas(
            String cnpj,
            String ambiente,
            Integer top,
            Integer skip,
            Boolean inlinecount,
            String referencia,
            String chave,
            String serie
    ) {
        if (!enabled) {
            throw new IllegalStateException("Fiscal Simplify está desabilitado.");
        }
        String cnpjLimpo = cnpj != null ? cnpj.replaceAll("\\D", "") : "";
        String amb = (ambiente != null && ambiente.equalsIgnoreCase("producao")) ? "producao" : "homologacao";
        try {
            Map<String, Object> result = client().get()
                    .uri(uriBuilder -> {
                        var b = uriBuilder.path("/nfe")
                                .queryParam("cnpj", cnpjLimpo)
                                .queryParam("ambiente", amb);
                        if (top != null) b.queryParam("$top", top);
                        if (skip != null) b.queryParam("$skip", skip);
                        if (inlinecount != null) b.queryParam("$inlinecount", inlinecount);
                        if (referencia != null && !referencia.isBlank()) b.queryParam("referencia", referencia.trim());
                        if (chave != null && !chave.isBlank()) b.queryParam("chave", chave.trim());
                        if (serie != null && !serie.isBlank()) b.queryParam("serie", serie.trim());
                        return b.build();
                    })
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return result != null ? result : Map.of();
        } catch (WebClientResponseException e) {
            log.error("Fiscal Simplify erro ao listar NF-e emitidas: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao listar NF-e emitidas: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao listar NF-e emitidas no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao listar NF-e emitidas: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> listarNfeRecebidas(
            String cnpj,
            String ambiente,
            Integer top,
            Integer skip,
            Boolean inlinecount,
            Integer distNsu,
            String formaDistribuicao,
            String chaveAcesso
    ) {
        if (!enabled) {
            throw new IllegalStateException("Fiscal Simplify está desabilitado.");
        }
        String cnpjLimpo = cnpj != null ? cnpj.replaceAll("\\D", "") : "";
        String amb = (ambiente != null && ambiente.equalsIgnoreCase("producao")) ? "producao" : "homologacao";
        log.info("Fiscal Simplify client: GET {}/nfe/received?cnpj={}&ambiente={} ...", baseUrl, cnpjLimpo, amb);
        try {
            Map<String, Object> result = client().get()
                    .uri(uriBuilder -> {
                        var b = uriBuilder.path("/nfe/received")
                                .queryParam("cnpj", cnpjLimpo)
                                .queryParam("ambiente", amb);
                        if (top != null) b.queryParam("$top", top);
                        if (skip != null) b.queryParam("$skip", skip);
                        if (inlinecount != null) b.queryParam("$inlinecount", inlinecount);
                        if (distNsu != null) b.queryParam("dist_nsu", distNsu);
                        if (formaDistribuicao != null && !formaDistribuicao.isBlank()) {
                            b.queryParam("forma_distribuicao", formaDistribuicao.trim());
                        }
                        if (chaveAcesso != null && !chaveAcesso.isBlank()) b.queryParam("chave_acesso", chaveAcesso.trim());
                        return b.build();
                    })
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return result != null ? result : Map.of();
        } catch (WebClientResponseException e) {
            log.error("Fiscal Simplify erro ao listar NF-e recebidas: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao listar NF-e recebidas: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao listar NF-e recebidas no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao listar NF-e recebidas: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> listarNfceEmitidas(
            String cnpj,
            String ambiente,
            Integer top,
            Integer skip,
            Boolean inlinecount,
            String referencia,
            String chave,
            String serie
    ) {
        if (!enabled) {
            throw new IllegalStateException("Fiscal Simplify está desabilitado.");
        }
        String cnpjLimpo = cnpj != null ? cnpj.replaceAll("\\D", "") : "";
        String amb = (ambiente != null && ambiente.equalsIgnoreCase("producao")) ? "producao" : "homologacao";
        try {
            Map<String, Object> result = client().get()
                    .uri(uriBuilder -> {
                        var b = uriBuilder.path("/nfce")
                                .queryParam("cnpj", cnpjLimpo)
                                .queryParam("ambiente", amb);
                        if (top != null) b.queryParam("$top", top);
                        if (skip != null) b.queryParam("$skip", skip);
                        if (inlinecount != null) b.queryParam("$inlinecount", inlinecount);
                        if (referencia != null && !referencia.isBlank()) b.queryParam("referencia", referencia.trim());
                        if (chave != null && !chave.isBlank()) b.queryParam("chave", chave.trim());
                        if (serie != null && !serie.isBlank()) b.queryParam("serie", serie.trim());
                        return b.build();
                    })
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return result != null ? result : Map.of();
        } catch (WebClientResponseException e) {
            log.error("Fiscal Simplify erro ao listar NFC-e emitidas: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao listar NFC-e emitidas: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao listar NFC-e emitidas no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao listar NFC-e emitidas: " + e.getMessage(), e);
        }
    }

    public byte[] getNfceXml(String nfceId) {
        if (!enabled) throw new IllegalStateException("Fiscal Simplify está desabilitado.");
        try {
            byte[] xml = client().get()
                    .uri("/nfce/{id}/xml?download=false", nfceId)
                    .accept(MediaType.APPLICATION_XML)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
            return xml != null ? xml : new byte[0];
        } catch (WebClientResponseException e) {
            log.error("Fiscal Simplify erro ao obter XML NFC-e: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao obter XML da NFC-e: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao obter XML NFC-e no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao obter XML da NFC-e: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getNfceById(String nfceId) {
        if (!enabled) throw new IllegalStateException("Fiscal Simplify está desabilitado.");
        try {
            Map<String, Object> result = client().get()
                    .uri("/nfce/{id}", nfceId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return result != null ? result : Map.of();
        } catch (WebClientResponseException e) {
            log.error("Fiscal Simplify erro ao detalhar NFC-e: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao detalhar NFC-e: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao detalhar NFC-e no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao detalhar NFC-e: " + e.getMessage(), e);
        }
    }

    public byte[] getNfeReceivedPdf(String docId) {
        if (!enabled) throw new IllegalStateException("Fiscal Simplify está desabilitado.");
        try {
            byte[] pdf = client().get()
                    .uri("/nfe/received/{id}/pdf?download=false", docId)
                    .accept(MediaType.APPLICATION_PDF)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
            return pdf != null ? pdf : new byte[0];
        } catch (WebClientResponseException e) {
            log.error("Fiscal Simplify erro ao obter PDF NF-e recebida: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao obter PDF da NF-e recebida: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao obter PDF NF-e recebida no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao obter PDF da NF-e recebida: " + e.getMessage(), e);
        }
    }

    public byte[] getNfeReceivedXml(String docId) {
        if (!enabled) throw new IllegalStateException("Fiscal Simplify está desabilitado.");
        try {
            byte[] xml = client().get()
                    .uri("/nfe/received/{id}/xml?download=false", docId)
                    .accept(MediaType.APPLICATION_XML)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
            return xml != null ? xml : new byte[0];
        } catch (WebClientResponseException e) {
            log.error("Fiscal Simplify erro ao obter XML NF-e recebida: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao obter XML da NF-e recebida: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao obter XML NF-e recebida no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao obter XML da NF-e recebida: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getNfeReceivedById(String docId) {
        if (!enabled) throw new IllegalStateException("Fiscal Simplify está desabilitado.");
        try {
            Map<String, Object> result = client().get()
                    .uri("/nfe/received/{id}", docId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return result != null ? result : Map.of();
        } catch (WebClientResponseException e) {
            log.error("Fiscal Simplify erro ao detalhar NF-e recebida: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Falha ao detalhar NF-e recebida: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro ao detalhar NF-e recebida no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao detalhar NF-e recebida: " + e.getMessage(), e);
        }
    }
}
