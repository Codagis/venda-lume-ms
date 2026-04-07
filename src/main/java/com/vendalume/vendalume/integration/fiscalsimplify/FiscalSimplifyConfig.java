package com.vendalume.vendalume.integration.fiscalsimplify;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuração Spring (WebClient) para integração com a API Fiscal Simplify:
 * base URL, chave e habilitação da emissão fiscal (NFC-e, NF-e) e cadastros relacionados.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Configuration
public class FiscalSimplifyConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
