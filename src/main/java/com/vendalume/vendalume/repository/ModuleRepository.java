package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {

    List<Module> findByActiveTrueOrderByDisplayOrderAsc();

    Optional<Module> findByCode(String code);

    boolean existsByCode(String code);
}
