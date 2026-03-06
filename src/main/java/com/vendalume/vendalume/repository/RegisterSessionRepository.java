package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.RegisterSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link RegisterSession}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-03-05
 */
@Repository
public interface RegisterSessionRepository extends JpaRepository<RegisterSession, UUID> {

    @Query(value = """
            SELECT * FROM register_session
            WHERE register_id = CAST(:registerId AS UUID) AND tenant_id = CAST(:tenantId AS UUID)
            ORDER BY opened_at DESC
            """, nativeQuery = true)
    List<RegisterSession> findByRegisterIdAndTenantIdOrderByOpenedAtDesc(
            @Param("registerId") UUID registerId,
            @Param("tenantId") UUID tenantId);

    @Query(value = """
            SELECT * FROM register_session
            WHERE id = CAST(:id AS UUID) AND tenant_id = CAST(:tenantId AS UUID)
            LIMIT 1
            """, nativeQuery = true)
    Optional<RegisterSession> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = """
            SELECT * FROM register_session
            WHERE register_id = CAST(:registerId AS UUID) AND user_id = CAST(:userId AS UUID) AND closed_at IS NULL
            LIMIT 1
            """, nativeQuery = true)
    Optional<RegisterSession> findOpenByRegisterIdAndUserId(
            @Param("registerId") UUID registerId,
            @Param("userId") UUID userId);
}
