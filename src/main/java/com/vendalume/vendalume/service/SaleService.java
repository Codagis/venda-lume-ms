package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.dto.sale.*;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.domain.entity.Product;
import com.vendalume.vendalume.domain.entity.Sale;
import com.vendalume.vendalume.domain.entity.SaleAudit;
import com.vendalume.vendalume.domain.entity.SaleItem;
import com.vendalume.vendalume.domain.entity.User;
import com.vendalume.vendalume.domain.enums.PaymentMethod;
import com.vendalume.vendalume.domain.enums.SaleAuditEventType;
import com.vendalume.vendalume.domain.enums.SaleStatus;
import com.vendalume.vendalume.domain.enums.SaleType;
import com.vendalume.vendalume.domain.entity.CardMachine;
import com.vendalume.vendalume.domain.entity.Customer;
import com.vendalume.vendalume.domain.entity.Tenant;
import com.vendalume.vendalume.repository.CardMachineRepository;
import com.vendalume.vendalume.repository.CustomerRepository;
import com.vendalume.vendalume.repository.ProductRepository;
import com.vendalume.vendalume.repository.SaleAuditRepository;
import com.vendalume.vendalume.repository.SaleItemRepository;
import com.vendalume.vendalume.repository.SaleRepository;
import com.vendalume.vendalume.repository.TenantRepository;
import com.vendalume.vendalume.repository.UserRepository;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final SaleAuditRepository saleAuditRepository;
    private final CardMachineRepository cardMachineRepository;
    private final StockService stockService;

    @Transactional
    public SaleResponse create(SaleCreateRequest request) {
        UUID tenantId = resolveTenantId(request.getTenantId());
        UUID sellerId = SecurityUtils.getCurrentUserId();

        SaleStatus status = request.getStatus() != null ? request.getStatus() : SaleStatus.COMPLETED;
        if (status == SaleStatus.COMPLETED && request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória quando a venda é registrada como concluída.");
        }

        String saleNumber = generateSaleNumber(tenantId);
        LocalDateTime saleDate = LocalDateTime.now();

        Sale sale = Sale.builder()
                .tenantId(tenantId)
                .saleNumber(saleNumber)
                .saleDate(saleDate)
                .status(status)
                .saleType(request.getSaleType())
                .sellerId(sellerId)
                .customerId(resolveCustomerIdForCreate(request.getCustomerId(), tenantId))
                .customerName(request.getCustomerName() != null ? request.getCustomerName().trim() : null)
                .customerDocument(request.getCustomerDocument() != null ? request.getCustomerDocument().trim() : null)
                .customerPhone(request.getCustomerPhone() != null ? request.getCustomerPhone().trim() : null)
                .customerEmail(request.getCustomerEmail() != null ? request.getCustomerEmail().trim() : null)
                .notes(request.getNotes() != null ? request.getNotes().trim() : null)
                .paymentMethod(request.getPaymentMethod())
                .installmentsCount(status == SaleStatus.OPEN ? null : request.getInstallmentsCount())
                .cardMachineId(status == SaleStatus.OPEN ? null : request.getCardMachineId())
                .cardBrand(status == SaleStatus.OPEN ? null : normalizeCardBrand(request.getCardBrand()))
                .cardAuthorization(status == SaleStatus.OPEN ? null : normalizeCardAuthorization(request.getCardAuthorization()))
                .cardIntegrationType(status == SaleStatus.OPEN ? null : normalizeCardIntegrationType(request.getCardIntegrationType()))
                .deliveryFee(request.getDeliveryFee() != null ? request.getDeliveryFee() : BigDecimal.ZERO)
                .deliveryAddress(request.getDeliveryAddress() != null ? request.getDeliveryAddress().trim() : null)
                .deliveryComplement(request.getDeliveryComplement() != null ? request.getDeliveryComplement().trim() : null)
                .deliveryZipCode(request.getDeliveryZipCode() != null ? request.getDeliveryZipCode().trim() : null)
                .deliveryNeighborhood(request.getDeliveryNeighborhood() != null ? request.getDeliveryNeighborhood().trim() : null)
                .deliveryCity(request.getDeliveryCity() != null ? request.getDeliveryCity().trim() : null)
                .deliveryState(request.getDeliveryState() != null ? request.getDeliveryState().trim().toUpperCase() : null)
                .discountAmount(BigDecimal.ZERO)
                .discountPercent(null)
                .taxAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
        sale.setCreatedBy(sellerId);
        sale.setUpdatedBy(sellerId);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        int order = 0;
        for (SaleItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + itemReq.getProductId()));
            if (!product.getTenantId().equals(tenantId)) {
                throw new IllegalArgumentException("Produto não pertence à empresa da venda.");
            }
            if (!Boolean.TRUE.equals(product.getActive()) || !Boolean.TRUE.equals(product.getAvailableForSale())) {
                throw new IllegalArgumentException("Produto não disponível para venda: " + product.getName());
            }

            BigDecimal unitPrice = resolveProductPrice(product);
            BigDecimal itemDiscountAmt = itemReq.getDiscountAmount() != null ? itemReq.getDiscountAmount() : BigDecimal.ZERO;
            if (itemReq.getDiscountPercent() != null && itemReq.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pct = itemReq.getDiscountPercent().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                itemDiscountAmt = unitPrice.multiply(itemReq.getQuantity()).multiply(pct);
            }
            BigDecimal itemSubtotal = unitPrice.multiply(itemReq.getQuantity()).setScale(4, RoundingMode.HALF_UP);
            BigDecimal itemTaxRate = product.getTaxRate() != null ? product.getTaxRate() : BigDecimal.ZERO;
            BigDecimal itemTax = itemSubtotal.subtract(itemDiscountAmt).multiply(itemTaxRate).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal itemTotal = itemSubtotal.subtract(itemDiscountAmt).add(itemTax);

            SaleItem item = SaleItem.builder()
                    .sale(sale)
                    .product(product)
                    .itemOrder(order++)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .unitOfMeasure(product.getUnitOfMeasure())
                    .discountAmount(itemDiscountAmt)
                    .taxAmount(itemTax)
                    .total(itemTotal)
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .build();
            sale.getItems().add(item);

            subtotal = subtotal.add(itemSubtotal);
            totalDiscount = totalDiscount.add(itemDiscountAmt);
            totalTax = totalTax.add(itemTax);
        }

        BigDecimal saleDiscountAmt = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal saleDiscountPct = request.getDiscountPercent();
        if (saleDiscountPct != null && saleDiscountPct.compareTo(BigDecimal.ZERO) > 0) {
            saleDiscountAmt = subtotal.multiply(saleDiscountPct).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        }
        totalDiscount = totalDiscount.add(saleDiscountAmt);

        BigDecimal deliveryFee = request.getDeliveryFee() != null ? request.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(totalDiscount).add(totalTax).add(deliveryFee);

        sale.setSubtotal(subtotal);
        sale.setDiscountAmount(totalDiscount);
        sale.setDiscountPercent(saleDiscountPct);
        sale.setTaxAmount(totalTax);
        sale.setDeliveryFee(deliveryFee);
        sale.setTotal(total);

        if (status == SaleStatus.OPEN) {
            sale.setAmountPaid(BigDecimal.ZERO);
            sale.setChangeAmount(BigDecimal.ZERO);
        } else {
            BigDecimal amountReceived = request.getAmountReceived() != null ? request.getAmountReceived() : total;
            sale.setAmountPaid(amountReceived.min(total));
            sale.setChangeAmount(amountReceived.subtract(total).max(BigDecimal.ZERO));
        }

        sale = saleRepository.save(sale);

        logSaleAudit(sale.getId(), SaleAuditEventType.CREATED, sellerId, "Venda criada.");

        for (SaleItem item : sale.getItems()) {
            Product p = item.getProduct();
            if (p != null && Boolean.TRUE.equals(p.getTrackStock()) && Boolean.TRUE.equals(p.getDeductStockOnSale())) {
                BigDecimal qtyToDeduct = item.getQuantity() != null ? item.getQuantity().negate() : BigDecimal.ZERO;
                stockService.recordSaleMovement(tenantId, p.getId(), qtyToDeduct, sale.getId(), sale.getSaleNumber());
            }
        }

        return toResponse(sale);
    }

    @Transactional(readOnly = true)
    public SaleResponse getById(UUID id) {
        Sale sale;
        if (SecurityUtils.isCurrentUserRoot()) {
            sale = saleRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Venda", id));
        } else {
            UUID tenantId = SecurityUtils.requireTenantId();
            sale = saleRepository.findByIdAndTenantId(id, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Venda", id));
        }
        return toResponse(sale);
    }

    @Transactional(readOnly = true)
    public PageResponse<SaleResponse> search(UUID requestTenantId, SaleFilterRequest filter) {
        UUID tenantId = resolveTenantIdForSearch(requestTenantId, filter.getTenantId());
        Pageable pageable = PageRequest.of(
                filter.getPage() != null ? filter.getPage() : 0,
                Math.min(filter.getSize() != null ? filter.getSize() : 20, 100),
                Sort.by(Sort.Direction.DESC, "saleDate")
        );
        Specification<Sale> spec = buildSearchSpecification(tenantId, filter, false);
        Page<Sale> page = saleRepository.findAll(spec, pageable);
        return PageResponse.<SaleResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> getSalesForReport(UUID requestTenantId, SaleFilterRequest filter) {
        UUID tenantId = resolveTenantIdForSearch(requestTenantId, filter.getTenantId());
        Specification<Sale> spec = buildSearchSpecification(tenantId, filter, false);
        Pageable pageable = PageRequest.of(0, 5000, Sort.by(Sort.Direction.DESC, "saleDate"));
        List<Sale> sales = saleRepository.findAll(spec, pageable).getContent();
        return sales.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SaleSummaryResponse getSummary(UUID requestTenantId, SaleFilterRequest filter) {
        UUID tenantId = resolveTenantIdForSearch(requestTenantId, filter.getTenantId());
        boolean excludeCancelled = filter.getStatus() == null;
        Specification<Sale> spec = buildSearchSpecification(tenantId, filter, excludeCancelled);
        List<Sale> sales = saleRepository.findAll(spec);
        long count = sales.size();
        BigDecimal totalAmount = sales.stream().map(Sale::getTotal).filter(t -> t != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subtotalAmount = sales.stream().map(Sale::getSubtotal).filter(s -> s != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discountAmount = sales.stream().map(Sale::getDiscountAmount).filter(d -> d != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxAmount = sales.stream().map(Sale::getTaxAmount).filter(t -> t != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deliveryFeeAmount = sales.stream().map(Sale::getDeliveryFee).filter(d -> d != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        return SaleSummaryResponse.builder()
                .count(count)
                .totalAmount(totalAmount)
                .subtotalAmount(subtotalAmount)
                .discountAmount(discountAmount)
                .taxAmount(taxAmount)
                .deliveryFeeAmount(deliveryFeeAmount)
                .build();
    }

    private Specification<Sale> buildSearchSpecification(UUID tenantId, SaleFilterRequest filter, boolean excludeCancelled) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (excludeCancelled) {
                predicates.add(cb.notEqual(root.get("status"), SaleStatus.CANCELLED));
            }
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getSaleType() != null) {
                predicates.add(cb.equal(root.get("saleType"), filter.getSaleType()));
            }
            if (filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("saleDate"), filter.getStartDate()));
            }
            if (filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("saleDate"), filter.getEndDate()));
            }
            String search = filter.getSearch() != null ? filter.getSearch().trim() : null;
            if (search != null && !search.isEmpty()) {
                String pattern = "%" + search + "%";
                Predicate searchPred = cb.or(
                        cb.like(cb.lower(root.get("saleNumber")), pattern.toLowerCase()),
                        cb.like(cb.lower(cb.coalesce(root.get("customerName"), "")), pattern.toLowerCase()),
                        cb.like(cb.lower(cb.coalesce(root.get("customerDocument"), "")), pattern.toLowerCase())
                );
                predicates.add(searchPred);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public SaleResponse cancel(UUID id, String reason) {
        Sale sale;
        if (SecurityUtils.isCurrentUserRoot()) {
            sale = saleRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Venda", id));
        } else {
            UUID tenantId = SecurityUtils.requireTenantId();
            sale = saleRepository.findByIdAndTenantId(id, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Venda", id));
        }
        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new IllegalArgumentException("Venda já está cancelada.");
        }
        sale.setStatus(SaleStatus.CANCELLED);
        sale.setCancellationReason(reason != null ? reason.trim() : null);
        sale.setCancelledAt(LocalDateTime.now());
        sale.setCancelledBy(SecurityUtils.getCurrentUserId());
        sale = saleRepository.save(sale);
        String auditDesc = reason != null && !reason.isBlank()
                ? "Venda cancelada. Motivo: " + reason.trim()
                : "Venda cancelada.";
        logSaleAudit(sale.getId(), SaleAuditEventType.CANCELLED, SecurityUtils.getCurrentUserId(), auditDesc);
        return toResponse(sale);
    }

    @Transactional
    public SaleResponse updateCardAuthorization(UUID id, String cardAuthorization) {
        Sale sale;
        if (SecurityUtils.isCurrentUserRoot()) {
            sale = saleRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Venda", id));
        } else {
            UUID tenantId = SecurityUtils.requireTenantId();
            sale = saleRepository.findByIdAndTenantId(id, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Venda", id));
        }
        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new IllegalArgumentException("Não é possível alterar venda cancelada.");
        }
        if (sale.getPaymentMethod() != PaymentMethod.CREDIT_CARD && sale.getPaymentMethod() != PaymentMethod.DEBIT_CARD) {
            throw new IllegalArgumentException("Código de autorização só se aplica a vendas com pagamento em cartão de crédito ou débito.");
        }
        String normalized = normalizeCardAuthorization(cardAuthorization);
        String current = sale.getCardAuthorization() != null ? sale.getCardAuthorization() : "";
        if ((normalized != null ? normalized : "").equals(current)) {
            return toResponse(sale);
        }
        sale.setCardAuthorization(normalized);
        sale.setUpdatedBy(SecurityUtils.getCurrentUserId());
        sale = saleRepository.save(sale);
        logSaleAudit(sale.getId(), SaleAuditEventType.UPDATED, SecurityUtils.getCurrentUserId(),
                "Código de autorização do cartão alterado.");
        return toResponse(sale);
    }

    @Transactional
    public SaleResponse updateSaleCustomer(UUID id, SaleCustomerUpdateRequest request) {
        Sale sale;
        if (SecurityUtils.isCurrentUserRoot()) {
            sale = saleRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Venda", id));
        } else {
            UUID tenantId = SecurityUtils.requireTenantId();
            sale = saleRepository.findByIdAndTenantId(id, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Venda", id));
        }
        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new IllegalArgumentException("Não é possível alterar venda cancelada.");
        }
        UUID userId = SecurityUtils.getCurrentUserId();
        List<String> changes = new ArrayList<>();
        if (request.getCustomerId() != null) {
            UUID newId = resolveCustomerIdForCreate(request.getCustomerId(), sale.getTenantId());
            if (newId != null && !newId.equals(sale.getCustomerId())) {
                sale.setCustomerId(newId);
                changes.add("Cliente (vínculo) alterado");
            }
        }
        if (request.getCustomerName() != null) {
            String v = request.getCustomerName().trim();
            if (!v.equals(sale.getCustomerName() != null ? sale.getCustomerName() : "")) {
                sale.setCustomerName(v.isEmpty() ? null : v);
                changes.add("Nome do cliente alterado");
            }
        }
        if (request.getCustomerDocument() != null) {
            String v = request.getCustomerDocument().trim();
            String current = sale.getCustomerDocument() != null ? sale.getCustomerDocument() : "";
            if (!v.equals(current)) {
                sale.setCustomerDocument(v.isEmpty() ? null : v);
                changes.add("CPF/CNPJ do cliente alterado");
            }
        }
        if (changes.isEmpty()) {
            return toResponse(sale);
        }
        sale.setUpdatedBy(userId);
        sale = saleRepository.save(sale);
        logSaleAudit(sale.getId(), SaleAuditEventType.UPDATED, userId, "Cliente da venda alterado: " + String.join("; ", changes));
        return toResponse(sale);
    }

    /**
     * Adiciona o pagamento a uma venda com status OPEN e atualiza o status para COMPLETED.
     * Permite gerar notas após o pagamento ser registrado.
     */
    @Transactional
    public SaleResponse addPayment(UUID id, SalePaymentUpdateRequest request) {
        Sale sale;
        if (SecurityUtils.isCurrentUserRoot()) {
            sale = saleRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Venda", id));
        } else {
            UUID tenantId = SecurityUtils.requireTenantId();
            sale = saleRepository.findByIdAndTenantId(id, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Venda", id));
        }
        if (sale.getStatus() != SaleStatus.OPEN) {
            throw new IllegalArgumentException("Só é possível adicionar pagamento em vendas com status Pendente (Aberta). " +
                    "Esta venda está com status " + sale.getStatus().getDescription() + ".");
        }
        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new IllegalArgumentException("Não é possível adicionar pagamento em venda cancelada.");
        }
        BigDecimal subtotal = sale.getSubtotal() != null ? sale.getSubtotal() : BigDecimal.ZERO;
        BigDecimal discountAmt = sale.getDiscountAmount() != null ? sale.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal deliveryFeeVal = sale.getDeliveryFee() != null ? sale.getDeliveryFee() : BigDecimal.ZERO;

        if (request.getDiscountAmount() != null) {
            discountAmt = request.getDiscountAmount();
        } else if (request.getDiscountPercent() != null && request.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            discountAmt = subtotal.multiply(request.getDiscountPercent()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        }
        if (request.getDeliveryFee() != null) {
            deliveryFeeVal = request.getDeliveryFee();
        }

        BigDecimal total = subtotal.subtract(discountAmt).add(deliveryFeeVal).max(BigDecimal.ZERO);
        BigDecimal amountReceived = request.getAmountReceived() != null ? request.getAmountReceived() : total;

        sale.setDiscountAmount(discountAmt);
        sale.setDeliveryFee(deliveryFeeVal);
        sale.setTotal(total);
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setInstallmentsCount(request.getInstallmentsCount());
        sale.setCardMachineId(request.getCardMachineId());
        sale.setCardBrand(normalizeCardBrand(request.getCardBrand()));
        sale.setCardAuthorization(normalizeCardAuthorization(request.getCardAuthorization()));
        sale.setCardIntegrationType(normalizeCardIntegrationType(request.getCardIntegrationType()));
        sale.setAmountPaid(amountReceived.min(total));
        sale.setChangeAmount(amountReceived.subtract(total).max(BigDecimal.ZERO));
        sale.setStatus(SaleStatus.COMPLETED);
        sale.setUpdatedBy(SecurityUtils.getCurrentUserId());
        sale = saleRepository.save(sale);

        logSaleAudit(sale.getId(), SaleAuditEventType.UPDATED, SecurityUtils.getCurrentUserId(),
                "Pagamento adicionado. Venda concluída.");
        return toResponse(sale);
    }

    @Transactional(readOnly = true)
    public List<SaleAuditResponse> getAudit(UUID saleId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            if (saleRepository.findById(saleId).isEmpty()) {
                throw new ResourceNotFoundException("Venda", saleId);
            }
        } else {
            UUID tenantId = SecurityUtils.requireTenantId();
            if (saleRepository.findByIdAndTenantId(saleId, tenantId).isEmpty()) {
                throw new ResourceNotFoundException("Venda", saleId);
            }
        }
        return saleAuditRepository.findBySaleIdOrderByOccurredAtDesc(saleId).stream()
                .map(a -> SaleAuditResponse.builder()
                        .id(a.getId())
                        .eventType(a.getEventType())
                        .occurredAt(a.getOccurredAt())
                        .userName(a.getUserName())
                        .description(a.getDescription())
                        .build())
                .toList();
    }

    private void logSaleAudit(UUID saleId, SaleAuditEventType eventType, UUID userId, String description) {
        String userName = userId != null ? userRepository.findById(userId).map(User::getFullName).orElse(null) : null;
        SaleAudit audit = new SaleAudit();
        audit.setSaleId(saleId);
        audit.setEventType(eventType);
        audit.setOccurredAt(Instant.now());
        audit.setUserId(userId);
        audit.setUserName(userName);
        audit.setDescription(description);
        saleAuditRepository.save(audit);
    }

    private UUID resolveTenantId(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            if (requestTenantId == null) {
                throw new IllegalArgumentException("Selecione a empresa da venda.");
            }
            return requestTenantId;
        }
        return SecurityUtils.requireTenantId();
    }

    /** Valida se o cliente pertence ao tenant e retorna o customerId; caso contrário retorna null. */
    private UUID resolveCustomerIdForCreate(UUID customerId, UUID tenantId) {
        if (customerId == null || tenantId == null) return null;
        return customerRepository.findByIdAndTenantId(customerId, tenantId).map(Customer::getId).orElse(null);
    }

    private UUID resolveTenantIdForSearch(UUID requestTenantId, UUID filterTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            UUID chosen = requestTenantId != null ? requestTenantId : filterTenantId;
            return chosen != null ? chosen : SecurityUtils.getTenantIdOptional()
                    .orElseThrow(() -> new IllegalStateException("Selecione uma empresa para listar as vendas."));
        }
        return SecurityUtils.requireTenantId();
    }

    private String generateSaleNumber(UUID tenantId) {
        long count = saleRepository.countByTenantId(tenantId);
        return String.format("V%06d", count + 1);
    }

    private BigDecimal resolveProductPrice(Product product) {
        if (product.getDiscountPrice() != null && product.getDiscountStartAt() == null && product.getDiscountEndAt() == null) {
            return product.getDiscountPrice();
        }
        if (product.getDiscountPrice() != null && product.getDiscountStartAt() != null && product.getDiscountEndAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (!now.isBefore(product.getDiscountStartAt()) && !now.isAfter(product.getDiscountEndAt())) {
                return product.getDiscountPrice();
            }
        }
        return product.getUnitPrice();
    }

    private SaleResponse toResponse(Sale s) {
        List<SaleItem> rawItems = saleItemRepository.findBySaleIdOrderByItemOrderAsc(s.getId());
        List<SaleItemResponse> items = rawItems.stream()
                .map(this::toItemResponse)
                .toList();
        return SaleResponse.builder()
                .id(s.getId())
                .tenantId(s.getTenantId())
                .saleNumber(s.getSaleNumber())
                .saleDate(s.getSaleDate())
                .status(s.getStatus())
                .saleType(s.getSaleType())
                .customerName(s.getCustomerName())
                .customerDocument(resolveCustomerDocument(s))
                .customerPhone(s.getCustomerPhone())
                .customerEmail(s.getCustomerEmail())
                .deliveryAddress(buildDeliveryAddress(s))
                .deliveryComplement(s.getDeliveryComplement())
                .deliveryZipCode(s.getDeliveryZipCode())
                .deliveryNeighborhood(s.getDeliveryNeighborhood())
                .deliveryCity(s.getDeliveryCity())
                .deliveryState(s.getDeliveryState())
                .cardMachineId(s.getCardMachineId())
                .cardMachineName(resolveCardMachineName(s.getCardMachineId(), s.getTenantId()))
                .invoiceKey(s.getInvoiceKey())
                .invoiceNumber(s.getInvoiceNumber())
                .subtotal(s.getSubtotal())
                .discountAmount(s.getDiscountAmount())
                .discountPercent(s.getDiscountPercent())
                .taxAmount(s.getTaxAmount())
                .deliveryFee(s.getDeliveryFee())
                .total(s.getTotal())
                .amountPaid(s.getAmountPaid())
                .changeAmount(s.getChangeAmount())
                .paymentMethod(s.getPaymentMethod())
                .installmentsCount(s.getInstallmentsCount())
                .cardBrand(s.getCardBrand())
                .cardAuthorization(s.getCardAuthorization())
                .items(items)
                .sellerName(resolveSellerName(s.getSellerId()))
                .notes(s.getNotes())
                .canEmitFiscalReceipt(computeCanEmitFiscalReceipt(s.getTenantId(), rawItems) && hasCardAuthorizationForNfce(s))
                .canEmitSimpleReceipt(computeCanEmitSimpleReceipt(rawItems))
                .canEmitNfe(computeCanEmitNfe(s.getTenantId(), rawItems, s))
                .nfeRequiresCustomerDocument(computeNfeRequiresCustomerDocument(s.getTenantId(), rawItems, s))
                .createdAt(s.getCreatedAt())
                .build();
    }

    private String buildDeliveryAddress(Sale s) {
        if (s.getDeliveryAddress() == null && s.getDeliveryCity() == null) return null;
        List<String> parts = new ArrayList<>();
        if (s.getDeliveryAddress() != null && !s.getDeliveryAddress().isBlank()) parts.add(s.getDeliveryAddress());
        if (s.getDeliveryComplement() != null && !s.getDeliveryComplement().isBlank()) parts.add(s.getDeliveryComplement());
        if (s.getDeliveryNeighborhood() != null && !s.getDeliveryNeighborhood().isBlank()) parts.add(s.getDeliveryNeighborhood());
        if (s.getDeliveryCity() != null || s.getDeliveryState() != null) {
            String cityState = (s.getDeliveryCity() != null ? s.getDeliveryCity() : "")
                    + (s.getDeliveryState() != null && !s.getDeliveryState().isBlank() ? (s.getDeliveryCity() != null ? "/" : "") + s.getDeliveryState() : "");
            if (!cityState.isBlank()) parts.add(cityState);
        }
        if (s.getDeliveryZipCode() != null && !s.getDeliveryZipCode().isBlank()) parts.add("CEP: " + s.getDeliveryZipCode());
        return parts.isEmpty() ? null : String.join(" - ", parts);
    }

    private SaleItemResponse toItemResponse(SaleItem i) {
        return SaleItemResponse.builder()
                .id(i.getId())
                .productId(i.getProduct() != null ? i.getProduct().getId() : null)
                .productName(i.getProductName())
                .productSku(i.getProductSku())
                .quantity(i.getQuantity())
                .unitOfMeasure(i.getUnitOfMeasure())
                .unitPrice(i.getUnitPrice())
                .discountAmount(i.getDiscountAmount())
                .taxAmount(i.getTaxAmount())
                .total(i.getTotal())
                .observations(i.getObservations())
                .build();
    }

    private String resolveSellerName(UUID sellerId) {
        if (sellerId == null) return null;
        return userRepository.findById(sellerId).map(User::getFullName).orElse(null);
    }

    private String resolveCardMachineName(UUID cardMachineId, UUID tenantId) {
        if (cardMachineId == null || tenantId == null) return null;
        return cardMachineRepository.findByIdAndTenantId(cardMachineId, tenantId)
                .map(CardMachine::getName)
                .orElse(null);
    }

    private boolean tenantHasFiscalConfig(UUID tenantId) {
        if (tenantId == null) return false;
        return tenantRepository.findById(tenantId)
                .filter(t -> {
                    boolean ieOk = t.getStateRegistration() != null && !t.getStateRegistration().isBlank();
                    boolean imOk = t.getMunicipalRegistration() != null && !t.getMunicipalRegistration().isBlank();
                    boolean codigoMunOk = t.getCodigoMunicipio() != null && t.getCodigoMunicipio().length() == 7;
                    boolean docOk = t.getDocument() != null && t.getDocument().replaceAll("\\D", "").length() == 14;
                    return (ieOk || imOk) && codigoMunOk && docOk;
                })
                .isPresent();
    }

    private boolean anyItemEmitsNfce(List<SaleItem> items) {
        return items != null && items.stream()
                .anyMatch(i -> i.getProduct() != null && Boolean.TRUE.equals(i.getProduct().getEmitsNfce()));
    }

    private boolean anyItemEmitsNfe(List<SaleItem> items) {
        return items != null && items.stream()
                .anyMatch(i -> i.getProduct() != null && Boolean.TRUE.equals(i.getProduct().getEmitsNfe()));
    }

    private boolean anyItemEmitsComprovanteSimples(List<SaleItem> items) {
        return items != null && items.stream()
                .anyMatch(i -> i.getProduct() != null && Boolean.TRUE.equals(i.getProduct().getEmitsComprovanteSimples()));
    }

    private boolean computeCanEmitFiscalReceipt(UUID tenantId, List<SaleItem> items) {
        return tenantHasFiscalConfig(tenantId) && anyItemEmitsNfce(items);
    }

    /** Para pagamento em cartão (crédito/débito), NFC-e exige código de autorização. */
    private boolean hasCardAuthorizationForNfce(Sale s) {
        if (s.getPaymentMethod() != PaymentMethod.CREDIT_CARD && s.getPaymentMethod() != PaymentMethod.DEBIT_CARD) {
            return true;
        }
        return s.getCardAuthorization() != null && !s.getCardAuthorization().isBlank();
    }

    /** Documento do cliente: da venda, do cadastro (customerId) ou, como fallback, único cliente com mesmo nome no tenant. */
    private String resolveCustomerDocument(Sale sale) {
        if (sale == null) return null;
        if (sale.getCustomerDocument() != null && !sale.getCustomerDocument().isBlank()) return sale.getCustomerDocument();
        if (sale.getCustomerId() != null) {
            String fromCustomer = customerRepository.findByIdAndTenantId(sale.getCustomerId(), sale.getTenantId())
                    .map(Customer::getDocument)
                    .filter(d -> d != null && !d.isBlank())
                    .orElse(null);
            if (fromCustomer != null) return fromCustomer;
        }
        if (sale.getCustomerName() != null && !sale.getCustomerName().isBlank() && sale.getTenantId() != null) {
            List<Customer> byName = customerRepository.findByTenantIdAndNameIgnoreCaseAndDocumentNotNull(sale.getTenantId(), sale.getCustomerName().trim());
            if (byName.size() == 1) return byName.get(0).getDocument();
        }
        return null;
    }

    private boolean hasValidCustomerDocument(Sale sale) {
        String doc = resolveCustomerDocument(sale);
        if (doc == null || doc.isBlank()) return false;
        String digits = doc.replaceAll("\\D", "");
        return digits.length() == 11 || digits.length() == 14;
    }

    private boolean computeCanEmitNfe(UUID tenantId, List<SaleItem> items, Sale sale) {
        return tenantHasFiscalConfig(tenantId) && anyItemEmitsNfe(items) && hasValidCustomerDocument(sale);
    }

    /** true quando a NF-e só não está disponível por falta de CPF/CNPJ do cliente (para exibir alerta no front). */
    private boolean computeNfeRequiresCustomerDocument(UUID tenantId, List<SaleItem> items, Sale sale) {
        return Boolean.FALSE.equals(computeCanEmitNfe(tenantId, items, sale))
                && tenantHasFiscalConfig(tenantId)
                && anyItemEmitsNfe(items)
                && !hasValidCustomerDocument(sale);
    }

    private boolean computeCanEmitSimpleReceipt(List<SaleItem> items) {
        return anyItemEmitsComprovanteSimples(items);
    }

    /** Bandeira para NFC-e: 01=Visa, 02=Master, 03=Amex, 04=Sorocred, 99=Outros. Retorna null se inválido. */
    private static String normalizeCardBrand(String cardBrand) {
        if (cardBrand == null || cardBrand.isBlank()) return null;
        String b = cardBrand.trim();
        return List.of("01", "02", "03", "04", "99").contains(b) ? b : null;
    }

    /** Código de autorização: trim, máx 20 caracteres. */
    private static String normalizeCardAuthorization(String cAut) {
        if (cAut == null || cAut.isBlank()) return null;
        String s = cAut.trim();
        return s.length() > 20 ? s.substring(0, 20) : s;
    }

    /** Tipo de integração: 1 = TEF, 2 = POS. Retorna 2 se null ou inválido. */
    private static Integer normalizeCardIntegrationType(Integer v) {
        if (v == null) return 2;
        return (v == 1 || v == 2) ? v : 2;
    }
}
