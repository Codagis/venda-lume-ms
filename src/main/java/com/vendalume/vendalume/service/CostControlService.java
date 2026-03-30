package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.costcontrol.*;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.domain.entity.AccountPayable;
import com.vendalume.vendalume.domain.entity.AccountReceivable;
import com.vendalume.vendalume.domain.entity.Contractor;
import com.vendalume.vendalume.domain.entity.Customer;
import com.vendalume.vendalume.domain.entity.Sale;
import com.vendalume.vendalume.domain.entity.Employee;
import com.vendalume.vendalume.domain.entity.Supplier;
import com.vendalume.vendalume.domain.enums.AccountStatus;
import com.vendalume.vendalume.domain.enums.PaymentMethod;
import com.vendalume.vendalume.repository.AccountPayableRepository;
import com.vendalume.vendalume.repository.AccountReceivableRepository;
import com.vendalume.vendalume.repository.ContractorInvoiceRepository;
import com.vendalume.vendalume.repository.ContractorRepository;
import com.vendalume.vendalume.repository.CustomerRepository;
import com.vendalume.vendalume.repository.EmployeeRepository;
import com.vendalume.vendalume.repository.SaleRepository;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Serviço de controle de custos (contas a pagar e contas a receber).
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Service
@RequiredArgsConstructor
public class CostControlService {

    private static final List<String> AP_SORT_FIELDS = List.of("dueDate", "amount", "description", "status", "createdAt");
    private static final List<String> AR_SORT_FIELDS = List.of("dueDate", "amount", "description", "status", "createdAt");

    private final AccountPayableRepository apRepository;
    private final AccountReceivableRepository arRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractorRepository contractorRepository;
    private final ContractorInvoiceRepository contractorInvoiceRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;

    @Transactional
    public AccountPayableResponse createPayable(AccountPayableCreateRequest request) {
        UUID tenantId = resolveTenantIdForCreate(request.getTenantId());
        UUID userId = SecurityUtils.getCurrentUserId();
        AccountPayable ap = toPayableEntity(request, tenantId);
        ap.setCreatedBy(userId);
        ap.setUpdatedBy(userId);
        ap = apRepository.save(ap);
        return toPayableResponse(ap);
    }

    @Transactional(readOnly = true)
    public AccountPayableResponse findPayableById(UUID id) {
        UUID tenantId = SecurityUtils.requireTenantId();
        AccountPayable ap = apRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a pagar", id));
        return toPayableResponse(ap);
    }

