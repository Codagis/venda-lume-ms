package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link Permission}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    @Query(value = "SELECT * FROM permissions ORDER BY module ASC, code ASC", nativeQuery = true)
    List<Permission> findAllByOrderByModuleAscCodeAsc();

    @Query(value = "SELECT * FROM permissions WHERE module = :module ORDER BY code ASC", nativeQuery = true)
    List<Permission> findByModuleOrderByCodeAsc(@Param("module") String module);

    @Query(value = "SELECT * FROM permissions WHERE code = :code LIMIT 1", nativeQuery = true)
    Optional<Permission> findByCode(@Param("code") String code);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM permissions WHERE code = :code)", nativeQuery = true)
    boolean existsByCode(@Param("code") String code);
}
