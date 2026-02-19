package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    List<Profile> findByTenantIdOrderByNameAsc(UUID tenantId);

    List<Profile> findByTenantIdIsNullOrderByNameAsc();

    List<Profile> findAllByOrderByNameAsc();

    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.permissions WHERE p.id = :id")
    Optional<Profile> findByIdWithPermissions(@Param("id") UUID id);

    boolean existsByTenantIdAndName(UUID tenantId, String name);

    boolean existsByTenantIdAndNameAndIdNot(UUID tenantId, String name, UUID excludeId);

    boolean existsByTenantIdIsNullAndName(String name);
}
