package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link Reservation}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID>, JpaSpecificationExecutor<Reservation> {

    @Query(value = "SELECT * FROM reservation WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<Reservation> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM reservation WHERE tenant_id = CAST(:tenantId AS UUID) ORDER BY scheduled_at DESC", nativeQuery = true)
    List<Reservation> findByTenantIdOrderByScheduledAtDesc(@Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM reservation WHERE tenant_id = CAST(:tenantId AS UUID) AND table_id = CAST(:tableId AS UUID) AND scheduled_at >= :fromDate AND scheduled_at < :toDate ORDER BY scheduled_at ASC", nativeQuery = true)
    List<Reservation> findByTenantIdAndTableIdAndScheduledAtBetween(@Param("tenantId") UUID tenantId, @Param("tableId") UUID tableId, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);
}