    @Transactional(readOnly = true)
    public PageResponse<AccountPayableResponse> searchPayables(UUID requestTenantId, AccountPayableFilterRequest filter) {
        UUID tenantId = resolveTenantId(requestTenantId, filter.getTenantId());
        String sortField = AP_SORT_FIELDS.contains(filter.getSortBy() != null ? filter.getSortBy() : "") ? filter.getSortBy() : "dueDate";
        Sort.Direction dir = "desc".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(filter.getPage() != null ? filter.getPage() : 0,
                Math.min(filter.getSize() != null ? filter.getSize() : 20, 100), Sort.by(dir, sortField));
        Specification<AccountPayable> spec = buildPayableSpec(tenantId, filter);
        Page<AccountPayable> page = apRepository.findAll(spec, pageable);
        return PageResponse.<AccountPayableResponse>builder()
                .content(page.getContent().stream().map(this::toPayableResponse).toList())
                .page(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .first(page.isFirst()).last(page.isLast()).build();
    }

    @Transactional
    public AccountPayableResponse updatePayable(UUID id, AccountPayableUpdateRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        AccountPayable ap = apRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a pagar", id));
        updatePayableEntity(ap, request);
        ap.setUpdatedBy(SecurityUtils.getCurrentUserId());
        ap = apRepository.save(ap);
        return toPayableResponse(ap);
    }

    @Transactional
    public AccountPayableResponse registerPayablePayment(UUID id, PaymentRegistrationRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        AccountPayable ap = apRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a pagar", id));
        if (ap.getStatus() == AccountStatus.PAID || ap.getStatus() == AccountStatus.CANCELLED) {
            throw new IllegalArgumentException("Conta já paga ou cancelada.");
        }
        BigDecimal newPaid = (ap.getPaidAmount() != null ? ap.getPaidAmount() : BigDecimal.ZERO).add(request.getAmount());
        if (newPaid.compareTo(ap.getAmount()) > 0) {
            throw new IllegalArgumentException("Valor do pagamento excede o valor total da conta.");
        }
        ap.setPaidAmount(newPaid);
        ap.setStatus(newPaid.compareTo(ap.getAmount()) >= 0 ? AccountStatus.PAID : AccountStatus.PARTIAL);
        ap.setPaymentDate(request.getPaymentDate());
        ap.setPaymentMethod(request.getPaymentMethod());
        ap.setUpdatedBy(SecurityUtils.getCurrentUserId());
        ap = apRepository.save(ap);
        return toPayableResponse(ap);
    }

    @Transactional
    public void deletePayable(UUID id) {
        UUID tenantId = SecurityUtils.requireTenantId();
        AccountPayable ap = apRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a pagar", id));
        apRepository.delete(ap);
    }

    @Transactional
    public AccountReceivableResponse createReceivable(AccountReceivableCreateRequest request) {
        UUID tenantId = resolveTenantIdForCreate(request.getTenantId());
        UUID userId = SecurityUtils.getCurrentUserId();
        AccountReceivable ar = toReceivableEntity(request, tenantId);
        ar.setCreatedBy(userId);
        ar.setUpdatedBy(userId);
        ar = arRepository.save(ar);
        return toReceivableResponse(ar);
    }

    @Transactional(readOnly = true)
    public AccountReceivableResponse findReceivableById(UUID id) {
        UUID tenantId = SecurityUtils.requireTenantId();
        AccountReceivable ar = arRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a receber", id));
        return toReceivableResponse(ar);
    }

    @Transactional(readOnly = true)
    public PageResponse<AccountReceivableResponse> searchReceivables(UUID requestTenantId, AccountReceivableFilterRequest filter) {
        UUID tenantId = resolveTenantId(requestTenantId, filter.getTenantId());
        String sortField = AR_SORT_FIELDS.contains(filter.getSortBy() != null ? filter.getSortBy() : "") ? filter.getSortBy() : "dueDate";
        Sort.Direction dir = "desc".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(filter.getPage() != null ? filter.getPage() : 0,
                Math.min(filter.getSize() != null ? filter.getSize() : 20, 100), Sort.by(dir, sortField));
        Specification<AccountReceivable> spec = buildReceivableSpec(tenantId, filter);
        Page<AccountReceivable> page = arRepository.findAll(spec, pageable);
        return PageResponse.<AccountReceivableResponse>builder()
                .content(page.getContent().stream().map(this::toReceivableResponse).toList())
                .page(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .first(page.isFirst()).last(page.isLast()).build();
    }

    @Transactional(readOnly = true)
    public List<AccountPayableResponse> getPayablesForReport(UUID requestTenantId, AccountPayableFilterRequest filter) {
        UUID tenantId = resolveTenantId(requestTenantId, filter.getTenantId());
        String sortField = AP_SORT_FIELDS.contains(filter.getSortBy() != null ? filter.getSortBy() : "") ? filter.getSortBy() : "dueDate";
        Sort.Direction dir = "desc".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(0, 5000, Sort.by(dir, sortField));
        Specification<AccountPayable> spec = buildPayableSpec(tenantId, filter);
        return apRepository.findAll(spec, pageable).getContent().stream().map(this::toPayableResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AccountReceivableResponse> getReceivablesForReport(UUID requestTenantId, AccountReceivableFilterRequest filter) {
        UUID tenantId = resolveTenantId(requestTenantId, filter.getTenantId());
        String sortField = AR_SORT_FIELDS.contains(filter.getSortBy() != null ? filter.getSortBy() : "") ? filter.getSortBy() : "dueDate";
        Sort.Direction dir = "desc".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(0, 5000, Sort.by(dir, sortField));
        Specification<AccountReceivable> spec = buildReceivableSpec(tenantId, filter);
        return arRepository.findAll(spec, pageable).getContent().stream().map(this::toReceivableResponse).toList();
    }

    @Transactional
    public AccountReceivableResponse updateReceivable(UUID id, AccountReceivableUpdateRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        AccountReceivable ar = arRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a receber", id));
        updateReceivableEntity(ar, request);
        ar.setUpdatedBy(SecurityUtils.getCurrentUserId());
        ar = arRepository.save(ar);
        return toReceivableResponse(ar);
    }

    @Transactional
    public AccountReceivableResponse registerReceivablePayment(UUID id, PaymentRegistrationRequest request) {
        UUID tenantId = SecurityUtils.requireTenantId();
        AccountReceivable ar = arRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a receber", id));
        if (ar.getStatus() == AccountStatus.PAID || ar.getStatus() == AccountStatus.CANCELLED) {
            throw new IllegalArgumentException("Conta já recebida ou cancelada.");
        }
        BigDecimal newReceived = (ar.getReceivedAmount() != null ? ar.getReceivedAmount() : BigDecimal.ZERO).add(request.getAmount());
        if (newReceived.compareTo(ar.getAmount()) > 0) {
            throw new IllegalArgumentException("Valor do recebimento excede o valor total da conta.");
        }
        ar.setReceivedAmount(newReceived);
        ar.setStatus(newReceived.compareTo(ar.getAmount()) >= 0 ? AccountStatus.PAID : AccountStatus.PARTIAL);
        ar.setReceiptDate(request.getPaymentDate());
        ar.setPaymentMethod(request.getPaymentMethod());
        ar.setUpdatedBy(SecurityUtils.getCurrentUserId());
        ar = arRepository.save(ar);
        return toReceivableResponse(ar);
    }

    @Transactional
    public void deleteReceivable(UUID id) {
        UUID tenantId = SecurityUtils.requireTenantId();
        AccountReceivable ar = arRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a receber", id));
        arRepository.delete(ar);
    }

    private UUID resolveTenantIdForCreate(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            if (requestTenantId == null) {
                throw new IllegalArgumentException("Selecione a empresa para cadastrar a conta.");
            }
            return requestTenantId;
        }
        return SecurityUtils.requireTenantId();
    }

    private UUID resolveTenantId(UUID requestTenantId, UUID filterTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            UUID chosen = requestTenantId != null ? requestTenantId : filterTenantId;
            return chosen != null ? chosen : SecurityUtils.getTenantIdOptional().orElseThrow(() -> new IllegalStateException("Selecione uma empresa."));
        }
        return SecurityUtils.requireTenantId();
    }

    private Specification<AccountPayable> buildPayableSpec(UUID tenantId, AccountPayableFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.equal(root.get("tenantId"), tenantId));
            if (filter.getStatus() != null) preds.add(cb.equal(root.get("status"), filter.getStatus()));
            if (filter.getSupplierId() != null) preds.add(cb.equal(root.get("supplierId"), filter.getSupplierId()));
            if (filter.getEmployeeId() != null) preds.add(cb.equal(root.get("employeeId"), filter.getEmployeeId()));
            if (filter.getContractorId() != null) preds.add(cb.equal(root.get("contractorId"), filter.getContractorId()));
            if (filter.getDueDateFrom() != null) preds.add(cb.greaterThanOrEqualTo(root.get("dueDate"), filter.getDueDateFrom()));
            if (filter.getDueDateTo() != null) preds.add(cb.lessThanOrEqualTo(root.get("dueDate"), filter.getDueDateTo()));
            String search = filter.getSearch() != null ? filter.getSearch().trim() : null;
            if (search != null && !search.isEmpty()) {
                String p = "%" + search + "%";
                preds.add(cb.or(
                        cb.like(cb.lower(root.get("description")), p.toLowerCase()),
                        cb.like(cb.lower(root.get("reference")), p.toLowerCase()),
                        cb.like(cb.lower(root.get("category")), p.toLowerCase())
                ));
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };
    }

    private Specification<AccountReceivable> buildReceivableSpec(UUID tenantId, AccountReceivableFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.equal(root.get("tenantId"), tenantId));
            if (filter.getStatus() != null) preds.add(cb.equal(root.get("status"), filter.getStatus()));
            if (filter.getCustomerId() != null) preds.add(cb.equal(root.get("customerId"), filter.getCustomerId()));
            if (filter.getDueDateFrom() != null) preds.add(cb.greaterThanOrEqualTo(root.get("dueDate"), filter.getDueDateFrom()));
            if (filter.getDueDateTo() != null) preds.add(cb.lessThanOrEqualTo(root.get("dueDate"), filter.getDueDateTo()));
            String search = filter.getSearch() != null ? filter.getSearch().trim() : null;
            if (search != null && !search.isEmpty()) {
                String p = "%" + search + "%";
                preds.add(cb.or(
                        cb.like(cb.lower(root.get("description")), p.toLowerCase()),
                        cb.like(cb.lower(root.get("reference")), p.toLowerCase()),
                        cb.like(cb.lower(root.get("category")), p.toLowerCase())
                ));
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };
    }

