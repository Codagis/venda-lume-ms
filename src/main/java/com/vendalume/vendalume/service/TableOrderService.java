package com.vendalume.vendalume.service;

import com.vendalume.vendalume.api.dto.sale.SaleCreateRequest;
import com.vendalume.vendalume.api.dto.sale.SaleItemRequest;
import com.vendalume.vendalume.api.dto.sale.SaleResponse;
import com.vendalume.vendalume.api.dto.table.*;
import com.vendalume.vendalume.api.exception.ResourceNotFoundException;
import com.vendalume.vendalume.domain.entity.Product;
import com.vendalume.vendalume.domain.entity.RestaurantTable;
import com.vendalume.vendalume.domain.entity.TableOrder;
import com.vendalume.vendalume.domain.entity.TableOrderItem;
import com.vendalume.vendalume.domain.enums.OrderStatus;
import com.vendalume.vendalume.domain.enums.SaleType;
import com.vendalume.vendalume.domain.enums.TableStatus;
import com.vendalume.vendalume.repository.ProductRepository;
import com.vendalume.vendalume.repository.RestaurantTableRepository;
import com.vendalume.vendalume.repository.TableOrderItemRepository;
import com.vendalume.vendalume.repository.TableOrderRepository;
import com.vendalume.vendalume.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço de gestão de comandas e pedidos de mesas.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Service
@RequiredArgsConstructor
public class TableOrderService {

    private final TableOrderRepository tableOrderRepository;
    private final TableOrderItemRepository tableOrderItemRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final ProductRepository productRepository;
    private final SaleService saleService;

    @Transactional
    public OrderResponse openOrder(OrderCreateRequest request) {
        UUID tenantId = resolveTenantId(request.getTenantId());
        UUID tableId = request.getTableId();
        UUID userId = SecurityUtils.getCurrentUserId();

        RestaurantTable table = restaurantTableRepository.findByIdAndTenantId(tableId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa", tableId));

        Optional<TableOrder> existing = tableOrderRepository.findByTenantIdAndTableIdAndStatus(
                tenantId, tableId, OrderStatus.OPEN.name());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Já existe uma comanda aberta para esta mesa.");
        }

        TableOrder order = TableOrder.builder()
                .tenantId(tenantId)
                .tableId(tableId)
                .status(OrderStatus.OPEN)
                .openedAt(Instant.now())
                .closedAt(null)
                .saleId(null)
                .items(new ArrayList<>())
                .build();
        order.setCreatedBy(userId);
        order.setUpdatedBy(userId);
        order = tableOrderRepository.save(order);

        table.setStatus(TableStatus.OCCUPIED);
        table.setUpdatedBy(userId);
        restaurantTableRepository.save(table);

        return toResponse(order, table.getName());
    }

    @Transactional
    public OrderItemResponse addItem(UUID orderId, UUID productId, BigDecimal quantity) {
        TableOrder order = findOrderEntity(orderId);
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new IllegalArgumentException("Não é possível adicionar itens a uma comanda fechada.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", productId));
        if (!product.getTenantId().equals(order.getTenantId())) {
            throw new IllegalArgumentException("Produto não pertence à empresa.");
        }
        if (!Boolean.TRUE.equals(product.getActive()) || !Boolean.TRUE.equals(product.getAvailableForSale())) {
            throw new IllegalArgumentException("Produto não disponível para venda.");
        }

        BigDecimal unitPrice = resolveProductPrice(product);
        int nextOrder = order.getItems() != null ? order.getItems().size() : 0;

        TableOrderItem item = TableOrderItem.builder()
                .order(order)
                .productId(product.getId())
                .quantity(quantity)
                .unitPrice(unitPrice)
                .productName(product.getName())
                .productSku(product.getSku())
                .itemOrder(nextOrder)
                .build();
        item = tableOrderItemRepository.save(item);
        order.getItems().add(item);

