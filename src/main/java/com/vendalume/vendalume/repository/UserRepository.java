package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vendalume.vendalume.domain.enums.UserRole;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link User}.
 * Oferece métodos de busca por username, tenant, validação de existência e controle de tentativas de login.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query(value = "SELECT * FROM users WHERE LOWER(username) = LOWER(CAST(:username AS TEXT)) LIMIT 1", nativeQuery = true)
    Optional<User> findByUsernameIgnoreCase(@Param("username") String username);

    @Query(value = "SELECT * FROM users WHERE tenant_id = CAST(:tenantId AS UUID) AND LOWER(username) = LOWER(CAST(:username AS TEXT)) LIMIT 1", nativeQuery = true)
    Optional<User> findByTenantIdAndUsernameIgnoreCase(@Param("tenantId") UUID tenantId, @Param("username") String username);

    @Query(value = """
            SELECT * FROM users
            WHERE LOWER(username) = LOWER(CAST(:username AS TEXT))
            AND (tenant_id = CAST(:tenantId AS UUID) OR (CAST(:tenantId AS UUID) IS NULL AND tenant_id IS NULL))
            AND active = true
            LIMIT 1
            """, nativeQuery = true)
    Optional<User> findByUsernameAndTenant(@Param("username") String username, @Param("tenantId") UUID tenantId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(username) = LOWER(CAST(:username AS TEXT)))", nativeQuery = true)
    boolean existsByUsernameIgnoreCase(@Param("username") String username);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(email) = LOWER(CAST(:email AS TEXT)))", nativeQuery = true)
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(email) = LOWER(CAST(:email AS TEXT)) AND id != CAST(:id AS UUID))", nativeQuery = true)
    boolean existsByEmailIgnoreCaseAndIdNot(@Param("email") String email, @Param("id") UUID id);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM users WHERE tenant_id = CAST(:tenantId AS UUID) AND LOWER(username) = LOWER(CAST(:username AS TEXT)))", nativeQuery = true)
    boolean existsByTenantIdAndUsernameIgnoreCase(@Param("tenantId") UUID tenantId, @Param("username") String username);

    List<User> findByTenantIdOrderByUsernameAsc(UUID tenantId);

    List<User> findByTenantIdAndRoleAndActiveTrueOrderByFullNameAsc(UUID tenantId, UserRole role);

    List<User> findByTenantIdAndRoleInAndActiveTrueOrderByFullNameAsc(UUID tenantId, Set<UserRole> roles);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginAt, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void recordSuccessfulLogin(@Param("userId") UUID userId, @Param("loginAt") Instant loginAt);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    void incrementFailedLoginAttempts(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    void lockAccount(@Param("userId") UUID userId, @Param("lockedUntil") Instant lockedUntil);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = null WHERE u.id = :userId")
    void resetLoginAttempts(@Param("userId") UUID userId);
}
