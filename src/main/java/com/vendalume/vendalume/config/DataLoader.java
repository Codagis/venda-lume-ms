package com.vendalume.vendalume.config;

import com.vendalume.vendalume.api.dto.auth.RegisterRequest;
import com.vendalume.vendalume.domain.entity.Permission;
import com.vendalume.vendalume.domain.entity.Tenant;
import com.vendalume.vendalume.domain.entity.User;
import com.vendalume.vendalume.domain.enums.UserRole;
import com.vendalume.vendalume.domain.entity.Module;
import com.vendalume.vendalume.repository.ModuleRepository;
import com.vendalume.vendalume.repository.PermissionRepository;
import com.vendalume.vendalume.repository.ProfileRepository;
import com.vendalume.vendalume.repository.TenantRepository;
import com.vendalume.vendalume.repository.UserRepository;
import com.vendalume.vendalume.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Carrega dados iniciais para ambiente de desenvolvimento.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Slf4j
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DataLoader {

    private static final String CODAGIS_NAME = "Codagis";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private static final String PERFIL_PADRAO = "Padrão";

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final ProfileRepository profileRepository;
    private final PermissionRepository permissionRepository;
    private final ModuleRepository moduleRepository;
    private final AuthService authService;

    @Bean
    CommandLineRunner loadInitialData() {
        return args -> {
            ensurePermissionsExist();
            ensureModulesExist();
            Tenant codagis = tenantRepository.findByNameIgnoreCase(CODAGIS_NAME)
                    .orElseGet(() -> {
                        Tenant t = new Tenant();
                        t.setName(CODAGIS_NAME);
                        t.setTradeName(CODAGIS_NAME);
                        t.setActive(true);
                        return tenantRepository.save(t);
                    });

            if (!userRepository.existsByUsernameIgnoreCase(ADMIN_USERNAME)) {
                var request = RegisterRequest.builder()
                        .username(ADMIN_USERNAME)
                        .password(ADMIN_PASSWORD)
                        .email("admin@vendalume.local")
                        .fullName("Administrador")
                        .role(UserRole.SUPER_ADMIN)
                        .tenantId(codagis.getId())
                        .build();

                authService.register(request);

                User admin = userRepository.findByUsernameIgnoreCase(ADMIN_USERNAME).orElseThrow();
                admin.setIsRoot(true);
                admin.setTenantId(codagis.getId());
                userRepository.save(admin);

                // Perfil padrão para não-root: DASHBOARD_VIEW, PRODUCT_VIEW, PROFILE_VIEW
                if (!profileRepository.existsByTenantIdAndName(codagis.getId(), PERFIL_PADRAO)) {
                    var perms = new java.util.HashSet<Permission>();
                    permissionRepository.findByCode("DASHBOARD_VIEW").ifPresent(perms::add);
                    permissionRepository.findByCode("PRODUCT_VIEW").ifPresent(perms::add);
                    permissionRepository.findByCode("MODULE_VIEW").ifPresent(perms::add);
                    permissionRepository.findByCode("USER_VIEW").ifPresent(perms::add);
                    permissionRepository.findByCode("PROFILE_VIEW").ifPresent(perms::add);
                    permissionRepository.findByCode("SALE_VIEW").ifPresent(perms::add);
                    permissionRepository.findByCode("SALE_CREATE").ifPresent(perms::add);
                    permissionRepository.findByCode("STOCK_VIEW").ifPresent(perms::add);
                    permissionRepository.findByCode("STOCK_MANAGE").ifPresent(perms::add);
                    permissionRepository.findByCode("CUSTOMER_VIEW").ifPresent(perms::add);
                    permissionRepository.findByCode("CUSTOMER_CREATE").ifPresent(perms::add);
                    permissionRepository.findByCode("SUPPLIER_VIEW").ifPresent(perms::add);
                    permissionRepository.findByCode("SUPPLIER_CREATE").ifPresent(perms::add);
                    permissionRepository.findByCode("COST_CONTROL_VIEW").ifPresent(perms::add);
                    permissionRepository.findByCode("COST_CONTROL_MANAGE").ifPresent(perms::add);
                    permissionRepository.findByCode("TABLE_VIEW").ifPresent(perms::add);
                    permissionRepository.findByCode("TABLE_MANAGE").ifPresent(perms::add);
                    com.vendalume.vendalume.domain.entity.Profile p = new com.vendalume.vendalume.domain.entity.Profile();
                    p.setTenantId(codagis.getId());
                    p.setName(PERFIL_PADRAO);
                    p.setDescription("Acesso básico: Dashboard, Produtos e Configurações");
                    p.setPermissions(perms);
                    profileRepository.save(p);
                    log.info("Perfil '{}' criado com permissões iniciais", PERFIL_PADRAO);
                }

                log.info("Usuário admin criado: {} / {} (root, tenant Codagis)", ADMIN_USERNAME, ADMIN_PASSWORD);
            } else {
                // Admin já existe: garantir que seja root
                userRepository.findByUsernameIgnoreCase(ADMIN_USERNAME).ifPresent(admin -> {
                    if (!Boolean.TRUE.equals(admin.getIsRoot())) {
                        admin.setIsRoot(true);
                        userRepository.save(admin);
                        log.info("Admin atualizado: isRoot=true");
                    }
                });
            }
        };
    }

    private void ensurePermissionsExist() {
        createPermissionIfNotExists("DASHBOARD_VIEW", "Visualizar dashboard", "Acessar a tela inicial", "DASHBOARD");
        createPermissionIfNotExists("PRODUCT_VIEW", "Visualizar produtos", "Ver listagem e detalhes de produtos", "PRODUCTS");
        createPermissionIfNotExists("PRODUCT_CREATE", "Criar produtos", "Cadastrar novos produtos", "PRODUCTS");
        createPermissionIfNotExists("PRODUCT_UPDATE", "Editar produtos", "Alterar dados de produtos", "PRODUCTS");
        createPermissionIfNotExists("PRODUCT_DELETE", "Excluir produtos", "Remover produtos do cadastro", "PRODUCTS");
        createPermissionIfNotExists("MODULE_VIEW", "Visualizar módulos", "Ver módulos do sistema", "MODULES");
        createPermissionIfNotExists("MODULE_MANAGE", "Gerenciar módulos", "Criar e editar módulos", "MODULES");
        createPermissionIfNotExists("USER_VIEW", "Visualizar usuários", "Ver usuários da empresa", "USERS");
        createPermissionIfNotExists("USER_CREATE", "Criar usuários", "Cadastrar usuários", "USERS");
        createPermissionIfNotExists("USER_UPDATE", "Editar usuários", "Alterar dados de usuários", "USERS");
        createPermissionIfNotExists("USER_DELETE", "Excluir usuários", "Desativar ou remover usuários", "USERS");
        createPermissionIfNotExists("PROFILE_VIEW", "Visualizar perfis", "Ver perfis e configurações", "PROFILES");
        createPermissionIfNotExists("PROFILE_MANAGE", "Gerenciar perfis", "Criar e editar perfis", "PROFILES");
        createPermissionIfNotExists("TENANT_VIEW", "Visualizar empresas", "Ver listagem de empresas (uso restrito)", "TENANTS");
        createPermissionIfNotExists("TENANT_MANAGE", "Gerenciar empresas", "Criar/editar empresas (root: todas; demais: só a própria)", "TENANTS");
        createPermissionIfNotExists("SALE_VIEW", "Visualizar vendas", "Ver vendas e relatórios", "SALES");
        createPermissionIfNotExists("SALE_CREATE", "Registrar vendas", "Realizar vendas no PDV", "SALES");
        createPermissionIfNotExists("SALE_CANCEL", "Cancelar vendas", "Cancelar vendas", "SALES");
        createPermissionIfNotExists("DELIVERY_VIEW", "Visualizar entregas", "Ver entregas", "DELIVERY");
        createPermissionIfNotExists("DELIVERY_MANAGE", "Gerenciar entregas", "Alterar status e atribuir entregas", "DELIVERY");
        createPermissionIfNotExists("MY_DELIVERIES_VIEW", "Minhas entregas", "Tela de entregas do entregador com registro de comprovante", "MY_DELIVERIES");
        createPermissionIfNotExists("CUSTOMER_VIEW", "Visualizar clientes", "Ver listagem e detalhes de clientes", "CUSTOMERS");
        createPermissionIfNotExists("CUSTOMER_CREATE", "Criar clientes", "Cadastrar novos clientes", "CUSTOMERS");
        createPermissionIfNotExists("CUSTOMER_UPDATE", "Editar clientes", "Alterar dados de clientes", "CUSTOMERS");
        createPermissionIfNotExists("CUSTOMER_DELETE", "Excluir clientes", "Remover clientes do cadastro", "CUSTOMERS");
        createPermissionIfNotExists("STOCK_VIEW", "Visualizar estoque", "Ver gestão de estoque e movimentações", "STOCK");
        createPermissionIfNotExists("STOCK_MANAGE", "Gerenciar estoque", "Registrar entradas, saídas e ajustes", "STOCK");
        createPermissionIfNotExists("SUPPLIER_VIEW", "Visualizar fornecedores", "Ver listagem e detalhes de fornecedores", "SUPPLIERS");
        createPermissionIfNotExists("SUPPLIER_CREATE", "Criar fornecedores", "Cadastrar novos fornecedores", "SUPPLIERS");
        createPermissionIfNotExists("SUPPLIER_UPDATE", "Editar fornecedores", "Alterar dados de fornecedores", "SUPPLIERS");
        createPermissionIfNotExists("SUPPLIER_DELETE", "Excluir fornecedores", "Remover fornecedores do cadastro", "SUPPLIERS");
        createPermissionIfNotExists("COST_CONTROL_VIEW", "Visualizar controle de custos", "Ver contas a pagar e a receber", "COST_CONTROL");
        createPermissionIfNotExists("COST_CONTROL_MANAGE", "Gerenciar controle de custos", "Criar, editar e registrar pagamentos/recebimentos", "COST_CONTROL");
        createPermissionIfNotExists("TABLE_VIEW", "Visualizar mesas", "Ver seções, mesas e reservas", "RESTAURANT_TABLES");
        createPermissionIfNotExists("TABLE_MANAGE", "Gerenciar mesas", "Criar, editar e excluir seções, mesas e reservas", "RESTAURANT_TABLES");
        createPermissionIfNotExists("REGISTER_VIEW", "Visualizar pontos de venda", "Ver caixas e operadores", "REGISTERS");
        createPermissionIfNotExists("REGISTER_MANAGE", "Gerenciar pontos de venda", "Criar, editar caixas e atribuir operadores", "REGISTERS");

        createPermissionIfNotExists("FISCAL_VIEW", "Visualizar notas fiscais", "Consultar NF-e/NFC-e emitidas e recebidas", "FISCAL");
    }

    private void createPermissionIfNotExists(String code, String name, String description, String module) {
        if (permissionRepository.existsByCode(code)) return;
        Permission p = new Permission();
        p.setCode(code);
        p.setName(name);
        p.setDescription(description);
        p.setModule(module);
        permissionRepository.save(p);
        log.debug("Permissão criada: {}", code);
    }

    private void ensureModulesExist() {
        createModule("DASHBOARD", "Dashboard", "Tela inicial", "DashboardOutlined", "/", "Dashboard", 0, "DASHBOARD_VIEW");
        createModule("PRODUCTS", "Produtos", "Cadastro de produtos", "ShoppingOutlined", "/products", "Products", 10, "PRODUCT_VIEW");
        createModule("CUSTOMERS", "Clientes", "Cadastro de clientes", "UserOutlined", "/customers", "Customers", 12, "CUSTOMER_VIEW");
        createModule("SALES", "Registrar vendas", "PDV e registro de vendas", "ShoppingCartOutlined", "/sales", "Sales", 15, "SALE_VIEW");
        createModule("SALES_CONSULT", "Consultar vendas", "Consulta detalhada e totalizador de vendas", "FileSearchOutlined", "/sales-consult", "SalesConsult", 16, "SALE_VIEW");
        createModule("STOCK", "Estoque", "Controle de estoque e movimentações", "InboxOutlined", "/stock", "Stock", 11, "STOCK_VIEW");
        createModule("SUPPLIERS", "Fornecedores", "Cadastro de fornecedores", "ShopOutlined", "/suppliers", "Suppliers", 13, "SUPPLIER_VIEW");
        createModule("COST_CONTROL", "Controle de Custos", "Contas a pagar e contas a receber", "DollarOutlined", "/cost-control", "CostControl", 14, "COST_CONTROL_VIEW");
        createModule("DELIVERY", "Entregas", "Gestão e acompanhamento de entregas", "CarOutlined", "/delivery", "Deliveries", 17, "DELIVERY_VIEW");
        createModule("DELIVERY_PERSONS", "Entregadores", "Cadastro de entregadores", "UserOutlined", "/delivery-persons", "DeliveryPersons", 18, "DELIVERY_VIEW");
        createModule("MY_DELIVERIES", "Minhas Entregas", "Tela do entregador: listar entregas e registrar comprovante com foto", "CarOutlined", "/my-deliveries", "MyDeliveries", 18, "MY_DELIVERIES_VIEW");
        createModule("REGISTERS", "Pontos de Venda", "Cadastro de caixas (PDV) e equipamentos", "DesktopOutlined", "/registers", "Registers", 18, "REGISTER_VIEW");
        createModule("CASHIERS", "Operadores de Caixa", "Usuários que operam os caixas (perfil Caixa/Operador)", "UserOutlined", "/cashiers", "Cashiers", 18, "REGISTER_VIEW");
        createModule("RESTAURANT_TABLES", "Mesas do Restaurante", "Seções, mesas e reservas", "CoffeeOutlined", "/restaurant-tables", "RestaurantTables", 19, "TABLE_VIEW");
        createModule("MODULES", "Módulos", "Cadastro de módulos do sistema", "AppstoreOutlined", "/modules", "Modules", 20, "MODULE_VIEW");
        createModule("FISCAL", "Notas Fiscais", "Consulta de NF-e/NFC-e (emitidas e recebidas)", "FileSearchOutlined", "/fiscal-notes", "FiscalNotes", 21, "FISCAL_VIEW");
        createModule("USERS", "Usuários", "Cadastro de usuários", "TeamOutlined", "/users", "Users", 30, "USER_VIEW");
        createModule("SETTINGS", "Configurações", "Empresas, perfis e permissões", "SettingOutlined", "/settings", "Settings", 100, "PROFILE_VIEW");
        log.info("Módulos padrão criados");
    }

    private void createModule(String code, String name, String desc, String icon, String route, String component, int order, String viewPerm) {
        if (moduleRepository.existsByCode(code)) return;
        Module m = new Module();
        m.setCode(code);
        m.setName(name);
        m.setDescription(desc);
        m.setIcon(icon);
        m.setRoute(route);
        m.setComponent(component);
        m.setDisplayOrder(order);
        m.setViewPermissionCode(viewPerm);
        m.setActive(true);
        moduleRepository.save(m);
    }
}
