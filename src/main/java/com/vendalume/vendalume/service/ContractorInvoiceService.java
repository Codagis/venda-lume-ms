package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.contractor.ContractorInvoiceCreateRequest;
import com.vendalume.vendalume.api.dto.contractor.ContractorInvoiceResponse;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.domain.entity.Contractor;
import com.vendalume.vendalume.domain.entity.ContractorInvoice;
import com.vendalume.vendalume.repository.ContractorInvoiceRepository;
import com.vendalume.vendalume.repository.ContractorRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractorInvoiceService {

    private final ContractorInvoiceRepository invoiceRepository;
    private final ContractorRepository contractorRepository;
    private final Optional<GcsStorageService> gcsStorageService;

    @Transactional
    public ContractorInvoiceResponse create(UUID contractorId, ContractorInvoiceCreateRequest request, MultipartFile file) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Contractor contractor = contractorRepository.findByIdAndTenantId(contractorId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestador PJ", contractorId));
        UUID userId = SecurityUtils.getCurrentUserId();
        ContractorInvoice invoice = ContractorInvoice.builder()
                .tenantId(tenantId)
                .contractorId(contractorId)
                .referenceMonth(request.getReferenceMonth().trim())
                .amount(request.getAmount())
                .nfNumber(request.getNfNumber() != null ? request.getNfNumber().trim() : null)
                .nfKey(request.getNfKey() != null ? request.getNfKey().trim() : null)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .build();
        invoice.setCreatedBy(userId);
        invoice.setUpdatedBy(userId);
        invoice = invoiceRepository.save(invoice);
        if (file != null && !file.isEmpty() && gcsStorageService.isPresent()) {
            try {
                String url = gcsStorageService.get().uploadContractorInvoice(tenantId, contractorId, invoice.getId(), file);
                invoice.setFileGcsPath(url);
                invoice.setFileOriginalName(file.getOriginalFilename());
                invoice.setUploadedAt(Instant.now());
                invoice = invoiceRepository.save(invoice);
            } catch (IOException e) {
                log.warn("Falha ao enviar NF do prestador ao GCS: {}", e.getMessage());
            }
        }
        return toResponse(invoice, contractor.getName());
    }

    @Transactional
    public ContractorInvoiceResponse uploadFile(UUID contractorId, UUID invoiceId, MultipartFile file) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Contractor contractor = contractorRepository.findByIdAndTenantId(contractorId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestador PJ", contractorId));
        ContractorInvoice invoice = invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota fiscal do prestador", invoiceId));
        if (!invoice.getContractorId().equals(contractorId)) {
            throw new IllegalArgumentException("Nota fiscal não pertence a este prestador.");
        }
        if (!gcsStorageService.isPresent()) {
            throw new IllegalStateException("Upload de arquivo não está disponível (Google Cloud Storage não configurado).");
        }
        try {
            String url = gcsStorageService.get().uploadContractorInvoice(tenantId, contractorId, invoiceId, file);
            invoice.setFileGcsPath(url);
            invoice.setFileOriginalName(file.getOriginalFilename());
            invoice.setUploadedAt(Instant.now());
            invoice.setUpdatedBy(SecurityUtils.getCurrentUserId());
            invoice = invoiceRepository.save(invoice);
        } catch (IOException e) {
            throw new IllegalArgumentException("Erro ao enviar arquivo: " + e.getMessage());
        }
        return toResponse(invoice, contractor.getName());
    }

    @Transactional(readOnly = true)
    public ContractorInvoiceResponse findById(UUID contractorId, UUID invoiceId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Contractor contractor = contractorRepository.findByIdAndTenantId(contractorId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestador PJ", contractorId));
        ContractorInvoice invoice = invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota fiscal do prestador", invoiceId));
        if (!invoice.getContractorId().equals(contractorId)) {
            throw new ResourceNotFoundException("Nota fiscal do prestador", invoiceId);
        }
        return toResponse(invoice, contractor.getName());
    }

    @Transactional(readOnly = true)
    public List<ContractorInvoiceResponse> listByContractor(UUID contractorId) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Contractor contractor = contractorRepository.findByIdAndTenantId(contractorId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestador PJ", contractorId));
        return invoiceRepository.findByTenantIdAndContractorIdOrderByReferenceMonthDesc(tenantId, contractorId)
                .stream()
                .map(inv -> toResponse(inv, contractor.getName()))
                .toList();
    }

    private ContractorInvoiceResponse toResponse(ContractorInvoice inv, String contractorName) {
        return ContractorInvoiceResponse.builder()
                .id(inv.getId()).tenantId(inv.getTenantId()).contractorId(inv.getContractorId())
                .contractorName(contractorName)
                .referenceMonth(inv.getReferenceMonth()).amount(inv.getAmount())
                .nfNumber(inv.getNfNumber()).nfKey(inv.getNfKey()).description(inv.getDescription())
                .fileGcsPath(inv.getFileGcsPath()).fileOriginalName(inv.getFileOriginalName()).uploadedAt(inv.getUploadedAt())
                .createdAt(inv.getCreatedAt())
                .build();
    }
}
