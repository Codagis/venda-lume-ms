package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Sale;
import com.vendalume.vendalume.domain.enums.SaleStatus;
import com.vendalume.vendalume.domain.enums.SaleType;
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
 * Interface de repositório para operações de persistência da entidade {@link Sale}.
 * Oferece busca por tenant, período, status, tipo, vendedor e cliente.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID>, JpaSpecificationExecutor<Sale> {

    @Query(value = """
            SELECT * FROM sales
            WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID)
            LIMIT 1
            """, nativeQuery = true)
    Optional<Sale> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = """
            SELECT * FROM sales
            WHERE tenant_id = CAST(:tenantId AS UUID) AND sale_number = :saleNumber
            LIMIT 1
            """, nativeQuery = true)
    Optional<Sale> findByTenantIdAndSaleNumber(@Param("tenantId") UUID tenantId, @Param("saleNumber") String saleNumber);

    @Query(value = """
            SELECT * FROM sales
            WHERE tenant_id = CAST(:tenantId AS UUID) AND status = CAST(:status AS TEXT)
            ORDER BY sale_date DESC
            """, nativeQuery = true)
    List<Sale> findByTenantIdAndStatus(@Param("tenantId") UUID tenantId, @Param("status") SaleStatus status);

    @Query(value = """
            SELECT * FROM sales
            WHERE tenant_id = CAST(:tenantId AS UUID) AND sale_type = CAST(:saleType AS TEXT)
            ORDER BY sale_date DESC
            """, nativeQuery = true)
    List<Sale> findByTenantIdAndSaleType(@Param("tenantId") UUID tenantId, @Param("saleType") SaleType saleType);

    @Query(value = """
            SELECT * FROM sales
            WHERE tenant_id = CAST(:tenantId AS UUID)
            AND sale_date >= :startDate AND sale_date <= :endDate
            ORDER BY sale_date DESC
            """, nativeQuery = true)
    List<Sale> findByTenantIdAndSaleDateBetween(
            @Param("tenantId") UUID tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = """
            SELECT * FROM sales
            WHERE tenant_id = CAST(:tenantId AS UUID) AND seller_id = CAST(:sellerId AS UUID)
            ORDER BY sale_date DESC
            """, nativeQuery = true)
    List<Sale> findByTenantIdAndSellerId(@Param("tenantId") UUID tenantId, @Param("sellerId") UUID sellerId);

    @Query(value = """
            SELECT * FROM sales
            WHERE tenant_id = CAST(:tenantId AS UUID) AND customer_id = CAST(:customerId AS UUID)
            ORDER BY sale_date DESC
            """, nativeQuery = true)
    List<Sale> findByTenantIdAndCustomerId(@Param("tenantId") UUID tenantId, @Param("customerId") UUID customerId);

    @Query(value = """
            SELECT EXISTS(SELECT 1 FROM sales
            WHERE tenant_id = CAST(:tenantId AS UUID) AND sale_number = :saleNumber)
            """, nativeQuery = true)
    boolean existsByTenantIdAndSaleNumber(@Param("tenantId") UUID tenantId, @Param("saleNumber") String saleNumber);

    @Query(value = "SELECT COUNT(*) FROM sales WHERE tenant_id = CAST(:tenantId AS UUID)", nativeQuery = true)
    long countByTenantId(@Param("tenantId") UUID tenantId);

    @Query(value = """
            SELECT * FROM sales
            WHERE tenant_id = CAST(:tenantId AS UUID)
            AND register_id = :registerId AND seller_id = CAST(:sellerId AS UUID)
            AND sale_date >= :fromDate AND sale_date <= :toDate
            ORDER BY sale_date ASC
            """, nativeQuery = true)
    List<Sale> findByTenantIdAndRegisterIdAndSellerIdAndSaleDateBetween(
            @Param("tenantId") UUID tenantId,
            @Param("registerId") String registerId,
            @Param("sellerId") UUID sellerId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
}
