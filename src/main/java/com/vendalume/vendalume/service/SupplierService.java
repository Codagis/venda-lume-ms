package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.supplier.*;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.domain.entity.Supplier;
import com.vendalume.vendalume.repository.SupplierRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("name", "document", "tradeName", "createdAt");
    private final SupplierRepository supplierRepository;

    @Transactional
    public SupplierResponse create(SupplierCreateRequest request) {
        UUID tenantId;
        if (SecurityUtils.isCurrentUserRoot()) {
            if (request.getTenantId() == null) {
                throw new IllegalArgumentException("Selecione a empresa do fornecedor.");
            }
            tenantId = request.getTenantId();
        } else {
            tenantId = SecurityUtils.requireTenantId();
        }
        UUID userId = SecurityUtils.getCurrentUserId();
        if (request.getDocument() != null && !request.getDocument().isBlank()) {
            validateDocumentUnique(tenantId, null, request.getDocument());
        }
        Supplier supplier = toEntity(request, tenantId);
        supplier.setCreatedBy(userId);
        supplier.setUpdatedBy(userId);
        supplier = supplierRepository.save(supplier);
        return toResponse(supplier);
    }

    @Transactional(readOnly = true)
    public SupplierResponse findById(UUID id) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Supplier supplier = supplierRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor", id));
        return toResponse(supplier);
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> search(UUID requestTenantId, SupplierFilterRequest filter) {
        final UUID tenantId;
        if (SecurityUtils.isCurrentUserRoot()) {
            UUID chosen = requestTenantId != null ? requestTenantId : filter.getTenantId();
            tenantId = chosen != null ? chosen : SecurityUtils.getTenantIdOptional().orElse(null);
            if (tenantId == null) {
                throw new IllegalStateException("Selecione uma empresa para listar os fornecedores.");
            }
        } else {
            tenantId = SecurityUtils.requireTenantId();
        }
        String sortField = isValidSortField(filter.getSortBy()) ? filter.getSortBy() : "name";
        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(filter.getPage() != null ? filter.getPage() : 0,
                Math.min(filter.getSize() != null ? filter.getSize() : 20, 100), Sort.by(direction, sortField));
        Specification<Supplier> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (filter.getActive() != null) predicates.add(cb.equal(root.get("active"), filter.getActive()));
            String search = filter.getSearch() != null ? filter.getSearch().trim() : null;
            if (search != null && !search.isEmpty()) {
                String pattern = "%" + search + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern.toLowerCase()),
                        cb.like(cb.lower(root.get("tradeName")), pattern.toLowerCase()),
                        cb.like(cb.lower(root.get("document")), pattern.toLowerCase()),
                        cb.like(cb.lower(root.get("email")), pattern.toLowerCase()),
                        cb.like(root.get("phone"), pattern),
                        cb.and(cb.isNotNull(root.get("phoneAlt")), cb.like(root.get("phoneAlt"), pattern)),
                        cb.and(cb.isNotNull(root.get("contactName")), cb.like(cb.lower(root.get("contactName")), pattern.toLowerCase()))
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Supplier> page = supplierRepository.findAll(spec, pageable);
        return PageResponse.<SupplierResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .first(page.isFirst()).last(page.isLast()).build();
    }

    @Transactional
    public SupplierResponse update(UUID id, SupplierUpdateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Supplier supplier = SecurityUtils.isCurrentUserRoot()
                ? supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fornecedor", id))
                : supplierRepository.findByIdAndTenantId(id, SecurityUtils.requireTenantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Fornecedor", id));
        UUID tenantId = supplier.getTenantId();
        if (request.getDocument() != null && !request.getDocument().isBlank()) {
            validateDocumentUnique(tenantId, id, request.getDocument());
        }
        updateEntity(supplier, request);
        supplier.setUpdatedBy(userId);
        supplier = supplierRepository.save(supplier);
        return toResponse(supplier);
    }

    @Transactional
    public void delete(UUID id) {
        Supplier supplier = SecurityUtils.isCurrentUserRoot()
                ? supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fornecedor", id))
                : supplierRepository.findByIdAndTenantId(id, SecurityUtils.requireTenantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Fornecedor", id));
        supplierRepository.delete(supplier);
    }

    private void validateDocumentUnique(UUID tenantId, UUID excludeId, String document) {
        boolean exists = excludeId != null
                ? supplierRepository.existsByTenantIdAndDocumentAndIdNot(tenantId, document, excludeId)
                : supplierRepository.existsByTenantIdAndDocument(tenantId, document);
        if (exists) throw new IllegalArgumentException("Documento já cadastrado: " + document);
    }

    private boolean isValidSortField(String field) {
        return field != null && ALLOWED_SORT_FIELDS.contains(field);
    }

    private Supplier toEntity(SupplierCreateRequest req, UUID tenantId) {
        return Supplier.builder()
                .tenantId(tenantId).name(req.getName().trim())
                .tradeName(req.getTradeName() != null ? req.getTradeName().trim() : null)
                .document(req.getDocument() != null ? req.getDocument().trim() : null)
                .stateRegistration(req.getStateRegistration() != null ? req.getStateRegistration().trim() : null)
                .municipalRegistration(req.getMunicipalRegistration() != null ? req.getMunicipalRegistration().trim() : null)
                .email(req.getEmail() != null ? req.getEmail().trim() : null)
                .phone(req.getPhone() != null ? req.getPhone().trim() : null)
                .phoneAlt(req.getPhoneAlt() != null ? req.getPhoneAlt().trim() : null)
                .addressStreet(req.getAddressStreet() != null ? req.getAddressStreet().trim() : null)
                .addressNumber(req.getAddressNumber() != null ? req.getAddressNumber().trim() : null)
                .addressComplement(req.getAddressComplement() != null ? req.getAddressComplement().trim() : null)
                .addressNeighborhood(req.getAddressNeighborhood() != null ? req.getAddressNeighborhood().trim() : null)
                .addressCity(req.getAddressCity() != null ? req.getAddressCity().trim() : null)
                .addressState(req.getAddressState() != null ? req.getAddressState().trim().toUpperCase() : null)
                .addressZip(req.getAddressZip() != null ? req.getAddressZip().trim() : null)
                .contactName(req.getContactName() != null ? req.getContactName().trim() : null)
                .contactPhone(req.getContactPhone() != null ? req.getContactPhone().trim() : null)
                .contactEmail(req.getContactEmail() != null ? req.getContactEmail().trim() : null)
                .bankName(req.getBankName() != null ? req.getBankName().trim() : null)
                .bankAgency(req.getBankAgency() != null ? req.getBankAgency().trim() : null)
                .bankAccount(req.getBankAccount() != null ? req.getBankAccount().trim() : null)
                .bankPix(req.getBankPix() != null ? req.getBankPix().trim() : null)
                .paymentTerms(req.getPaymentTerms() != null ? req.getPaymentTerms().trim() : null)
                .notes(req.getNotes() != null ? req.getNotes().trim() : null)
                .active(req.getActive() != null ? req.getActive() : true)
                .build();
    }

    private void updateEntity(Supplier s, SupplierUpdateRequest req) {
        s.setName(req.getName().trim());
        s.setTradeName(req.getTradeName() != null ? req.getTradeName().trim() : null);
        s.setDocument(req.getDocument() != null ? req.getDocument().trim() : null);
        s.setStateRegistration(req.getStateRegistration() != null ? req.getStateRegistration().trim() : null);
        s.setMunicipalRegistration(req.getMunicipalRegistration() != null ? req.getMunicipalRegistration().trim() : null);
        s.setEmail(req.getEmail() != null ? req.getEmail().trim() : null);
        s.setPhone(req.getPhone() != null ? req.getPhone().trim() : null);
        s.setPhoneAlt(req.getPhoneAlt() != null ? req.getPhoneAlt().trim() : null);
        s.setAddressStreet(req.getAddressStreet() != null ? req.getAddressStreet().trim() : null);
        s.setAddressNumber(req.getAddressNumber() != null ? req.getAddressNumber().trim() : null);
        s.setAddressComplement(req.getAddressComplement() != null ? req.getAddressComplement().trim() : null);
        s.setAddressNeighborhood(req.getAddressNeighborhood() != null ? req.getAddressNeighborhood().trim() : null);
        s.setAddressCity(req.getAddressCity() != null ? req.getAddressCity().trim() : null);
        s.setAddressState(req.getAddressState() != null ? req.getAddressState().trim().toUpperCase() : null);
        s.setAddressZip(req.getAddressZip() != null ? req.getAddressZip().trim() : null);
        s.setContactName(req.getContactName() != null ? req.getContactName().trim() : null);
        s.setContactPhone(req.getContactPhone() != null ? req.getContactPhone().trim() : null);
        s.setContactEmail(req.getContactEmail() != null ? req.getContactEmail().trim() : null);
        s.setBankName(req.getBankName() != null ? req.getBankName().trim() : null);
        s.setBankAgency(req.getBankAgency() != null ? req.getBankAgency().trim() : null);
        s.setBankAccount(req.getBankAccount() != null ? req.getBankAccount().trim() : null);
        s.setBankPix(req.getBankPix() != null ? req.getBankPix().trim() : null);
        s.setPaymentTerms(req.getPaymentTerms() != null ? req.getPaymentTerms().trim() : null);
        s.setNotes(req.getNotes() != null ? req.getNotes().trim() : null);
        s.setActive(req.getActive() != null ? req.getActive() : true);
    }

    private SupplierResponse toResponse(Supplier s) {
        return SupplierResponse.builder()
                .id(s.getId()).tenantId(s.getTenantId()).name(s.getName()).tradeName(s.getTradeName())
                .document(s.getDocument()).stateRegistration(s.getStateRegistration()).municipalRegistration(s.getMunicipalRegistration())
                .email(s.getEmail()).phone(s.getPhone()).phoneAlt(s.getPhoneAlt())
                .addressStreet(s.getAddressStreet()).addressNumber(s.getAddressNumber())
                .addressComplement(s.getAddressComplement()).addressNeighborhood(s.getAddressNeighborhood())
                .addressCity(s.getAddressCity()).addressState(s.getAddressState()).addressZip(s.getAddressZip())
                .contactName(s.getContactName()).contactPhone(s.getContactPhone()).contactEmail(s.getContactEmail())
                .bankName(s.getBankName()).bankAgency(s.getBankAgency()).bankAccount(s.getBankAccount()).bankPix(s.getBankPix())
                .paymentTerms(s.getPaymentTerms()).notes(s.getNotes()).active(s.getActive()).version(s.getVersion())
                .createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt()).build();
    }
}
