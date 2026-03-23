package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.contractor.*;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.domain.entity.Contractor;
import com.vendalume.vendalume.repository.ContractorRepository;
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
public class ContractorService {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("name", "tradeName", "cnpj", "createdAt");
    private final ContractorRepository contractorRepository;

    @Transactional
    public ContractorResponse create(ContractorCreateRequest request) {
        UUID tenantId = resolveTenantId(request.getTenantId());
        UUID userId = SecurityUtils.getCurrentUserId();
        if (request.getCnpj() != null && !request.getCnpj().isBlank()) {
            validateCnpjUnique(tenantId, null, request.getCnpj().trim());
        }
        Contractor contractor = toEntity(request, tenantId);
        contractor.setCreatedBy(userId);
        contractor.setUpdatedBy(userId);
        contractor = contractorRepository.save(contractor);
        return toResponse(contractor);
    }

    @Transactional(readOnly = true)
    public ContractorResponse findById(UUID id) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Contractor contractor = SecurityUtils.isCurrentUserRoot()
                ? contractorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Prestador PJ", id))
                : contractorRepository.findByIdAndTenantId(id, tenantId).orElseThrow(() -> new ResourceNotFoundException("Prestador PJ", id));
        return toResponse(contractor);
    }

    @Transactional(readOnly = true)
    public PageResponse<ContractorResponse> search(UUID requestTenantId, String search, Boolean active, Integer page, Integer size, String sortBy, String sortDirection) {
        UUID tenantId = resolveTenantIdForSearch(requestTenantId);
        String sortField = isValidSortField(sortBy) ? sortBy : "name";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page != null ? page : 0, Math.min(size != null ? size : 20, 100), Sort.by(direction, sortField));
        Specification<Contractor> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (active != null) predicates.add(cb.equal(root.get("active"), active));
            String s = search != null ? search.trim() : null;
            if (s != null && !s.isEmpty()) {
                String pattern = "%" + s + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern.toLowerCase()),
                        cb.like(cb.lower(root.get("tradeName")), pattern.toLowerCase()),
                        cb.like(root.get("cnpj"), pattern),
                        cb.like(cb.lower(root.get("email")), pattern.toLowerCase()),
                        cb.like(root.get("phone"), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Contractor> result = contractorRepository.findAll(spec, pageable);
        return PageResponse.<ContractorResponse>builder()
                .content(result.getContent().stream().map(this::toResponse).toList())
                .page(result.getNumber()).size(result.getSize())
                .totalElements(result.getTotalElements()).totalPages(result.getTotalPages())
                .first(result.isFirst()).last(result.isLast()).build();
    }

    @Transactional(readOnly = true)
    public List<ContractorResponse> listActive(UUID tenantIdParam) {
        UUID tenantId = tenantIdParam != null && SecurityUtils.isCurrentUserRoot() ? tenantIdParam : SecurityUtils.requireTenantId();
        return contractorRepository.findByTenantIdAndActiveTrueOrderByName(tenantId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ContractorResponse update(UUID id, ContractorUpdateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Contractor contractor = SecurityUtils.isCurrentUserRoot()
                ? contractorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Prestador PJ", id))
                : contractorRepository.findByIdAndTenantId(id, SecurityUtils.requireTenantId()).orElseThrow(() -> new ResourceNotFoundException("Prestador PJ", id));
        UUID tenantId = contractor.getTenantId();
        if (request.getCnpj() != null && !request.getCnpj().isBlank()) {
            validateCnpjUnique(tenantId, id, request.getCnpj().trim());
        }
        updateEntity(contractor, request);
        contractor.setUpdatedBy(userId);
        contractor = contractorRepository.save(contractor);
        return toResponse(contractor);
    }

    @Transactional
    public void delete(UUID id) {
        Contractor contractor = SecurityUtils.isCurrentUserRoot()
                ? contractorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Prestador PJ", id))
                : contractorRepository.findByIdAndTenantId(id, SecurityUtils.requireTenantId()).orElseThrow(() -> new ResourceNotFoundException("Prestador PJ", id));
        contractorRepository.delete(contractor);
    }

    private UUID resolveTenantId(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            if (requestTenantId == null) throw new IllegalArgumentException("Selecione a empresa do prestador.");
            return requestTenantId;
        }
        return SecurityUtils.requireTenantId();
    }

    private UUID resolveTenantIdForSearch(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            return requestTenantId != null ? requestTenantId : SecurityUtils.getTenantIdOptional().orElseThrow(() -> new IllegalStateException("Selecione uma empresa para listar prestadores."));
        }
        return SecurityUtils.requireTenantId();
    }

    private void validateCnpjUnique(UUID tenantId, UUID excludeId, String cnpj) {
        boolean exists = excludeId != null
                ? contractorRepository.existsByTenantIdAndCnpjAndIdNot(tenantId, cnpj, excludeId)
                : contractorRepository.existsByTenantIdAndCnpj(tenantId, cnpj);
        if (exists) throw new IllegalArgumentException("CNPJ já cadastrado para outro prestador: " + cnpj);
    }

    private boolean isValidSortField(String field) {
        return field != null && ALLOWED_SORT_FIELDS.contains(field);
    }

    private Contractor toEntity(ContractorCreateRequest req, UUID tenantId) {
        return Contractor.builder()
                .tenantId(tenantId)
                .name(req.getName().trim())
                .tradeName(req.getTradeName() != null ? req.getTradeName().trim() : null)
                .cnpj(req.getCnpj() != null ? req.getCnpj().trim() : null)
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
                .bankName(req.getBankName() != null ? req.getBankName().trim() : null)
                .bankAgency(req.getBankAgency() != null ? req.getBankAgency().trim() : null)
                .bankAccount(req.getBankAccount() != null ? req.getBankAccount().trim() : null)
                .bankPix(req.getBankPix() != null ? req.getBankPix().trim() : null)
                .notes(req.getNotes() != null ? req.getNotes().trim() : null)
                .active(req.getActive() != null ? req.getActive() : true)
                .build();
    }

    private void updateEntity(Contractor c, ContractorUpdateRequest req) {
        c.setName(req.getName().trim());
        c.setTradeName(req.getTradeName() != null ? req.getTradeName().trim() : null);
        c.setCnpj(req.getCnpj() != null ? req.getCnpj().trim() : null);
        c.setEmail(req.getEmail() != null ? req.getEmail().trim() : null);
        c.setPhone(req.getPhone() != null ? req.getPhone().trim() : null);
        c.setPhoneAlt(req.getPhoneAlt() != null ? req.getPhoneAlt().trim() : null);
        c.setAddressStreet(req.getAddressStreet() != null ? req.getAddressStreet().trim() : null);
        c.setAddressNumber(req.getAddressNumber() != null ? req.getAddressNumber().trim() : null);
        c.setAddressComplement(req.getAddressComplement() != null ? req.getAddressComplement().trim() : null);
        c.setAddressNeighborhood(req.getAddressNeighborhood() != null ? req.getAddressNeighborhood().trim() : null);
        c.setAddressCity(req.getAddressCity() != null ? req.getAddressCity().trim() : null);
        c.setAddressState(req.getAddressState() != null ? req.getAddressState().trim().toUpperCase() : null);
        c.setAddressZip(req.getAddressZip() != null ? req.getAddressZip().trim() : null);
        c.setBankName(req.getBankName() != null ? req.getBankName().trim() : null);
        c.setBankAgency(req.getBankAgency() != null ? req.getBankAgency().trim() : null);
        c.setBankAccount(req.getBankAccount() != null ? req.getBankAccount().trim() : null);
        c.setBankPix(req.getBankPix() != null ? req.getBankPix().trim() : null);
        c.setNotes(req.getNotes() != null ? req.getNotes().trim() : null);
        c.setActive(req.getActive() != null ? req.getActive() : true);
    }

    private ContractorResponse toResponse(Contractor c) {
        return ContractorResponse.builder()
                .id(c.getId()).tenantId(c.getTenantId()).name(c.getName()).tradeName(c.getTradeName())
                .cnpj(c.getCnpj()).email(c.getEmail()).phone(c.getPhone()).phoneAlt(c.getPhoneAlt())
                .addressStreet(c.getAddressStreet()).addressNumber(c.getAddressNumber())
                .addressComplement(c.getAddressComplement()).addressNeighborhood(c.getAddressNeighborhood())
                .addressCity(c.getAddressCity()).addressState(c.getAddressState()).addressZip(c.getAddressZip())
                .bankName(c.getBankName()).bankAgency(c.getBankAgency()).bankAccount(c.getBankAccount()).bankPix(c.getBankPix())
                .notes(c.getNotes()).active(c.getActive())
                .version(c.getVersion()).createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt())
                .build();
    }
}
