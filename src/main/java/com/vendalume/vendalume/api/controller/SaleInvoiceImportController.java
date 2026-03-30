package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.domain.enums.SaleType;
import com.vendalume.vendalume.service.SaleInvoiceImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Vendas", description = "Importação de venda a partir de Nota Fiscal (PDF/XML/JSON)")
@DefaultApiResponses
@RestController
@RequestMapping("/sales/import")
@RequiredArgsConstructor
public class SaleInvoiceImportController {

    private final SaleInvoiceImportService saleInvoiceImportService;

    @Operation(summary = "Cadastrar venda a partir de nota fiscal (upload PDF/XML/JSON)")
    @PostMapping(value = "/invoice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PERMISSION_SALE_CREATE') or hasAuthority('PERMISSION_FULL_SYSTEM_ACCESS')")
    public ResponseEntity<Map<String, Object>> importInvoice(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) SaleType saleType,
            @RequestPart(required = false) MultipartFile pdf,
            @RequestPart(required = false) MultipartFile xml,
            @RequestPart(required = false) MultipartFile json,
            @RequestParam(required = false) String jsonText,
            @RequestParam(required = false) String notes
    ) {
        Map<String, Object> result = saleInvoiceImportService.importSaleFromInvoice(
                tenantId, saleType, pdf, xml, json, jsonText, notes
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping(value = "/invoice", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> importInvoiceWrongContentType() {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(Map.of(
                "message", "Envie multipart/form-data com xml/json (arquivos) ou jsonText. Não envie application/json neste endpoint."
        ));
    }
}

