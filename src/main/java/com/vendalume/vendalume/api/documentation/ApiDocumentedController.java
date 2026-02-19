package com.vendalume.vendalume.api.documentation;

/**
 * Interface de documentação da API para implementação nos controllers.
 * Define constantes de tags para padronização Swagger/OpenAPI.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
public interface ApiDocumentedController {

    String TAG_AUTH = "Autenticação";
    String TAG_HEALTH = "Health";
    String TAG_USERS = "Usuários";
    String TAG_PRODUCTS = "Produtos";
    String TAG_SALES = "Vendas";
    String TAG_DELIVERIES = "Entregas";
}
