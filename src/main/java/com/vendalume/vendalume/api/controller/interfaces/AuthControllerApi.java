package com.vendalume.vendalume.api.controller.interfaces;

import com.vendalume.vendalume.api.documentation.ApiDocumentedController;
import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.api.dto.auth.LoginRequest;
import com.vendalume.vendalume.api.dto.auth.LoginResponse;
import com.vendalume.vendalume.api.dto.auth.RefreshRequest;
import com.vendalume.vendalume.api.dto.auth.RegisterRequest;
import com.vendalume.vendalume.api.dto.auth.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interface do controller de autenticação OAuth 2.0 com JWT.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Tag(name = ApiDocumentedController.TAG_AUTH, description = "Login, refresh de tokens e registro de usuários")
@DefaultApiResponses
@SecurityRequirements
public interface AuthControllerApi {

    @Operation(summary = "Login", description = "Autentica usuário; tokens são definidos em cookies HTTP-only (não expostos no corpo da resposta)")
    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou conta bloqueada")
    @PostMapping("/login")
    ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response);

    @Operation(summary = "Refresh token", description = "Renova tokens via cookie HTTP-only ou body; novos tokens em cookies")
    @ApiResponse(responseCode = "200", description = "Tokens renovados com sucesso", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
    @PostMapping("/refresh")
    ResponseEntity<LoginResponse> refresh(@RequestBody(required = false) RefreshRequest request,
                                          HttpServletRequest httpRequest,
                                          HttpServletResponse response);

    @Operation(summary = "Logout", description = "Remove cookies de autenticação (logout seguro)")
    @ApiResponse(responseCode = "204", description = "Cookies removidos")
    @PostMapping("/logout")
    ResponseEntity<Void> logout(HttpServletResponse response);

    @Operation(summary = "Registro", description = "Cadastra novo usuário no sistema")
    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou username/email já existente")
    @PostMapping("/register")
    ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request);
}
