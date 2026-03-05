package com.vendalume.vendalume.integration.fiscalsimplify;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuração do cliente HTTP para integração com Fiscal Simplify.
 * Fornece WebClient.Builder pois não é auto-configurado em projetos webmvc.
 *
 * @author VendaLume
 */
@Configuration
public class FiscalSimplifyConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