    private AccountPayable toPayableEntity(AccountPayableCreateRequest req, UUID tenantId) {
        AccountStatus status = computeStatus(req.getAmount(), BigDecimal.ZERO, req.getDueDate());
        return AccountPayable.builder()
                .tenantId(tenantId).supplierId(req.getSupplierId())
                .employeeId(req.getEmployeeId()).payrollReference(req.getPayrollReference() != null ? req.getPayrollReference().trim() : null)
                .contractorId(req.getContractorId()).contractorInvoiceId(req.getContractorInvoiceId())
                .description(req.getDescription().trim())
                .reference(req.getReference() != null ? req.getReference().trim() : null)
                .category(req.getCategory() != null ? req.getCategory().trim() : null)
                .dueDate(req.getDueDate()).amount(req.getAmount())
                .paidAmount(BigDecimal.ZERO).status(status)
                .notes(req.getNotes() != null ? req.getNotes().trim() : null)
                .build();
    }

    private void updatePayableEntity(AccountPayable ap, AccountPayableUpdateRequest req) {
        ap.setDescription(req.getDescription().trim());
        ap.setReference(req.getReference() != null ? req.getReference().trim() : null);
        ap.setCategory(req.getCategory() != null ? req.getCategory().trim() : null);
        ap.setDueDate(req.getDueDate());
        ap.setAmount(req.getAmount());
        ap.setSupplierId(req.getSupplierId());
        if (req.getEmployeeId() != null || req.getPayrollReference() != null) {
            ap.setEmployeeId(req.getEmployeeId());
            ap.setPayrollReference(req.getPayrollReference() != null ? req.getPayrollReference().trim() : null);
        }
        ap.setContractorId(req.getContractorId());
        ap.setContractorInvoiceId(req.getContractorInvoiceId());
        ap.setNotes(req.getNotes() != null ? req.getNotes().trim() : null);
        ap.setStatus(computeStatus(ap.getAmount(), ap.getPaidAmount(), ap.getDueDate()));
    }

