package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.delivery.*;
import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.domain.entity.Delivery;
import com.vendalume.vendalume.domain.entity.Sale;
import com.vendalume.vendalume.domain.entity.Tenant;
import com.vendalume.vendalume.domain.entity.User;
import com.vendalume.vendalume.domain.enums.DeliveryPriority;
import com.vendalume.vendalume.domain.enums.DeliveryStatus;
import com.vendalume.vendalume.domain.enums.SaleStatus;
import com.vendalume.vendalume.domain.enums.SaleType;
import com.vendalume.vendalume.domain.enums.UserRole;
import com.vendalume.vendalume.repository.DeliveryRepository;
import com.vendalume.vendalume.repository.SaleRepository;
import com.vendalume.vendalume.repository.TenantRepository;
import com.vendalume.vendalume.repository.UserRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Serviço de gestão de entregas.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final SaleService saleService;

    private UUID resolveTenantId(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            return requestTenantId;
        }
        return SecurityUtils.requireTenantId();
    }

    private UUID resolveTenantIdForSearch(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            if (requestTenantId != null) return requestTenantId;
            return SecurityUtils.getTenantIdOptional().orElseThrow(
                    () -> new IllegalStateException("Selecione uma empresa para listar as entregas."));
        }
        return SecurityUtils.requireTenantId();
    }

    @Transactional(readOnly = true)
    public PageResponse<DeliveryResponse> search(UUID requestTenantId, DeliveryFilterRequest filter) {
        UUID tenantId = resolveTenantIdForSearch(requestTenantId != null ? requestTenantId : filter.getTenantId());

        PageRequest pageable = PageRequest.of(
                filter.getPage() != null ? filter.getPage() : 0,
                Math.min(filter.getSize() != null ? filter.getSize() : 20, 100)
        );

        Specification<Delivery> spec = buildSearchSpec(tenantId, filter);
        Page<Delivery> page = deliveryRepository.findAll(spec, pageable);

        return PageResponse.<DeliveryResponse>builder()
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
    public DeliveryResponse findById(UUID id) {
        Delivery delivery = findDeliveryOrThrow(id);
        return toResponse(delivery);
    }

    @Transactional(readOnly = true)
    public String getTenantOriginAddress(UUID deliveryId) {
        Delivery delivery = findDeliveryOrThrow(deliveryId);
        Tenant t = tenantRepository.findById(delivery.getTenantId()).orElse(null);
        if (t == null) return null;
        List<String> parts = new ArrayList<>();
        if (t.getAddressStreet() != null && !t.getAddressStreet().isBlank()) parts.add(t.getAddressStreet());
        if (t.getAddressNumber() != null && !t.getAddressNumber().isBlank()) parts.add(t.getAddressNumber());
        if (t.getAddressNeighborhood() != null && !t.getAddressNeighborhood().isBlank()) parts.add(t.getAddressNeighborhood());
        if (t.getAddressCity() != null && !t.getAddressCity().isBlank()) parts.add(t.getAddressCity());
        if (t.getAddressState() != null && !t.getAddressState().isBlank()) parts.add(t.getAddressState());
        if (t.getAddressZip() != null && !t.getAddressZip().isBlank()) parts.add(t.getAddressZip());
        return parts.isEmpty() ? null : String.join(", ", parts);
    }

    @Transactional
    public DeliveryResponse create(DeliveryCreateRequest request) {
        UUID tenantId = resolveTenantId(request.getTenantId());
        if (tenantId == null) {
            throw new IllegalStateException("Selecione uma empresa.");
        }

        Sale sale = saleRepository.findById(request.getSaleId())
                .orElseThrow(() -> new ResourceNotFoundException("Venda", request.getSaleId()));
        if (!sale.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Venda não pertence à empresa selecionada.");
        }
        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new IllegalArgumentException("Não é possível criar entrega para venda cancelada.");
        }
        if (deliveryRepository.findBySaleId(sale.getId()).isPresent()) {
            throw new IllegalArgumentException("Já existe uma entrega para esta venda.");
        }

        String deliveryNumber = generateDeliveryNumber(tenantId);
        String recipientName = sale.getCustomerName() != null && !sale.getCustomerName().isBlank()
                ? sale.getCustomerName().trim() : "Cliente";
        String recipientPhone = sale.getCustomerPhone() != null && !sale.getCustomerPhone().isBlank()
                ? sale.getCustomerPhone().trim() : "";

        String address = buildAddressFromSale(sale);
        if (address == null || address.isBlank()) {
            address = "Endereço a confirmar";
        }

        UUID userId = SecurityUtils.getCurrentUserId();

        Delivery delivery = Delivery.builder()
                .tenantId(tenantId)
                .deliveryNumber(deliveryNumber)
                .sale(sale)
                .deliveryPerson(null)
                .status(DeliveryStatus.PENDING)
                .priority(request.getPriority() != null ? request.getPriority() : DeliveryPriority.NORMAL)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .address(address)
                .complement(sale.getDeliveryComplement())
                .zipCode(sale.getDeliveryZipCode())
                .neighborhood(sale.getDeliveryNeighborhood())
                .city(sale.getDeliveryCity())
                .state(sale.getDeliveryState())
                .instructions(request.getInstructions())
                .scheduledAt(request.getScheduledAt())
                .deliveryFee(sale.getDeliveryFee())
                .attemptCount(1)
                .build();
        delivery.setCreatedBy(userId);
        delivery.setUpdatedBy(userId);

        delivery = deliveryRepository.save(delivery);
        return toResponse(delivery);
    }

    @Transactional
    public DeliveryResponse assign(UUID id, DeliveryAssignRequest request) {
        Delivery delivery = findDeliveryOrThrow(id);
        User deliveryPerson = userRepository.findById(request.getDeliveryPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", request.getDeliveryPersonId()));
        if (!deliveryPerson.getTenantId().equals(delivery.getTenantId())) {
            throw new IllegalArgumentException("Entregador deve pertencer à mesma empresa.");
        }
        if (!Boolean.TRUE.equals(deliveryPerson.getActive())) {
            throw new IllegalArgumentException("Entregador deve estar ativo.");
        }

        delivery.setDeliveryPerson(deliveryPerson);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAcceptedAt(LocalDateTime.now());
        delivery.setUpdatedBy(SecurityUtils.getCurrentUserId());
        delivery = deliveryRepository.save(delivery);
        return toResponse(delivery);
    }

    @Transactional
    public DeliveryResponse updateStatus(UUID id, DeliveryStatusUpdateRequest request) {
        Delivery delivery = findDeliveryOrThrow(id);
        DeliveryStatus newStatus = request.getStatus();

        UUID userId = SecurityUtils.getCurrentUserId();
        delivery.setUpdatedBy(userId);

        switch (newStatus) {
            case ACCEPTED -> {
                delivery.setStatus(DeliveryStatus.ACCEPTED);
                delivery.setAcceptedAt(LocalDateTime.now());
            }
            case PICKING_UP -> {
                delivery.setStatus(DeliveryStatus.PICKING_UP);
            }
            case PICKED_UP -> {
                delivery.setStatus(DeliveryStatus.PICKED_UP);
                delivery.setPickedUpAt(LocalDateTime.now());
            }
            case IN_TRANSIT -> {
                delivery.setStatus(DeliveryStatus.IN_TRANSIT);
                delivery.setDepartedAt(LocalDateTime.now());
            }
            case ARRIVED -> {
                delivery.setStatus(DeliveryStatus.ARRIVED);
                delivery.setArrivedAt(LocalDateTime.now());
            }
            case DELIVERED -> {
                delivery.setStatus(DeliveryStatus.DELIVERED);
                delivery.setDeliveredAt(LocalDateTime.now());
                delivery.setReceivedBy(request.getReceivedBy());
                delivery.setDeliveryNotes(request.getDeliveryNotes());
                if (request.getProofOfDeliveryUrl() != null && !request.getProofOfDeliveryUrl().isBlank()) {
                    delivery.setProofOfDeliveryUrl(request.getProofOfDeliveryUrl().trim());
                }
            }
            case FAILED -> {
                delivery.setStatus(DeliveryStatus.FAILED);
                delivery.setFailureReason(request.getFailureReason());
            }
            case RETURNED -> {
                delivery.setStatus(DeliveryStatus.RETURNED);
                delivery.setReturnReason(request.getReturnReason());
            }
            case CANCELLED -> {
                delivery.setStatus(DeliveryStatus.CANCELLED);
                delivery.setCancelledAt(LocalDateTime.now());
                delivery.setCancelledBy(userId);
            }
            default -> throw new IllegalArgumentException("Status inválido: " + newStatus);
        }

        delivery = deliveryRepository.save(delivery);
        return toResponse(delivery);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> listMyDeliveries() {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));
        UUID tenantId = user.getTenantId();
        if (tenantId == null) return List.of();
        return deliveryRepository.findByTenantIdAndDeliveryPersonId(tenantId, userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> listActive(UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        if (tenantId == null) {
            return List.of();
        }
        return deliveryRepository.findActiveByTenantId(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DeliveryPersonOption> listDeliveryPersons(UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        if (tenantId == null) {
            return List.of();
        }
        List<User> users = userRepository.findByTenantIdAndRoleAndActiveTrueOrderByFullNameAsc(tenantId, UserRole.DELIVERY);
        List<User> allActive = userRepository.findByTenantIdOrderByUsernameAsc(tenantId);
        List<User> deliveryCandidates = new ArrayList<>(users);
        for (User u : allActive) {
            if (Boolean.TRUE.equals(u.getActive()) && !deliveryCandidates.contains(u)
                    && (u.getRole() == UserRole.MANAGER || u.getRole() == UserRole.OPERATOR)) {
                deliveryCandidates.add(u);
            }
        }
        return deliveryCandidates.stream()
                .map(u -> DeliveryPersonOption.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .username(u.getUsername())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<com.vendalume.vendalume.api.dto.sale.SaleResponse> listSalesWithoutDelivery(UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        if (tenantId == null) {
            return List.of();
        }
        List<Sale> sales = saleRepository.findByTenantIdAndStatus(tenantId, SaleStatus.COMPLETED);
        List<Sale> withoutDelivery = sales.stream()
                .filter(s -> s.getSaleType() == SaleType.DELIVERY)
                .filter(s -> deliveryRepository.findBySaleId(s.getId()).isEmpty())
                .limit(50)
                .toList();
        return withoutDelivery.stream()
                .map(s -> saleService.getById(s.getId()))
                .toList();
    }

    private Specification<Delivery> buildSearchSpec(UUID tenantId, DeliveryFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getDeliveryPersonId() != null) {
                predicates.add(cb.equal(root.get("deliveryPerson").get("id"), filter.getDeliveryPersonId()));
            }
            if (filter.getStartDate() != null) {
                Instant start = filter.getStartDate().atZone(ZoneId.systemDefault()).toInstant();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (filter.getEndDate() != null) {
                Instant end = filter.getEndDate().atZone(ZoneId.systemDefault()).toInstant();
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }
            String search = filter.getSearch() != null && !filter.getSearch().isBlank() ? filter.getSearch().trim() : null;
            if (search != null && !search.isEmpty()) {
                String pattern = "%" + search + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("deliveryNumber")), pattern.toLowerCase()),
                        cb.like(cb.lower(root.get("recipientName")), pattern.toLowerCase()),
                        cb.like(root.get("recipientPhone"), pattern),
                        cb.like(cb.lower(root.get("address")), pattern.toLowerCase())
                ));
            }

            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Delivery findDeliveryOrThrow(UUID id) {
        if (SecurityUtils.isCurrentUserRoot()) {
            return deliveryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Entrega", id));
        }
        UUID tenantId = SecurityUtils.requireTenantId();
        return deliveryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega", id));
    }

    private String generateDeliveryNumber(UUID tenantId) {
        long count = deliveryRepository.findAll().stream()
                .filter(d -> d.getTenantId().equals(tenantId))
                .count();
        return String.format("ENT%06d", count + 1);
    }

    private String buildAddressFromSale(Sale s) {
        return buildDeliveryAddressForSale(s);
    }

    private String buildDeliveryAddressForSale(Sale s) {
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

    private DeliveryResponse toResponse(Delivery d) {
        Sale sale = d.getSale();
        return DeliveryResponse.builder()
                .id(d.getId())
                .tenantId(d.getTenantId())
                .deliveryNumber(d.getDeliveryNumber())
                .saleId(sale != null ? sale.getId() : null)
                .saleNumber(sale != null ? sale.getSaleNumber() : null)
                .deliveryPersonId(d.getDeliveryPerson() != null ? d.getDeliveryPerson().getId() : null)
                .deliveryPersonName(d.getDeliveryPerson() != null ? d.getDeliveryPerson().getFullName() : null)
                .status(d.getStatus())
                .priority(d.getPriority())
                .recipientName(d.getRecipientName())
                .recipientPhone(d.getRecipientPhone())
                .address(d.getAddress())
                .complement(d.getComplement())
                .zipCode(d.getZipCode())
                .neighborhood(d.getNeighborhood())
                .city(d.getCity())
                .state(d.getState())
                .instructions(d.getInstructions())
                .scheduledAt(d.getScheduledAt())
                .acceptedAt(d.getAcceptedAt())
                .pickedUpAt(d.getPickedUpAt())
                .departedAt(d.getDepartedAt())
                .arrivedAt(d.getArrivedAt())
                .deliveredAt(d.getDeliveredAt())
                .deliveryFee(d.getDeliveryFee())
                .tipAmount(d.getTipAmount())
                .saleTotal(sale != null ? sale.getTotal() : null)
                .failureReason(d.getFailureReason())
                .returnReason(d.getReturnReason())
                .deliveryNotes(d.getDeliveryNotes())
                .receivedBy(d.getReceivedBy())
                .proofOfDeliveryUrl(d.getProofOfDeliveryUrl())
                .createdAt(d.getCreatedAt() != null ? d.getCreatedAt() : Instant.now())
                .updatedAt(d.getUpdatedAt() != null ? d.getUpdatedAt() : Instant.now())
                .build();
    }
}
