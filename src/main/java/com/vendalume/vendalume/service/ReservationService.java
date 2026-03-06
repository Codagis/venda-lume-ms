package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.dto.table.*;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.domain.entity.Reservation;
import com.vendalume.vendalume.domain.entity.RestaurantTable;
import com.vendalume.vendalume.repository.ReservationRepository;
import com.vendalume.vendalume.repository.RestaurantTableRepository;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço de gestão de reservas de mesas.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("scheduledAt", "customerName", "status", "createdAt");
    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository restaurantTableRepository;

    @Transactional
    public ReservationResponse create(ReservationCreateRequest request) {
        UUID tenantId = resolveTenantIdForCreate(request.getTenantId());
        validateTableExists(request.getTableId(), tenantId);
        UUID userId = SecurityUtils.getCurrentUserId();
        Reservation reservation = toEntity(request, tenantId);
        reservation.setCreatedBy(userId);
        reservation.setUpdatedBy(userId);
        reservation = reservationRepository.save(reservation);
        return toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponse findById(UUID id) {
        UUID tenantId = SecurityUtils.requireTenantId();
        Reservation reservation = reservationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
        return toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationForReceipt(UUID id) {
        Reservation reservation = findEntity(id);
        String tableName = loadTableNames(List.of(reservation.getTableId())).get(reservation.getTableId());
        return toResponse(reservation, tableName);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> search(UUID requestTenantId, ReservationFilterRequest filter) {
        UUID tenantId = resolveTenantIdForSearch(requestTenantId, filter.getTenantId());
        String sortField = isValidSortField(filter.getSortBy()) ? filter.getSortBy() : "scheduledAt";
        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(filter.getPage() != null ? filter.getPage() : 0,
                Math.min(filter.getSize() != null ? filter.getSize() : 20, 100), Sort.by(direction, sortField));
        Specification<Reservation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (filter.getTableId() != null) predicates.add(cb.equal(root.get("tableId"), filter.getTableId()));
            if (filter.getStatus() != null) predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            if (filter.getScheduledFrom() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("scheduledAt"), filter.getScheduledFrom()));
            if (filter.getScheduledTo() != null) predicates.add(cb.lessThan(root.get("scheduledAt"), filter.getScheduledTo()));
            String search = filter.getSearch() != null ? filter.getSearch().trim() : null;
            if (search != null && !search.isEmpty()) {
                String pattern = "%" + search + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("customerName")), pattern.toLowerCase()),
                        cb.and(cb.isNotNull(root.get("customerPhone")), cb.like(root.get("customerPhone"), pattern)),
                        cb.and(cb.isNotNull(root.get("customerEmail")), cb.like(cb.lower(root.get("customerEmail")), pattern.toLowerCase()))
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Reservation> page = reservationRepository.findAll(spec, pageable);
        Map<UUID, String> tableNames = loadTableNames(page.getContent().stream().map(Reservation::getTableId).distinct().toList());
        return PageResponse.<ReservationResponse>builder()
                .content(page.getContent().stream().map(r -> toResponse(r, tableNames.get(r.getTableId()))).toList())
                .page(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .first(page.isFirst()).last(page.isLast()).build();
    }

    @Transactional
    public ReservationResponse update(UUID id, ReservationUpdateRequest request) {
        Reservation reservation = findEntity(id);
        validateTableExists(request.getTableId(), reservation.getTenantId());
        UUID userId = SecurityUtils.getCurrentUserId();
        updateEntity(reservation, request);
        reservation.setUpdatedBy(userId);
        reservation = reservationRepository.save(reservation);
        return toResponse(reservation);
    }

    @Transactional
    public void delete(UUID id) {
        Reservation reservation = findEntity(id);
        reservationRepository.delete(reservation);
    }

    private UUID resolveTenantIdForCreate(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            if (requestTenantId == null) throw new IllegalArgumentException("Selecione a empresa.");
            return requestTenantId;
        }
        return SecurityUtils.requireTenantId();
    }

    private UUID resolveTenantIdForSearch(UUID requestTenantId, UUID filterTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            UUID chosen = requestTenantId != null ? requestTenantId : filterTenantId;
            chosen = chosen != null ? chosen : SecurityUtils.getTenantIdOptional().orElse(null);
            if (chosen == null) throw new IllegalStateException("Selecione uma empresa.");
            return chosen;
        }
        return SecurityUtils.requireTenantId();
    }

    private Reservation findEntity(UUID id) {
        if (SecurityUtils.isCurrentUserRoot()) {
            return reservationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
        }
        return reservationRepository.findByIdAndTenantId(id, SecurityUtils.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
    }

    private void validateTableExists(UUID tableId, UUID tenantId) {
        restaurantTableRepository.findByIdAndTenantId(tableId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa", tableId));
    }

    private boolean isValidSortField(String field) {
        return field != null && ALLOWED_SORT_FIELDS.contains(field);
    }

    private Map<UUID, String> loadTableNames(List<UUID> tableIds) {
        if (tableIds == null || tableIds.isEmpty()) return Map.of();
        return restaurantTableRepository.findAllById(tableIds).stream().collect(Collectors.toMap(RestaurantTable::getId, RestaurantTable::getName));
    }

    private Reservation toEntity(ReservationCreateRequest req, UUID tenantId) {
        return Reservation.builder()
                .tenantId(tenantId)
                .tableId(req.getTableId())
                .customerName(req.getCustomerName().trim())
                .customerPhone(req.getCustomerPhone() != null ? req.getCustomerPhone().trim() : null)
                .customerEmail(req.getCustomerEmail() != null ? req.getCustomerEmail().trim() : null)
                .scheduledAt(req.getScheduledAt())
                .numberOfGuests(req.getNumberOfGuests() != null ? req.getNumberOfGuests() : 1)
                .status(req.getStatus() != null ? req.getStatus() : com.vendalume.vendalume.domain.enums.ReservationStatus.PENDING)
                .notes(req.getNotes() != null ? req.getNotes().trim() : null)
                .build();
    }

    private void updateEntity(Reservation r, ReservationUpdateRequest req) {
        r.setTableId(req.getTableId());
        r.setCustomerName(req.getCustomerName().trim());
        r.setCustomerPhone(req.getCustomerPhone() != null ? req.getCustomerPhone().trim() : null);
        r.setCustomerEmail(req.getCustomerEmail() != null ? req.getCustomerEmail().trim() : null);
        r.setScheduledAt(req.getScheduledAt());
        r.setNumberOfGuests(req.getNumberOfGuests());
        if (req.getStatus() != null) r.setStatus(req.getStatus());
        r.setNotes(req.getNotes() != null ? req.getNotes().trim() : null);
    }

    private ReservationResponse toResponse(Reservation r) {
        return toResponse(r, null);
    }

    private ReservationResponse toResponse(Reservation r, String tableName) {
        return ReservationResponse.builder()
                .id(r.getId()).tenantId(r.getTenantId()).tableId(r.getTableId()).tableName(tableName)
                .customerName(r.getCustomerName()).customerPhone(r.getCustomerPhone()).customerEmail(r.getCustomerEmail())
                .scheduledAt(r.getScheduledAt()).numberOfGuests(r.getNumberOfGuests()).status(r.getStatus()).notes(r.getNotes())
                .version(r.getVersion()).createdAt(r.getCreatedAt()).updatedAt(r.getUpdatedAt()).build();
    }
}
