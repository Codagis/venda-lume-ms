package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.product.PageResponse;
import com.vendalume.vendalume.api.dto.table.*;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.domain.entity.RestaurantTable;
import com.vendalume.vendalume.domain.entity.TableSection;
import com.vendalume.vendalume.repository.RestaurantTableRepository;
import com.vendalume.vendalume.repository.TableSectionRepository;
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
 * Serviço de gestão de mesas e seções.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Service
@RequiredArgsConstructor
public class TableService {

    private static final List<String> SECTION_SORT_FIELDS = List.of("name", "displayOrder", "createdAt");
    private static final List<String> TABLE_SORT_FIELDS = List.of("name", "capacity", "status", "createdAt");
    private final TableSectionRepository tableSectionRepository;
    private final RestaurantTableRepository restaurantTableRepository;

    @Transactional
    public TableSectionResponse createSection(TableSectionCreateRequest request) {
        UUID tenantId = resolveTenantIdForCreate(request.getTenantId());
        UUID userId = SecurityUtils.getCurrentUserId();
        TableSection section = toSectionEntity(request, tenantId);
        section.setCreatedBy(userId);
        section.setUpdatedBy(userId);
        section = tableSectionRepository.save(section);
        return toSectionResponse(section);
    }

    @Transactional(readOnly = true)
    public TableSectionResponse findSectionById(UUID id) {
        UUID tenantId = resolveTenantIdForRead();
        TableSection section = tableSectionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Seção", id));
        return toSectionResponse(section);
    }