    private AccountReceivable toReceivableEntity(AccountReceivableCreateRequest req, UUID tenantId) {
        AccountStatus status = computeStatus(req.getAmount(), BigDecimal.ZERO, req.getDueDate());
        return AccountReceivable.builder()
                .tenantId(tenantId).customerId(req.getCustomerId()).saleId(req.getSaleId())
                .description(req.getDescription().trim())
                .reference(req.getReference() != null ? req.getReference().trim() : null)
                .category(req.getCategory() != null ? req.getCategory().trim() : null)
                .dueDate(req.getDueDate()).amount(req.getAmount())
                .receivedAmount(BigDecimal.ZERO).status(status)
                .notes(req.getNotes() != null ? req.getNotes().trim() : null)
                .build();
    }

    private void updateReceivableEntity(AccountReceivable ar, AccountReceivableUpdateRequest req) {
        ar.setDescription(req.getDescription().trim());
        ar.setReference(req.getReference() != null ? req.getReference().trim() : null);
        ar.setCategory(req.getCategory() != null ? req.getCategory().trim() : null);
        ar.setDueDate(req.getDueDate());
        ar.setAmount(req.getAmount());
        ar.setCustomerId(req.getCustomerId());
        ar.setSaleId(req.getSaleId());
        ar.setNotes(req.getNotes() != null ? req.getNotes().trim() : null);
        ar.setStatus(computeStatus(ar.getAmount(), ar.getReceivedAmount(), ar.getDueDate()));
    }

