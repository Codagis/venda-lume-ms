package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    List<Tenant> findAllByOrderByNameAsc();

    List<Tenant> findByActiveTrueOrderByNameAsc();

    Optional<Tenant> findByDocument(String document);

    Optional<Tenant> findByNameIgnoreCase(String name);

    boolean existsByDocument(String document);
}