        return toItemResponse(item, orderId);
    }

    @Transactional
    public void removeItem(UUID itemId) {
        TableOrderItem item = findItemEntity(itemId);
        TableOrder order = item.getOrder();
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new IllegalArgumentException("Não é possível remover itens de uma comanda fechada.");
        }
        order.getItems().remove(item);
        tableOrderItemRepository.delete(item);
    }

    @Transactional
    public OrderItemResponse updateItemQuantity(UUID itemId, BigDecimal quantity) {
        TableOrderItem item = findItemEntity(itemId);
        TableOrder order = item.getOrder();
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new IllegalArgumentException("Não é possível alterar itens de uma comanda fechada.");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        item.setQuantity(quantity);
        item = tableOrderItemRepository.save(item);
        return toItemResponse(item, order.getId());
    }

    @Transactional
    public OrderResponse closeOrder(UUID orderId, OrderCloseRequest request) {
        TableOrder order = findOrderEntity(orderId);
        if (order.getStatus() == OrderStatus.CLOSED) {
            throw new IllegalArgumentException("Comanda já está fechada.");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Não é possível fechar uma comanda sem itens.");
        }

        List<SaleItemRequest> saleItems = new ArrayList<>();
        for (TableOrderItem i : tableOrderItemRepository.findByOrderIdOrderByItemOrderAsc(order.getId())) {
            saleItems.add(SaleItemRequest.builder()
                    .productId(i.getProductId())
                    .quantity(i.getQuantity())
                    .discountAmount(BigDecimal.ZERO)
                    .discountPercent(null)
                    .build());
        }

        SaleCreateRequest saleReq = SaleCreateRequest.builder()
                .tenantId(order.getTenantId())
                .saleType(SaleType.TAKEAWAY)
                .items(saleItems)
                .paymentMethod(request.getPaymentMethod())
                .amountReceived(request.getAmountReceived())
                .installmentsCount(request.getInstallmentsCount())
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .discountPercent(request.getDiscountPercent())
                .cardMachineId(request.getCardMachineId())
                .cardBrand(request.getCardBrand())
                .cardAuthorization(request.getCardAuthorization())
                .cardIntegrationType(request.getCardIntegrationType())
                .customerName(request.getCustomerName())
                .customerDocument(request.getCustomerDocument())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .notes(request.getNotes())
                .deliveryFee(BigDecimal.ZERO)
                .build();

        SaleResponse sale = saleService.create(saleReq);

        UUID userId = SecurityUtils.getCurrentUserId();
        order.setStatus(OrderStatus.CLOSED);
        order.setClosedAt(Instant.now());
        order.setSaleId(sale.getId());
        order.setUpdatedBy(userId);
        order = tableOrderRepository.save(order);

        RestaurantTable table = restaurantTableRepository.findById(order.getTableId()).orElse(null);
        if (table != null) {
            table.setStatus(TableStatus.AVAILABLE);
            table.setUpdatedBy(userId);
            restaurantTableRepository.save(table);
        }

        return toResponse(order, table != null ? table.getName() : null);
    }

    @Transactional
    public OrderResponse closeOrderAsPending(UUID orderId, OrderClosePendingRequest request) {
        TableOrder order = findOrderEntity(orderId);
        if (order.getStatus() == OrderStatus.CLOSED) {
            throw new IllegalArgumentException("Comanda já está fechada.");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Não é possível fechar uma comanda sem itens.");
        }

        List<SaleItemRequest> saleItems = new ArrayList<>();
        for (TableOrderItem i : tableOrderItemRepository.findByOrderIdOrderByItemOrderAsc(order.getId())) {
            saleItems.add(SaleItemRequest.builder()
                    .productId(i.getProductId())
                    .quantity(i.getQuantity())
                    .discountAmount(BigDecimal.ZERO)
                    .discountPercent(null)
                    .build());
        }

        SaleCreateRequest saleReq = SaleCreateRequest.builder()
                .tenantId(order.getTenantId())
                .saleType(SaleType.TAKEAWAY)
                .status(com.vendalume.vendalume.domain.enums.SaleStatus.OPEN)
                .items(saleItems)
                .paymentMethod(null)
                .amountReceived(null)
                .installmentsCount(null)
                .customerName(request != null ? request.getCustomerName() : null)
                .customerPhone(request != null ? request.getCustomerPhone() : null)
                .customerEmail(request != null ? request.getCustomerEmail() : null)
                .notes(request != null ? request.getNotes() : null)
                .discountAmount(BigDecimal.ZERO)
                .discountPercent(null)
                .deliveryFee(BigDecimal.ZERO)
                .build();

        SaleResponse sale = saleService.create(saleReq);

        UUID userId = SecurityUtils.getCurrentUserId();
        order.setStatus(OrderStatus.CLOSED);
        order.setClosedAt(Instant.now());
        order.setSaleId(sale.getId());
        order.setUpdatedBy(userId);
        order = tableOrderRepository.save(order);

        RestaurantTable table = restaurantTableRepository.findById(order.getTableId()).orElse(null);
        if (table != null) {
            table.setStatus(TableStatus.AVAILABLE);
            table.setUpdatedBy(userId);
            restaurantTableRepository.save(table);
        }

        return toResponse(order, table != null ? table.getName() : null);
    }

    @Transactional
    public void cancelOrder(UUID orderId) {
        TableOrder order = findOrderEntity(orderId);
        if (order.getStatus() == OrderStatus.CLOSED) {
            throw new IllegalArgumentException("Comanda já está fechada.");
        }
        UUID userId = SecurityUtils.getCurrentUserId();
        order.setStatus(OrderStatus.CLOSED);
        order.setClosedAt(Instant.now());
        order.setSaleId(null);
        order.setUpdatedBy(userId);
        tableOrderRepository.save(order);

        RestaurantTable table = restaurantTableRepository.findById(order.getTableId()).orElse(null);
        if (table != null) {
            table.setStatus(TableStatus.AVAILABLE);
            table.setUpdatedBy(userId);
            restaurantTableRepository.save(table);
        }
    }

    @Transactional
    public OrderResponse updateNotes(UUID orderId, OrderNotesUpdateRequest request) {
        TableOrder order = findOrderEntity(orderId);
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new IllegalArgumentException("Não é possível alterar observações de uma comanda fechada.");
        }
        String notes = request.getNotes() != null ? request.getNotes().trim() : null;
        order.setNotes(notes != null && notes.length() > 500 ? notes.substring(0, 500) : notes);
        order.setUpdatedBy(SecurityUtils.getCurrentUserId());
        order = tableOrderRepository.save(order);
        RestaurantTable table = restaurantTableRepository.findById(order.getTableId()).orElse(null);
        return toResponse(order, table != null ? table.getName() : null);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {
        TableOrder order = findOrderEntity(id);
        RestaurantTable table = restaurantTableRepository.findById(order.getTableId()).orElse(null);
        return toResponse(order, table != null ? table.getName() : null);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOpenOrders(UUID requestTenantId) {
        UUID tenantId = resolveTenantId(requestTenantId);
        List<TableOrder> orders = tableOrderRepository.findByTenantIdAndStatus(tenantId, OrderStatus.OPEN.name());
        List<UUID> tableIds = orders.stream().map(TableOrder::getTableId).distinct().toList();
        var tableMap = restaurantTableRepository.findAllById(tableIds).stream()
                .collect(java.util.stream.Collectors.toMap(RestaurantTable::getId, RestaurantTable::getName));
        return orders.stream()
                .map(o -> toResponse(o, tableMap.get(o.getTableId())))
                .toList();
    }

    private TableOrder findOrderEntity(UUID id) {
        if (SecurityUtils.isCurrentUserRoot()) {
            return tableOrderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Comanda", id));
        }
        return tableOrderRepository.findByIdAndTenantId(id, SecurityUtils.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Comanda", id));
    }

    private TableOrderItem findItemEntity(UUID itemId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            return tableOrderItemRepository.findById(itemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Item da comanda", itemId));
        }
        return tableOrderItemRepository.findByIdAndOrderTenantId(itemId, SecurityUtils.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Item da comanda", itemId));
    }

    private UUID resolveTenantId(UUID requestTenantId) {
        if (SecurityUtils.isCurrentUserRoot()) {
            return requestTenantId != null ? requestTenantId : SecurityUtils.getTenantIdOptional()
                    .orElseThrow(() -> new IllegalStateException("Selecione uma empresa."));
        }
        return SecurityUtils.requireTenantId();
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

    private OrderResponse toResponse(TableOrder o, String tableName) {
        List<OrderItemResponse> items = tableOrderItemRepository.findByOrderIdOrderByItemOrderAsc(o.getId()).stream()
                .map(i -> toItemResponse(i, o.getId()))
                .toList();
        return OrderResponse.builder()
                .id(o.getId())
                .tenantId(o.getTenantId())
                .tableId(o.getTableId())
                .tableName(tableName)
                .status(o.getStatus())
                .notes(o.getNotes())
                .openedAt(o.getOpenedAt())
                .closedAt(o.getClosedAt())
                .saleId(o.getSaleId())
                .items(items)
                .version(o.getVersion())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    private OrderItemResponse toItemResponse(TableOrderItem i, UUID orderId) {
        BigDecimal qty = i.getQuantity() != null ? i.getQuantity() : BigDecimal.ZERO;
        BigDecimal unit = i.getUnitPrice() != null ? i.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal total = qty.multiply(unit);
        return OrderItemResponse.builder()
                .id(i.getId())
                .orderId(orderId)
                .productId(i.getProductId())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .productName(i.getProductName())
                .productSku(i.getProductSku())
                .itemOrder(i.getItemOrder())
                .total(total)
                .build();
    }
}