    private AccountStatus computeStatus(BigDecimal total, BigDecimal paid, LocalDate dueDate) {
        if (paid == null) paid = BigDecimal.ZERO;
        if (total != null && paid.compareTo(total) >= 0) return AccountStatus.PAID;
        if (paid.compareTo(BigDecimal.ZERO) > 0) return AccountStatus.PARTIAL;
        if (dueDate != null && dueDate.isBefore(LocalDate.now())) return AccountStatus.OVERDUE;
        return AccountStatus.PENDING;
    }

    private String supplierName(UUID supplierId) {
        if (supplierId == null) return null;
        return supplierRepository.findById(supplierId).map(Supplier::getName).orElse(null);
    }

    private String employeeName(UUID employeeId) {
        if (employeeId == null) return null;
        return employeeRepository.findById(employeeId).map(Employee::getName).orElse(null);
    }

    private String contractorName(UUID contractorId) {
        if (contractorId == null) return null;
        return contractorRepository.findById(contractorId).map(Contractor::getName).orElse(null);
    }

    private Boolean contractorInvoiceHasFile(UUID tenantId, UUID contractorInvoiceId) {
        if (tenantId == null || contractorInvoiceId == null) return null;
        return contractorInvoiceRepository.findByIdAndTenantId(contractorInvoiceId, tenantId)
                .map(inv -> inv.getFileGcsPath() != null && !inv.getFileGcsPath().isBlank())
                .orElse(Boolean.FALSE);
    }

    private String customerName(UUID customerId) {
        if (customerId == null) return null;
        return customerRepository.findById(customerId).map(Customer::getName).orElse(null);
    }

    private String saleNumber(UUID saleId) {
        if (saleId == null) return null;
        return saleRepository.findById(saleId).map(Sale::getSaleNumber).orElse(null);
    }

    private AccountPayableResponse toPayableResponse(AccountPayable ap) {
        return AccountPayableResponse.builder()
                .id(ap.getId()).tenantId(ap.getTenantId()).supplierId(ap.getSupplierId())
                .supplierName(supplierName(ap.getSupplierId()))
                .employeeId(ap.getEmployeeId()).employeeName(employeeName(ap.getEmployeeId())).payrollReference(ap.getPayrollReference())
                .contractorId(ap.getContractorId()).contractorName(contractorName(ap.getContractorId())).contractorInvoiceId(ap.getContractorInvoiceId())
                .contractorInvoiceHasFile(contractorInvoiceHasFile(ap.getTenantId(), ap.getContractorInvoiceId()))
                .description(ap.getDescription()).reference(ap.getReference()).category(ap.getCategory())
                .dueDate(ap.getDueDate()).amount(ap.getAmount()).paidAmount(ap.getPaidAmount())
                .status(ap.getStatus()).paymentDate(ap.getPaymentDate()).paymentMethod(ap.getPaymentMethod())
                .notes(ap.getNotes()).createdAt(ap.getCreatedAt()).updatedAt(ap.getUpdatedAt())
                .build();
    }

    private AccountReceivableResponse toReceivableResponse(AccountReceivable ar) {
        return AccountReceivableResponse.builder()
                .id(ar.getId()).tenantId(ar.getTenantId()).customerId(ar.getCustomerId())
                .customerName(customerName(ar.getCustomerId())).saleId(ar.getSaleId())
                .saleNumber(saleNumber(ar.getSaleId()))
                .description(ar.getDescription()).reference(ar.getReference()).category(ar.getCategory())
                .dueDate(ar.getDueDate()).amount(ar.getAmount()).receivedAmount(ar.getReceivedAmount())
                .status(ar.getStatus()).receiptDate(ar.getReceiptDate()).paymentMethod(ar.getPaymentMethod())
                .notes(ar.getNotes())
                .build();
    }
}
