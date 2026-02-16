package com.commo.commo.repository;

import com.commo.commo.domain.entity.Sale;
import com.commo.commo.domain.enums.SaleStatus;
import com.commo.commo.domain.enums.SaleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link com.commo.commo.domain.entity.Sale}.
 * Oferece busca por tenant, período, status, tipo, vendedor e cliente.
 *
 * @author Commo
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {

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

    @Query(value = """
            SELECT * FROM sales
            WHERE tenant_id = CAST(:tenantId AS UUID)
            AND (:status IS NULL OR status = CAST(:status AS TEXT))
            AND (:saleType IS NULL OR sale_type = CAST(:saleType AS TEXT))
            AND (:sellerId IS NULL OR seller_id = CAST(:sellerId AS UUID))
            AND (:startDate IS NULL OR sale_date >= :startDate)
            AND (:endDate IS NULL OR sale_date <= :endDate)
            AND (:search IS NULL OR :search = '' OR sale_number ILIKE '%' || :search || '%'
                OR customer_name ILIKE '%' || :search || '%'
                OR customer_document LIKE '%' || :search || '%')
            ORDER BY sale_date DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM sales
            WHERE tenant_id = CAST(:tenantId AS UUID)
            AND (:status IS NULL OR status = CAST(:status AS TEXT))
            AND (:saleType IS NULL OR sale_type = CAST(:saleType AS TEXT))
            AND (:sellerId IS NULL OR seller_id = CAST(:sellerId AS UUID))
            AND (:startDate IS NULL OR sale_date >= :startDate)
            AND (:endDate IS NULL OR sale_date <= :endDate)
            AND (:search IS NULL OR :search = '' OR sale_number ILIKE '%' || :search || '%'
                OR customer_name ILIKE '%' || :search || '%'
                OR customer_document LIKE '%' || :search || '%')
            """,
            nativeQuery = true)
    Page<Sale> searchByTenant(
            @Param("tenantId") UUID tenantId,
            @Param("status") SaleStatus status,
            @Param("saleType") SaleType saleType,
            @Param("sellerId") UUID sellerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("search") String search,
            Pageable pageable);
}
