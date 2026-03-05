package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Delivery;
import com.vendalume.vendalume.domain.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link Delivery}.
 * Oferece busca por tenant, período, status, entregador e venda.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID>, JpaSpecificationExecutor<Delivery> {

    @Query(value = """
            SELECT * FROM deliveries
            WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID)
            LIMIT 1
            """, nativeQuery = true)
    Optional<Delivery> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = """
            SELECT * FROM deliveries
            WHERE tenant_id = CAST(:tenantId AS UUID) AND delivery_number = :deliveryNumber
            LIMIT 1
            """, nativeQuery = true)
    Optional<Delivery> findByTenantIdAndDeliveryNumber(@Param("tenantId") UUID tenantId, @Param("deliveryNumber") String deliveryNumber);

    @Query(value = """
            SELECT * FROM deliveries
            WHERE sale_id = CAST(:saleId AS UUID)
            LIMIT 1
            """, nativeQuery = true)
    Optional<Delivery> findBySaleId(@Param("saleId") UUID saleId);

    @Query(value = """
            SELECT * FROM deliveries
            WHERE tenant_id = CAST(:tenantId AS UUID) AND status = CAST(:status AS TEXT)
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<Delivery> findByTenantIdAndStatus(@Param("tenantId") UUID tenantId, @Param("status") DeliveryStatus status);

    @Query(value = """
            SELECT * FROM deliveries
            WHERE tenant_id = CAST(:tenantId AS UUID) AND delivery_person_id = CAST(:deliveryPersonId AS UUID)
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<Delivery> findByTenantIdAndDeliveryPersonId(@Param("tenantId") UUID tenantId, @Param("deliveryPersonId") UUID deliveryPersonId);

    @Query(value = """
            SELECT * FROM deliveries
            WHERE tenant_id = CAST(:tenantId AS UUID) AND delivery_person_id = CAST(:deliveryPersonId AS UUID)
            AND status = CAST(:status AS TEXT)
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<Delivery> findByTenantIdAndDeliveryPersonIdAndStatus(
            @Param("tenantId") UUID tenantId,
            @Param("deliveryPersonId") UUID deliveryPersonId,
            @Param("status") DeliveryStatus status);

    @Query(value = """
            SELECT * FROM deliveries
            WHERE tenant_id = CAST(:tenantId AS UUID)
            AND created_at >= :startDate AND created_at <= :endDate
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<Delivery> findByTenantIdAndCreatedAtBetween(
            @Param("tenantId") UUID tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = """
            SELECT * FROM deliveries
            WHERE tenant_id = CAST(:tenantId AS UUID) AND status IN ('PENDING', 'ASSIGNED', 'ACCEPTED', 'PICKING_UP', 'PICKED_UP', 'IN_TRANSIT', 'ARRIVED')
            ORDER BY priority DESC, scheduled_at ASC NULLS LAST, created_at ASC
            """, nativeQuery = true)
    List<Delivery> findActiveByTenantId(@Param("tenantId") UUID tenantId);

    @Query(value = """
            SELECT EXISTS(SELECT 1 FROM deliveries
            WHERE tenant_id = CAST(:tenantId AS UUID) AND delivery_number = :deliveryNumber)
            """, nativeQuery = true)
    boolean existsByTenantIdAndDeliveryNumber(@Param("tenantId") UUID tenantId, @Param("deliveryNumber") String deliveryNumber);
}
