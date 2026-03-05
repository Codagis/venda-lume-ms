package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.CardMachine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardMachineRepository extends JpaRepository<CardMachine, UUID> {

    List<CardMachine> findByTenantIdAndActiveTrueOrderByIsDefaultDescNameAsc(UUID tenantId);

    List<CardMachine> findByTenantIdOrderByIsDefaultDescNameAsc(UUID tenantId);

    Optional<CardMachine> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndNameIgnoreCaseAndIdNot(UUID tenantId, String name, UUID excludeId);

    boolean existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name);
}
