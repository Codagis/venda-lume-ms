package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.customer.*;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.domain.entity.Customer;
import com.vendalume.vendalume.repository.CustomerRepository;
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

/**
 * Serviço de gestão de clientes.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("name", "document", "createdAt");
    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse create(CustomerCreateRequest request) {
        UUID tenantId;
        if (SecurityUtils.isCurrentUserRoot()) {
            if (request.getTenantId() == null) {
                throw new IllegalArgumentException("Selecione a empresa do cliente.");
            }
            tenantId = request.getTenantId();
        } else {
            tenantId = SecurityUtils.requireTenantId();
        }
        UUID userId = SecurityUtils.getCurrentUserId();
        if (request.getDocument() != null && !request.getDocument().isBlank()) {
            validateDocumentUnique(tenantId, null, request.getDocument());
        }
        Customer customer = toEntity(request, tenantId);
        customer.setCreatedBy(userId);
        customer.setUpdatedBy(userId);
        customer = customerRepository.save(customer);
        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> search(UUID requestTenantId, CustomerFilterRequest filter) {
        final UUID tenantId;
        if (SecurityUtils.isCurrentUserRoot()) {
            UUID chosen = requestTenantId != null ? requestTenantId : filter.getTenantId();
            tenantId = chosen != null ? chosen : SecurityUtils.getTenantIdOptional().orElse(null);
            if (tenantId == null) {
                throw new IllegalStateException("Selecione uma empresa para listar os clientes.");
            }
        } else {
            tenantId = SecurityUtils.requireTenantId();
        }
        String sortField = isValidSortField(filter.getSortBy()) ? filter.getSortBy() : "name";
        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(filter.getPage() != null ? filter.getPage() : 0,
                Math.min(filter.getSize() != null ? filter.getSize() : 20, 100), Sort.by(direction, sortField));
        Specification<Customer> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (filter.getActive() != null) predicates.add(cb.equal(root.get("active"), filter.getActive()));
            String search = filter.getSearch() != null ? filter.getSearch().trim() : null;
            if (search != null && !search.isEmpty()) {
                String pattern = "%" + search + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern.toLowerCase()),
                        cb.like(cb.lower(root.get("document")), pattern.toLowerCase()),
                        cb.like(cb.lower(root.get("email")), pattern.toLowerCase()),
                        cb.like(root.get("phone"), pattern),
                        cb.and(cb.isNotNull(root.get("phoneAlt")), cb.like(root.get("phoneAlt"), pattern))
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Customer> page = customerRepository.findAll(spec, pageable);
        return PageResponse.<CustomerResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .first(page.isFirst()).last(page.isLast()).build();
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerUpdateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Customer customer = SecurityUtils.isCurrentUserRoot()
                ? customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente", id))
                : customerRepository.findByIdAndTenantId(id, SecurityUtils.requireTenantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        UUID tenantId = customer.getTenantId();
        if (request.getDocument() != null && !request.getDocument().isBlank()) {
            validateDocumentUnique(tenantId, id, request.getDocument());
        }
        updateEntity(customer, request);
        customer.setUpdatedBy(userId);
        customer = customerRepository.save(customer);
        return toResponse(customer);
    }

    @Transactional
    public void delete(UUID id) {
        Customer customer = SecurityUtils.isCurrentUserRoot()
                ? customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente", id))
                : customerRepository.findByIdAndTenantId(id, SecurityUtils.requireTenantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        customerRepository.delete(customer);
    }

    private void validateDocumentUnique(UUID tenantId, UUID excludeId, String document) {
        boolean exists = excludeId != null
                ? customerRepository.existsByTenantIdAndDocumentAndIdNot(tenantId, document, excludeId)
                : customerRepository.existsByTenantIdAndDocument(tenantId, document);
        if (exists) throw new IllegalArgumentException("Documento já cadastrado: " + document);
    }

    private boolean isValidSortField(String field) {
        return field != null && ALLOWED_SORT_FIELDS.contains(field);
    }

    private Customer toEntity(CustomerCreateRequest req, UUID tenantId) {
        return Customer.builder()
                .tenantId(tenantId).name(req.getName().trim())
                .document(req.getDocument() != null ? req.getDocument().trim() : null)
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
                .notes(req.getNotes() != null ? req.getNotes().trim() : null)
                .active(req.getActive() != null ? req.getActive() : true)
                .build();
    }

    private void updateEntity(Customer c, CustomerUpdateRequest req) {
        c.setName(req.getName().trim());
        c.setDocument(req.getDocument() != null ? req.getDocument().trim() : null);
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
        c.setNotes(req.getNotes() != null ? req.getNotes().trim() : null);
        c.setActive(req.getActive() != null ? req.getActive() : true);
    }

    private CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId()).tenantId(c.getTenantId()).name(c.getName()).document(c.getDocument())
                .email(c.getEmail()).phone(c.getPhone()).phoneAlt(c.getPhoneAlt())
                .addressStreet(c.getAddressStreet()).addressNumber(c.getAddressNumber())
                .addressComplement(c.getAddressComplement()).addressNeighborhood(c.getAddressNeighborhood())
                .addressCity(c.getAddressCity()).addressState(c.getAddressState()).addressZip(c.getAddressZip())
                .notes(c.getNotes()).active(c.getActive()).version(c.getVersion())
                .createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt()).build();
    }
}
