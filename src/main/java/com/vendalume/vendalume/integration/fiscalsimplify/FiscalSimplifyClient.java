package com.vendalume.vendalume.integration.fiscalsimplify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * Cliente HTTP para integração com a API Fiscal Simplify.
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
        } catch (Exception e) {
            log.error("Erro ao cadastrar empresa no Fiscal Simplify", e);
            throw new RuntimeException("Falha ao integrar empresa com Fiscal Simplify: " + e.getMessage(), e);
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
}
