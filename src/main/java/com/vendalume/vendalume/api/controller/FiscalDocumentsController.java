package com.vendalume.vendalume.api.controller;

import com.vendalume.vendalume.api.documentation.DefaultApiResponses;
import com.vendalume.vendalume.service.FiscalDocumentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Controller REST que expõe os endpoints relacionados a FiscalDocumentsController.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */

@Tag(name = "Fiscal", description = "Consulta de NF-e/NFC-e (emitidas e recebidas) via Fiscal Simplify/Nuvem Fiscal")
@DefaultApiResponses
@RestController
@RequestMapping("/fiscal")
@RequiredArgsConstructor
public class FiscalDocumentsController {

    private final FiscalDocumentsService fiscalDocumentsService;

    @Operation(summary = "Listar NF-e emitidas (beneficiária/emitente)")
    @GetMapping("/nfe")
    @PreAuthorize("hasAuthority('PERMISSION_FISCAL_VIEW') or hasAuthority('PERMISSION_FULL_SYSTEM_ACCESS')")
    public ResponseEntity<Map<String, Object>> listarNfeEmitidas(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false, name = "$top") Integer top,
            @RequestParam(required = false, name = "$skip") Integer skip,
            @RequestParam(required = false, name = "$inlinecount") Boolean inlinecount,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) String chave,
            @RequestParam(required = false) String serie
    ) {
        return ResponseEntity.ok(fiscalDocumentsService.listarNfeEmitidas(tenantId, top, skip, inlinecount, referencia, chave, serie));
    }

    @Operation(summary = "Detalhar NF-e emitida por ID")
    @GetMapping("/nfe/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_FISCAL_VIEW') or hasAuthority('PERMISSION_FULL_SYSTEM_ACCESS')")
    public ResponseEntity<Map<String, Object>> detalharNfeEmitida(
            @RequestParam(required = false) UUID tenantId,
            @PathVariable String id
    ) {
        return ResponseEntity.ok(fiscalDocumentsService.detalharNfeEmitida(tenantId, id));
    }

    @Operation(summary = "Baixar PDF da NF-e emitida por ID")
    @GetMapping(value = "/nfe/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAuthority('PERMISSION_FISCAL_VIEW') or hasAuthority('PERMISSION_FULL_SYSTEM_ACCESS')")
    public ResponseEntity<byte[]> baixarPdfNfeEmitida(
            @RequestParam(required = false) UUID tenantId,
            @PathVariable String id
    ) {
        byte[] pdf = fiscalDocumentsService.baixarPdfNfeEmitida(tenantId, id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nfe-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @Operation(summary = "Baixar XML da NF-e emitida por ID")
    @GetMapping(value = "/nfe/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    @PreAuthorize("hasAuthority('PERMISSION_FISCAL_VIEW') or hasAuthority('PERMISSION_FULL_SYSTEM_ACCESS')")
    public ResponseEntity<byte[]> baixarXmlNfeEmitida(
            @RequestParam(required = false) UUID tenantId,
            @PathVariable String id
    ) {
        byte[] xml = fiscalDocumentsService.baixarXmlNfeEmitida(tenantId, id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nfe-" + id + ".xml\"")
                .contentType(MediaType.APPLICATION_XML)
                .body(xml);
    }

    @Operation(summary = "Listar NF-e recebidas via distribuição (a pagar/destinatária)")
    @GetMapping("/nfe/received")
    @PreAuthorize("hasAuthority('PERMISSION_FISCAL_VIEW') or hasAuthority('PERMISSION_FULL_SYSTEM_ACCESS')")
    public ResponseEntity<Map<String, Object>> listarNfeRecebidas(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false, name = "$top") Integer top,
            @RequestParam(required = false, name = "$skip") Integer skip,
            @RequestParam(required = false, name = "$inlinecount") Boolean inlinecount,
            @RequestParam(required = false, name = "dist_nsu") Integer distNsu,
            @RequestParam(required = false, name = "forma_distribuicao") String formaDistribuicao,
            @RequestParam(required = false, name = "chave_acesso") String chaveAcesso
    ) {
        return ResponseEntity.ok(fiscalDocumentsService.listarNfeRecebidas(tenantId, top, skip, inlinecount, distNsu, formaDistribuicao, chaveAcesso));
    }

    @Operation(summary = "Detalhar documento de distribuição NF-e (recebida) por ID")
    @GetMapping("/nfe/received/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_FISCAL_VIEW') or hasAuthority('PERMISSION_FULL_SYSTEM_ACCESS')")
    public ResponseEntity<Map<String, Object>> detalharNfeRecebida(
            @RequestParam(required = false) UUID tenantId,
            @PathVariable String id
    ) {
        return ResponseEntity.ok(fiscalDocumentsService.detalharNfeRecebida(tenantId, id));
    }

    @Operation(summary = "Baixar PDF do documento de distribuição NF-e (recebida) por ID")
    @GetMapping(value = "/nfe/received/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAuthority('PERMISSION_FISCAL_VIEW') or hasAuthority('PERMISSION_FULL_SYSTEM_ACCESS')")
    public ResponseEntity<byte[]> baixarPdfNfeRecebida(
            @RequestParam(required = false) UUID tenantId,
            @PathVariable String id
    ) {
        byte[] pdf = fiscalDocumentsService.baixarPdfNfeRecebida(tenantId, id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nfe-recebida-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @Operation(summary = "Baixar XML do documento de distribuição NF-e (recebida) por ID")
    @GetMapping(value = "/nfe/received/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    @PreAuthorize("hasAuthority('PERMISSION_FISCAL_VIEW') or hasAuthority('PERMISSION_FULL_SYSTEM_ACCESS')")
    public ResponseEntity<byte[]> baixarXmlNfeRecebida(
            @RequestParam(required = false) UUID tenantId,
            @PathVariable String id
    ) {
        byte[] xml = fiscalDocumentsService.baixarXmlNfeRecebida(tenantId, id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nfe-recebida-" + id + ".xml\"")
                .contentType(MediaType.APPLICATION_XML)
                .body(xml);
    }

    @Operation(summary = "Listar todas as NF-e (emitidas + recebidas) da empresa")
    @GetMapping("/nfe/all")
    @PreAuthorize("hasAuthority('PERMISSION_FISCAL_VIEW') or hasAuthority('PERMISSION_FULL_SYSTEM_ACCESS')")
    public ResponseEntity<Map<String, Object>> listarNfeTodas(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false, name = "$top") Integer top,
            @RequestParam(required = false, name = "$skip") Integer skip,
            @RequestParam(required = false, name = "$inlinecount") Boolean inlinecount,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) String chave,
            @RequestParam(required = false) String serie,
            @RequestParam(required = false, name = "dist_nsu") Integer distNsu,
            @RequestParam(required = false, name = "forma_distribuicao") String formaDistribuicao,
            @RequestParam(required = false, name = "chave_acesso") String chaveAcesso
    ) {
        return ResponseEntity.ok(fiscalDocumentsService.listarNfeTodas(
                tenantId, top, skip, inlinecount, referencia, chave, serie, distNsu, formaDistribuicao, chaveAcesso));
    }

    @Operation(summary = "Listar NFC-e emitidas")
    @GetMapping("/nfce")
    @PreAuthorize("hasAuthority('PERMISSION_FISCAL_VIEW') or hasAuthority('PERMISSION_FULL_SYSTEM_ACCESS')")
    public ResponseEntity<Map<String, Object>> listarNfceEmitidas(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false, name = "$top") Integer top,
            @RequestParam(required = false, name = "$skip") Integer skip,
            @RequestParam(required = false, name = "$inlinecount") Boolean inlinecount,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) String chave,
            @RequestParam(required = false) String serie
    ) {
        return ResponseEntity.ok(fiscalDocumentsService.listarNfceEmitidas(tenantId, top, skip, inlinecount, referencia, chave, serie));
    }

    @Operation(summary = "Detalhar NFC-e emitida por ID")
    @GetMapping("/nfce/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_FISCAL_VIEW') or hasAuthority('PERMISSION_FULL_SYSTEM_ACCESS')")
    public ResponseEntity<Map<String, Object>> detalharNfceEmitida(
            @RequestParam(required = false) UUID tenantId,
            @PathVariable String id
    ) {
        return ResponseEntity.ok(fiscalDocumentsService.detalharNfceEmitida(tenantId, id));
    }

    @Operation(summary = "Baixar PDF da NFC-e emitida por ID")
    @GetMapping(value = "/nfce/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAuthority('PERMISSION_FISCAL_VIEW') or hasAuthority('PERMISSION_FULL_SYSTEM_ACCESS')")
    public ResponseEntity<byte[]> baixarPdfNfceEmitida(
            @RequestParam(required = false) UUID tenantId,
            @PathVariable String id
    ) {
        byte[] pdf = fiscalDocumentsService.baixarPdfNfceEmitida(tenantId, id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nfce-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @Operation(summary = "Baixar XML da NFC-e emitida por ID")
    @GetMapping(value = "/nfce/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    @PreAuthorize("hasAuthority('PERMISSION_FISCAL_VIEW') or hasAuthority('PERMISSION_FULL_SYSTEM_ACCESS')")
    public ResponseEntity<byte[]> baixarXmlNfceEmitida(
            @RequestParam(required = false) UUID tenantId,
            @PathVariable String id
    ) {
        byte[] xml = fiscalDocumentsService.baixarXmlNfceEmitida(tenantId, id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nfce-" + id + ".xml\"")
                .contentType(MediaType.APPLICATION_XML)
                .body(xml);
    }
}

