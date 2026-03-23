package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.api.dto.contractor.*;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.service.ContractorInvoiceService;
import com.vendalume.vendalume.service.ContractorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Prestadores PJ", description = "Cadastro de prestadores de serviço PJ e notas fiscais")
@DefaultApiResponses
@RestController
@RequestMapping("/contractors")
@RequiredArgsConstructor
public class ContractorController {

    private final ContractorService contractorService;
    private final ContractorInvoiceService contractorInvoiceService;

    @Operation(summary = "Criar prestador PJ")
    @PostMapping
    public ResponseEntity<ContractorResponse> create(@Valid @RequestBody ContractorCreateRequest request) {
        ContractorResponse response = contractorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar prestador por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ContractorResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(contractorService.findById(id));
    }

    @Operation(summary = "Listar prestadores com filtros e paginação")
    @GetMapping
    public ResponseEntity<PageResponse<ContractorResponse>> search(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(contractorService.search(tenantId, search, active, page, size, sortBy, sortDirection));
    }

    @Operation(summary = "Listar prestadores ativos (para selects)")
    @GetMapping("/options")
    public ResponseEntity<List<ContractorResponse>> listActive(@RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(contractorService.listActive(tenantId));
    }

    @Operation(summary = "Atualizar prestador PJ")
    @PutMapping("/{id}")
    public ResponseEntity<ContractorResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ContractorUpdateRequest request) {
        return ResponseEntity.ok(contractorService.update(id, request));
    }

    @Operation(summary = "Excluir prestador PJ")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        contractorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Notas Fiscais do prestador ---

    @Operation(summary = "Listar notas fiscais do prestador")
    @GetMapping("/{contractorId}/invoices")
    public ResponseEntity<List<ContractorInvoiceResponse>> listInvoices(@PathVariable UUID contractorId) {
        return ResponseEntity.ok(contractorInvoiceService.listByContractor(contractorId));
    }

    @Operation(summary = "Buscar nota fiscal do prestador por ID")
    @GetMapping("/{contractorId}/invoices/{invoiceId}")
    public ResponseEntity<ContractorInvoiceResponse> findInvoiceById(
            @PathVariable UUID contractorId,
            @PathVariable UUID invoiceId) {
        return ResponseEntity.ok(contractorInvoiceService.findById(contractorId, invoiceId));
    }

    @Operation(summary = "Registrar nota fiscal do prestador (dados + opcional arquivo PDF/XML)")
    @PostMapping(value = "/{contractorId}/invoices", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContractorInvoiceResponse> createInvoice(
            @PathVariable UUID contractorId,
            @RequestPart("data") @Valid ContractorInvoiceCreateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        ContractorInvoiceResponse response = contractorInvoiceService.create(contractorId, request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Enviar arquivo da nota fiscal (PDF/XML) para uma NF já cadastrada")
    @PostMapping(value = "/{contractorId}/invoices/{invoiceId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContractorInvoiceResponse> uploadInvoiceFile(
            @PathVariable UUID contractorId,
            @PathVariable UUID invoiceId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(contractorInvoiceService.uploadFile(contractorId, invoiceId, file));
    }
}
