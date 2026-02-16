package com.commo.commo.config;

import com.commo.commo.api.dto.auth.RegisterRequest;
import com.commo.commo.domain.enums.UserRole;
import com.commo.commo.repository.UserRepository;
import com.commo.commo.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Carrega dados iniciais para ambiente de desenvolvimento (H2).
 * Cria usuário admin para testes: admin / admin123
 */
@Slf4j
@Configuration
@Profile("dev-h2")
@RequiredArgsConstructor
public class DataLoader {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Bean
    CommandLineRunner loadInitialData() {
        return args -> {
            if (userRepository.existsByUsernameIgnoreCase("admin")) {
                log.info("Usuário admin já existe. Pulando criação inicial.");
                return;
            }

            var request = RegisterRequest.builder()
                    .username("admin")
                    .password("admin123")
                    .email("admin@commo.local")
                    .fullName("Administrador")
                    .role(UserRole.SUPER_ADMIN)
                    .build();

            authService.register(request);
            log.info("Usuário inicial criado: admin / admin123");
        };
    }
}
