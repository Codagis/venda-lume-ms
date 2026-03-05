package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link Product}.
 * Oferece busca por SKU, barcode, filtros por tenant, categoria, status e busca paginada com suporte a multi-tenancy.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    @Query(value = "SELECT * FROM products WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<Product> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM products WHERE tenant_id = CAST(:tenantId AS UUID) AND sku = :sku LIMIT 1", nativeQuery = true)
    Optional<Product> findByTenantIdAndSku(@Param("tenantId") UUID tenantId, @Param("sku") String sku);

    @Query(value = "SELECT * FROM products WHERE tenant_id = CAST(:tenantId AS UUID) AND barcode = :barcode LIMIT 1", nativeQuery = true)
    Optional<Product> findByTenantIdAndBarcode(@Param("tenantId") UUID tenantId, @Param("barcode") String barcode);

    @Query(value = """
            SELECT * FROM products
            WHERE tenant_id = CAST(:tenantId AS UUID) AND active = :active
            ORDER BY display_order ASC NULLS LAST, name ASC
            """, nativeQuery = true)
    List<Product> findByTenantIdAndActiveOrderByDisplayOrderAscNameAsc(@Param("tenantId") UUID tenantId, @Param("active") Boolean active);

    @Query(value = """
            SELECT * FROM products
            WHERE tenant_id = CAST(:tenantId AS UUID) AND active = :active AND available_for_sale = true
            ORDER BY display_order ASC NULLS LAST, name ASC
            """, nativeQuery = true)
    List<Product> findByTenantIdAndActiveAndAvailableForSaleTrueOrderByDisplayOrderAscNameAsc(
            @Param("tenantId") UUID tenantId, @Param("active") Boolean active);

    @Query(value = """
            SELECT * FROM products
            WHERE tenant_id = CAST(:tenantId AS UUID) AND active = :active AND available_for_delivery = true
            ORDER BY display_order ASC NULLS LAST, name ASC
            """, nativeQuery = true)
    List<Product> findByTenantIdAndActiveAndAvailableForDeliveryTrueOrderByDisplayOrderAscNameAsc(
            @Param("tenantId") UUID tenantId, @Param("active") Boolean active);

    @Query(value = """
            SELECT * FROM products
            WHERE tenant_id = CAST(:tenantId AS UUID) AND active = :active AND featured = true
            ORDER BY display_order ASC NULLS LAST, name ASC
            """, nativeQuery = true)
    List<Product> findByTenantIdAndActiveAndFeaturedTrueOrderByDisplayOrderAscNameAsc(
            @Param("tenantId") UUID tenantId, @Param("active") Boolean active);

    @Query(value = """
            SELECT * FROM products
            WHERE tenant_id = CAST(:tenantId AS UUID) AND category_id = CAST(:categoryId AS UUID) AND active = :active
            ORDER BY display_order ASC NULLS LAST, name ASC
            """, nativeQuery = true)
    List<Product> findByTenantIdAndCategoryIdAndActiveOrderByDisplayOrderAscNameAsc(
            @Param("tenantId") UUID tenantId, @Param("categoryId") UUID categoryId, @Param("active") Boolean active);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM products WHERE tenant_id = CAST(:tenantId AS UUID) AND sku = :sku)", nativeQuery = true)
    boolean existsByTenantIdAndSku(@Param("tenantId") UUID tenantId, @Param("sku") String sku);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM products WHERE tenant_id = CAST(:tenantId AS UUID) AND sku = :sku AND id != CAST(:excludeId AS UUID))", nativeQuery = true)
    boolean existsByTenantIdAndSkuAndIdNot(@Param("tenantId") UUID tenantId, @Param("sku") String sku, @Param("excludeId") UUID excludeId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM products WHERE tenant_id = CAST(:tenantId AS UUID) AND barcode = :barcode)", nativeQuery = true)
    boolean existsByTenantIdAndBarcode(@Param("tenantId") UUID tenantId, @Param("barcode") String barcode);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM products WHERE tenant_id = CAST(:tenantId AS UUID) AND barcode = :barcode AND id != CAST(:excludeId AS UUID))", nativeQuery = true)
    boolean existsByTenantIdAndBarcodeAndIdNot(@Param("tenantId") UUID tenantId, @Param("barcode") String barcode, @Param("excludeId") UUID excludeId);

    @Query(value = "SELECT COUNT(*) FROM products WHERE tenant_id = CAST(:tenantId AS UUID)", nativeQuery = true)
    long countByTenantId(@Param("tenantId") UUID tenantId);

    @Query(value = """
            SELECT * FROM products
            WHERE tenant_id = CAST(:tenantId AS UUID)
            AND active = true
            AND track_stock = true
            AND min_stock IS NOT NULL
            AND stock_quantity < min_stock
            ORDER BY stock_quantity ASC
            """, nativeQuery = true)
    List<Product> findLowStockProducts(@Param("tenantId") UUID tenantId);
}
