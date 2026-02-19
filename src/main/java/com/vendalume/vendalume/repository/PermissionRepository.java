package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    List<Permission> findAllByOrderByModuleAscCodeAsc();

    List<Permission> findByModuleOrderByCodeAsc(String module);

    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);
}
