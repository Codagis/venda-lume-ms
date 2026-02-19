package com.vendalume.vendalume.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller de health check.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@RestController
@RequestMapping("/health")
public class HealthController implements HealthControllerApi {

    @Override
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
