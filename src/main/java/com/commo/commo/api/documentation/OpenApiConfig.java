package com.commo.commo.api.documentation;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do OpenAPI 3.0 / Swagger para documentação da API Commo.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers())
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT obtido via POST /auth/login")));
    }

    private Info apiInfo() {
        return new Info()
                .title("Commo API")
                .description("""
                        API do sistema Commo - SaaS completo para mercados, restaurantes, PDV, delivery e gestão de vendas.
                        
                        ## Autenticação
                        Utiliza OAuth 2.0 com JWT. Após login bem-sucedido, utilize o `access_token` no header:
                        ```
                        Authorization: Bearer {access_token}
                        ```
                        
                        ## Endpoints Públicos
                        - `POST /auth/login` - Login com username e senha
                        - `POST /auth/refresh` - Renovar tokens
                        - `POST /auth/register` - Cadastro de usuário
                        - `GET /health` - Health check
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Commo")
                        .email("contato@commo.com.br")
                        .url("https://commo.com.br"))
                .license(new License()
                        .name("Proprietário")
                        .url("https://commo.com.br/license"));
    }

    private List<Server> apiServers() {
        return List.of(
                new Server()
                        .url(contextPath)
                        .description("API Base")
        );
    }
}
