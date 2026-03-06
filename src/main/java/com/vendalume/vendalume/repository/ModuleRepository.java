package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link Module}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {

    @Query(value = "SELECT * FROM modules WHERE active = true ORDER BY display_order ASC", nativeQuery = true)
    List<Module> findByActiveTrueOrderByDisplayOrderAsc();

    @Query(value = "SELECT * FROM modules WHERE code = :code LIMIT 1", nativeQuery = true)
    Optional<Module> findByCode(@Param("code") String code);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM modules WHERE code = :code)", nativeQuery = true)
    boolean existsByCode(@Param("code") String code);
}
