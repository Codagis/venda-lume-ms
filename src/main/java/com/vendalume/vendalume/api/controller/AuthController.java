package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.controller.interfaces.AuthControllerApi;
import com.vendalume.vendalume.api.dto.auth.LoginRequest;
import com.vendalume.vendalume.api.dto.auth.LoginResponse;
import com.vendalume.vendalume.api.dto.auth.RefreshRequest;
import com.vendalume.vendalume.api.dto.auth.RegisterRequest;
import com.vendalume.vendalume.api.dto.auth.UserInfo;
import com.vendalume.vendalume.api.dto.auth.UserResponse;
import com.vendalume.vendalume.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de autenticação OAuth 2.0 com JWT.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse body = authService.login(request, response);
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<LoginResponse> refresh(@RequestBody(required = false) RefreshRequest request,
                                                  HttpServletRequest httpRequest,
                                                  HttpServletResponse response) {
        LoginResponse body = authService.refresh(request, httpRequest, response);
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfo> me() {
        return ResponseEntity.ok(authService.getCurrentUserInfo());
    }

    @Override
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
