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
    String TAG_TABLE_ORDERS = "Comandas";
    String TAG_TABLES = "Mesas";
    String TAG_RESERVATIONS = "Reservas";
    String TAG_STOCK = "Estoque";
    String TAG_SUPPLIERS = "Fornecedores";
    String TAG_CUSTOMERS = "Clientes";
    String TAG_MODULES = "Módulos";
    String TAG_DASHBOARD = "Dashboard";
    String TAG_COST_CONTROL = "Controle de Custos";
}