    @Transactional(readOnly = true)
    public PageResponse<TableSectionResponse> searchSections(UUID requestTenantId, TableSectionFilterRequest filter) {
        UUID tenantId = resolveTenantIdForSearch(requestTenantId, filter.getTenantId());
        String sortField = isValidSectionSortField(filter.getSortBy()) ? filter.getSortBy() : "displayOrder";
        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(filter.getPage() != null ? filter.getPage() : 0,
                Math.min(filter.getSize() != null ? filter.getSize() : 20, 100), Sort.by(direction, sortField));
        Specification<TableSection> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            String search = filter.getSearch() != null ? filter.getSearch().trim() : null;
            if (search != null && !search.isEmpty()) {
                String pattern = "%" + search + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern.toLowerCase()),
                        cb.and(cb.isNotNull(root.get("description")), cb.like(cb.lower(root.get("description")), pattern.toLowerCase()))
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<TableSection> page = tableSectionRepository.findAll(spec, pageable);
        return PageResponse.<TableSectionResponse>builder()
                .content(page.getContent().stream().map(this::toSectionResponse).toList())
                .page(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .first(page.isFirst()).last(page.isLast()).build();
    }

    @Transactional(readOnly = true)
    public List<TableSectionResponse> listSectionsByTenant(UUID requestTenantId) {
        UUID tenantId = resolveTenantIdForSearch(requestTenantId, null);
        return tableSectionRepository.findByTenantIdOrderByDisplayOrderAscNameAsc(tenantId).stream()
                .map(this::toSectionResponse).toList();
    }

    @Transactional
    public TableSectionResponse updateSection(UUID id, TableSectionUpdateRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        TableSection section = findSectionEntity(id);
        updateSectionEntity(section, request);
        section.setUpdatedBy(userId);
        section = tableSectionRepository.save(section);
        return toSectionResponse(section);
    }

    @Transactional
    public void deleteSection(UUID id) {
        TableSection section = findSectionEntity(id);
        long tablesCount = restaurantTableRepository.countBySectionId(section.getId());
        if (tablesCount > 0) {
            throw new IllegalArgumentException("Não é possível excluir a seção. Existem " + tablesCount + " mesa(s) vinculada(s).");
        }
        tableSectionRepository.delete(section);
    }

    @Transactional
    public RestaurantTableResponse createTable(RestaurantTableCreateRequest request) {
        UUID tenantId = resolveTenantIdForCreate(request.getTenantId());
        validateSectionExists(request.getSectionId(), tenantId);
        UUID userId = SecurityUtils.getCurrentUserId();
        RestaurantTable table = toTableEntity(request, tenantId);
        table.setCreatedBy(userId);
        table.setUpdatedBy(userId);
        table = restaurantTableRepository.save(table);
        return toTableResponse(table);
    }

    @Transactional(readOnly = true)
    public RestaurantTableResponse findTableById(UUID id) {
        UUID tenantId = resolveTenantIdForRead();
        RestaurantTable table = restaurantTableRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa", id));
        return toTableResponse(table);
    }

    @Transactional(readOnly = true)
    public PageResponse<RestaurantTableResponse> searchTables(UUID requestTenantId, RestaurantTableFilterRequest filter) {
        UUID tenantId = resolveTenantIdForSearch(requestTenantId, filter.getTenantId());
        String sortField = isValidTableSortField(filter.getSortBy()) ? filter.getSortBy() : "name";
        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(filter.getPage() != null ? filter.getPage() : 0,
                Math.min(filter.getSize() != null ? filter.getSize() : 20, 100), Sort.by(direction, sortField));
        Specification<RestaurantTable> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (filter.getSectionId() != null) predicates.add(cb.equal(root.get("sectionId"), filter.getSectionId()));
            if (filter.getStatus() != null) predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            if (filter.getActive() != null) predicates.add(cb.equal(root.get("active"), filter.getActive()));
            String search = filter.getSearch() != null ? filter.getSearch().trim() : null;
            if (search != null && !search.isEmpty()) {
                String pattern = "%" + search + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern.toLowerCase()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<RestaurantTable> page = restaurantTableRepository.findAll(spec, pageable);
        Map<UUID, String> sectionNames = loadSectionNames(page.getContent().stream().map(RestaurantTable::getSectionId).distinct().toList());
        return PageResponse.<RestaurantTableResponse>builder()
                .content(page.getContent().stream().map(t -> toTableResponse(t, sectionNames.get(t.getSectionId()))).toList())
                .page(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .first(page.isFirst()).last(page.isLast()).build();
    }

    @Transactional(readOnly = true)
    public List<RestaurantTableResponse> listTablesBySection(UUID requestTenantId, UUID sectionId) {
        UUID tenantId = resolveTenantIdForSearch(requestTenantId, null);
        List<RestaurantTable> tables = restaurantTableRepository.findByTenantIdAndSectionIdOrderByNameAsc(tenantId, sectionId);
        Map<UUID, String> sectionNames = loadSectionNames(List.of(sectionId));
        return tables.stream().map(t -> toTableResponse(t, sectionNames.get(t.getSectionId()))).toList();
    }

    @Transactional
    public RestaurantTableResponse updateTable(UUID id, RestaurantTableUpdateRequest request) {
        RestaurantTable table = findTableEntity(id);
        validateSectionExists(request.getSectionId(), table.getTenantId());
        UUID userId = SecurityUtils.getCurrentUserId();
        updateTableEntity(table, request);
        table.setUpdatedBy(userId);
        table = restaurantTableRepository.save(table);
        return toTableResponse(table);
    }

    @Transactional
    public void deleteTable(UUID id) {
        RestaurantTable table = findTableEntity(id);
        restaurantTableRepository.delete(table);
    }

    private UUID resolveTenantIdForCreate(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            if (requestTenantId == null) throw new IllegalArgumentException("Selecione a empresa.");
            return requestTenantId;
        }
        return SecurityUtils.requireTenantId();
    }

    private UUID resolveTenantIdForRead() {
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

    private TableSection findSectionEntity(UUID id) {
        if (SecurityUtils.isCurrentUserRoot()) {
            return tableSectionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Seção", id));
        }
        return tableSectionRepository.findByIdAndTenantId(id, SecurityUtils.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Seção", id));
    }

    private RestaurantTable findTableEntity(UUID id) {
        if (SecurityUtils.isCurrentUserRoot()) {
            return restaurantTableRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Mesa", id));
        }
        return restaurantTableRepository.findByIdAndTenantId(id, SecurityUtils.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Mesa", id));
    }

    private void validateSectionExists(UUID sectionId, UUID tenantId) {
        tableSectionRepository.findByIdAndTenantId(sectionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Seção", sectionId));
    }

    private boolean isValidSectionSortField(String field) {
        return field != null && SECTION_SORT_FIELDS.contains(field);
    }

    private boolean isValidTableSortField(String field) {
        return field != null && TABLE_SORT_FIELDS.contains(field);
    }

    private Map<UUID, String> loadSectionNames(List<UUID> sectionIds) {
        if (sectionIds == null || sectionIds.isEmpty()) return Map.of();
        return tableSectionRepository.findAllById(sectionIds).stream().collect(Collectors.toMap(TableSection::getId, TableSection::getName));
    }

    private TableSection toSectionEntity(TableSectionCreateRequest req, UUID tenantId) {
        return TableSection.builder()
                .tenantId(tenantId)
                .name(req.getName().trim())
                .description(req.getDescription() != null ? req.getDescription().trim() : null)
                .displayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0)
                .build();
    }

    private void updateSectionEntity(TableSection s, TableSectionUpdateRequest req) {
        s.setName(req.getName().trim());
        s.setDescription(req.getDescription() != null ? req.getDescription().trim() : null);
        s.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0);
    }

    private TableSectionResponse toSectionResponse(TableSection s) {
        return TableSectionResponse.builder()
                .id(s.getId()).tenantId(s.getTenantId()).name(s.getName()).description(s.getDescription())
                .displayOrder(s.getDisplayOrder()).version(s.getVersion())
                .createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt()).build();
    }

    private RestaurantTable toTableEntity(RestaurantTableCreateRequest req, UUID tenantId) {
        return RestaurantTable.builder()
                .tenantId(tenantId)
                .sectionId(req.getSectionId())
                .name(req.getName().trim())
                .capacity(req.getCapacity() != null ? req.getCapacity() : 2)
                .status(req.getStatus() != null ? req.getStatus() : com.vendalume.vendalume.domain.enums.TableStatus.AVAILABLE)
                .active(req.getActive() != null ? req.getActive() : true)
                .positionX(req.getPositionX())
                .positionY(req.getPositionY())
                .build();
    }

    private void updateTableEntity(RestaurantTable t, RestaurantTableUpdateRequest req) {
        t.setSectionId(req.getSectionId());
        t.setName(req.getName().trim());
        t.setCapacity(req.getCapacity());
        if (req.getStatus() != null) t.setStatus(req.getStatus());
        if (req.getActive() != null) t.setActive(req.getActive());
        if (req.getPositionX() != null) t.setPositionX(req.getPositionX());
        if (req.getPositionY() != null) t.setPositionY(req.getPositionY());
    }

    private RestaurantTableResponse toTableResponse(RestaurantTable t) {
        return toTableResponse(t, null);
    }

    private RestaurantTableResponse toTableResponse(RestaurantTable t, String sectionName) {
        return RestaurantTableResponse.builder()
                .id(t.getId()).tenantId(t.getTenantId()).sectionId(t.getSectionId()).sectionName(sectionName)
                .name(t.getName()).capacity(t.getCapacity()).status(t.getStatus()).active(t.getActive())
                .positionX(t.getPositionX()).positionY(t.getPositionY())
                .version(t.getVersion()).createdAt(t.getCreatedAt()).updatedAt(t.getUpdatedAt()).build();
    }
}
