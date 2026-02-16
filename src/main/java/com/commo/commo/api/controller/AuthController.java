package com.commo.commo.api.controller;

import com.commo.commo.api.dto.auth.LoginRequest;
import com.commo.commo.api.dto.auth.LoginResponse;
import com.commo.commo.api.dto.auth.RefreshRequest;
import com.commo.commo.api.dto.auth.RegisterRequest;
import com.commo.commo.api.dto.auth.UserResponse;
import com.commo.commo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de autenticação OAuth 2.0 com JWT.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        LoginResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
