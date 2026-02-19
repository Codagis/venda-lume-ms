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
 * Cria tenant Codagis e usuário admin root: admin / admin123
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
        createPermissionIfNotExists("TENANT_VIEW", "Visualizar empresas", "Ver empresas (somente root)", "TENANTS");
        createPermissionIfNotExists("TENANT_MANAGE", "Gerenciar empresas", "Criar e editar empresas", "TENANTS");
        createPermissionIfNotExists("SALE_VIEW", "Visualizar vendas", "Ver vendas e relatórios", "SALES");
        createPermissionIfNotExists("SALE_CREATE", "Registrar vendas", "Realizar vendas no PDV", "SALES");
        createPermissionIfNotExists("SALE_CANCEL", "Cancelar vendas", "Cancelar vendas", "SALES");
        createPermissionIfNotExists("DELIVERY_VIEW", "Visualizar entregas", "Ver entregas", "DELIVERY");
        createPermissionIfNotExists("DELIVERY_MANAGE", "Gerenciar entregas", "Alterar status e atribuir entregas", "DELIVERY");
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
        createModule("MODULES", "Módulos", "Cadastro de módulos do sistema", "AppstoreOutlined", "/modules", "Modules", 20, "MODULE_VIEW");
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
