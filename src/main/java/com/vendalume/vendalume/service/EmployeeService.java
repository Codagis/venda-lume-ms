package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.employee.*;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.domain.entity.Employee;
import com.vendalume.vendalume.repository.EmployeeRepository;
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
public class EmployeeService {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("name", "document", "role", "salary", "paymentDay", "createdAt");
    private final EmployeeRepository employeeRepository;

    @Transactional
    public EmployeeResponse create(EmployeeCreateRequest request) {
        UUID tenantId;
        if (SecurityUtils.isCurrentUserRoot()) {
            if (request.getTenantId() == null) {
                throw new IllegalArgumentException("Selecione a empresa do funcionário.");
            }
            tenantId = request.getTenantId();
        } else {
            tenantId = SecurityUtils.requireTenantId();
        }
        UUID userId = SecurityUtils.getCurrentUserId();
        if (request.getDocument() != null && !request.getDocument().isBlank()) {
            validateDocumentUnique(tenantId, null, request.getDocument());
        }
        Employee employee = toEntity(request, tenantId);
        employee.setCreatedBy(userId);
        employee.setUpdatedBy(userId);
        employee = employeeRepository.save(employee);
        return toResponse(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(UUID id) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Employee employee = SecurityUtils.isCurrentUserRoot()
                ? employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Funcionário", id))
                : employeeRepository.findByIdAndTenantId(id, tenantId).orElseThrow(() -> new ResourceNotFoundException("Funcionário", id));
        return toResponse(employee);
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> search(UUID requestTenantId, EmployeeFilterRequest filter) {
        final UUID tenantId;
        if (SecurityUtils.isCurrentUserRoot()) {
            UUID chosen = requestTenantId != null ? requestTenantId : filter.getTenantId();
            tenantId = chosen != null ? chosen : SecurityUtils.getTenantIdOptional().orElse(null);
            if (tenantId == null) {
                throw new IllegalStateException("Selecione uma empresa para listar os funcionários.");
            }
        } else {
            tenantId = SecurityUtils.requireTenantId();
        }
        String sortField = isValidSortField(filter.getSortBy()) ? filter.getSortBy() : "name";
        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(filter.getPage() != null ? filter.getPage() : 0,
                Math.min(filter.getSize() != null ? filter.getSize() : 20, 100), Sort.by(direction, sortField));
        Specification<Employee> spec = (root, query, cb) -> {
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
                        cb.like(cb.lower(root.get("role")), pattern.toLowerCase())
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Employee> page = employeeRepository.findAll(spec, pageable);
        return PageResponse.<EmployeeResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .first(page.isFirst()).last(page.isLast()).build();
    }

    @Transactional
    public EmployeeResponse update(UUID id, EmployeeUpdateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Employee employee = SecurityUtils.isCurrentUserRoot()
                ? employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Funcionário", id))
                : employeeRepository.findByIdAndTenantId(id, SecurityUtils.requireTenantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Funcionário", id));
        UUID tenantId = employee.getTenantId();
        if (request.getDocument() != null && !request.getDocument().isBlank()) {
            validateDocumentUnique(tenantId, id, request.getDocument());
        }
        updateEntity(employee, request);
        employee.setUpdatedBy(userId);
        employee = employeeRepository.save(employee);
        return toResponse(employee);
    }

    @Transactional
    public void delete(UUID id) {
        Employee employee = SecurityUtils.isCurrentUserRoot()
                ? employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Funcionário", id))
                : employeeRepository.findByIdAndTenantId(id, SecurityUtils.requireTenantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Funcionário", id));
        employeeRepository.delete(employee);
    }

    private void validateDocumentUnique(UUID tenantId, UUID excludeId, String document) {
        boolean exists = excludeId != null
                ? employeeRepository.existsByTenantIdAndDocumentAndIdNot(tenantId, document, excludeId)
                : employeeRepository.existsByTenantIdAndDocument(tenantId, document);
        if (exists) throw new IllegalArgumentException("Documento já cadastrado: " + document);
    }

    private boolean isValidSortField(String field) {
        return field != null && ALLOWED_SORT_FIELDS.contains(field);
    }

    private Employee toEntity(EmployeeCreateRequest req, UUID tenantId) {
        return Employee.builder()
                .tenantId(tenantId)
                .name(req.getName().trim())
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
                .role(req.getRole() != null ? req.getRole().trim() : null)
                .cbo(req.getCbo() != null ? req.getCbo().trim() : null)
                .salary(req.getSalary() != null ? req.getSalary() : java.math.BigDecimal.ZERO)
                .paymentDay(req.getPaymentDay() != null ? req.getPaymentDay() : 5)
                .bankName(req.getBankName() != null ? req.getBankName().trim() : null)
                .bankAgency(req.getBankAgency() != null ? req.getBankAgency().trim() : null)
                .bankAccount(req.getBankAccount() != null ? req.getBankAccount().trim() : null)
                .bankPix(req.getBankPix() != null ? req.getBankPix().trim() : null)
                .hireDate(req.getHireDate())
                .notes(req.getNotes() != null ? req.getNotes().trim() : null)
                .hazardousPayPercent(req.getHazardousPayPercent())
                .overtimeHours(req.getOvertimeHours())
                .overtimeValue(req.getOvertimeValue())
                .dsrValue(req.getDsrValue())
                .healthPlanDeduction(req.getHealthPlanDeduction())
                .inssPercent(req.getInssPercent())
                .irrfValue(req.getIrrfValue())
                .dependentes(req.getDependentes() != null ? req.getDependentes() : 0)
                .active(req.getActive() != null ? req.getActive() : true)
                .contractType(req.getContractType() != null ? req.getContractType().trim().toUpperCase() : "CLT")
                .contractorId(req.getContractorId())
                .build();
    }

    private void updateEntity(Employee e, EmployeeUpdateRequest req) {
        e.setName(req.getName().trim());
        e.setDocument(req.getDocument() != null ? req.getDocument().trim() : null);
        e.setEmail(req.getEmail() != null ? req.getEmail().trim() : null);
        e.setPhone(req.getPhone() != null ? req.getPhone().trim() : null);
        e.setPhoneAlt(req.getPhoneAlt() != null ? req.getPhoneAlt().trim() : null);
        e.setAddressStreet(req.getAddressStreet() != null ? req.getAddressStreet().trim() : null);
        e.setAddressNumber(req.getAddressNumber() != null ? req.getAddressNumber().trim() : null);
        e.setAddressComplement(req.getAddressComplement() != null ? req.getAddressComplement().trim() : null);
        e.setAddressNeighborhood(req.getAddressNeighborhood() != null ? req.getAddressNeighborhood().trim() : null);
        e.setAddressCity(req.getAddressCity() != null ? req.getAddressCity().trim() : null);
        e.setAddressState(req.getAddressState() != null ? req.getAddressState().trim().toUpperCase() : null);
        e.setAddressZip(req.getAddressZip() != null ? req.getAddressZip().trim() : null);
        e.setRole(req.getRole() != null ? req.getRole().trim() : null);
        e.setCbo(req.getCbo() != null ? req.getCbo().trim() : null);
        e.setSalary(req.getSalary() != null ? req.getSalary() : java.math.BigDecimal.ZERO);
        e.setPaymentDay(req.getPaymentDay() != null ? req.getPaymentDay() : 5);
        e.setBankName(req.getBankName() != null ? req.getBankName().trim() : null);
        e.setBankAgency(req.getBankAgency() != null ? req.getBankAgency().trim() : null);
        e.setBankAccount(req.getBankAccount() != null ? req.getBankAccount().trim() : null);
        e.setBankPix(req.getBankPix() != null ? req.getBankPix().trim() : null);
        e.setHireDate(req.getHireDate());
        e.setNotes(req.getNotes() != null ? req.getNotes().trim() : null);
        e.setHazardousPayPercent(req.getHazardousPayPercent());
        e.setOvertimeHours(req.getOvertimeHours());
        e.setOvertimeValue(req.getOvertimeValue());
        e.setDsrValue(req.getDsrValue());
        e.setHealthPlanDeduction(req.getHealthPlanDeduction());
        e.setInssPercent(req.getInssPercent());
        e.setIrrfValue(req.getIrrfValue());
        e.setDependentes(req.getDependentes() != null ? req.getDependentes() : 0);
        e.setActive(req.getActive() != null ? req.getActive() : true);
        e.setContractType(req.getContractType() != null ? req.getContractType().trim().toUpperCase() : "CLT");
        e.setContractorId(req.getContractorId());
    }

    private EmployeeResponse toResponse(Employee e) {
        return EmployeeResponse.builder()
                .id(e.getId()).tenantId(e.getTenantId()).name(e.getName()).document(e.getDocument())
                .email(e.getEmail()).phone(e.getPhone()).phoneAlt(e.getPhoneAlt())
                .addressStreet(e.getAddressStreet()).addressNumber(e.getAddressNumber())
                .addressComplement(e.getAddressComplement()).addressNeighborhood(e.getAddressNeighborhood())
                .addressCity(e.getAddressCity()).addressState(e.getAddressState()).addressZip(e.getAddressZip())
                .role(e.getRole()).cbo(e.getCbo()).salary(e.getSalary()).paymentDay(e.getPaymentDay())
                .bankName(e.getBankName()).bankAgency(e.getBankAgency()).bankAccount(e.getBankAccount()).bankPix(e.getBankPix())
                .hireDate(e.getHireDate()).notes(e.getNotes())
                .hazardousPayPercent(e.getHazardousPayPercent()).overtimeHours(e.getOvertimeHours())
                .overtimeValue(e.getOvertimeValue()).dsrValue(e.getDsrValue())
                .healthPlanDeduction(e.getHealthPlanDeduction()).inssPercent(e.getInssPercent()).irrfValue(e.getIrrfValue())
                .dependentes(e.getDependentes() != null ? e.getDependentes() : 0)
                .active(e.getActive())
                .contractType(e.getContractType() != null ? e.getContractType() : "CLT")
                .contractorId(e.getContractorId())
                .version(e.getVersion()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}
