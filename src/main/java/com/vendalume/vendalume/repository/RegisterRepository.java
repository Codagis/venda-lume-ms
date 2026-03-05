package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Register;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegisterRepository extends JpaRepository<Register, UUID> {

    @Query("SELECT r FROM Register r WHERE r.tenantId = :tenantId ORDER BY r.name")
    List<Register> findByTenantIdOrderByName(@Param("tenantId") UUID tenantId);

    @Query("SELECT r FROM Register r WHERE r.tenantId = :tenantId AND r.active = true ORDER BY r.name")
    List<Register> findByTenantIdAndActiveTrueOrderByName(@Param("tenantId") UUID tenantId);

    @Query("SELECT r FROM Register r WHERE r.id = :id AND r.tenantId = :tenantId")
    Optional<Register> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    boolean existsByTenantIdAndName(UUID tenantId, String name);

    boolean existsByTenantIdAndNameAndIdNot(UUID tenantId, String name, UUID id);
}
