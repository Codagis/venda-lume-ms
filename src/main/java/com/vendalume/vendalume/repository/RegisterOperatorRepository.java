package com.vendalume.vendalume.repository;

import com.vendalume.vendalume.domain.entity.RegisterOperator;
import com.vendalume.vendalume.domain.entity.RegisterOperatorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Interface de repositório para operações de persistência da entidade {@link RegisterOperator}.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Repository
public interface RegisterOperatorRepository extends JpaRepository<RegisterOperator, RegisterOperatorId> {

    @Query(value = "SELECT * FROM register_operators WHERE register_id = CAST(:registerId AS UUID) ORDER BY created_at ASC", nativeQuery = true)
    List<RegisterOperator> findByRegisterIdOrderByCreatedAtAsc(@Param("registerId") UUID registerId);

    @Modifying
    @Query(value = "DELETE FROM register_operators WHERE register_id = CAST(:registerId AS UUID)", nativeQuery = true)
    void deleteByRegisterId(@Param("registerId") UUID registerId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM register_operators WHERE register_id = CAST(:registerId AS UUID) AND user_id = CAST(:userId AS UUID))", nativeQuery = true)
    boolean existsByRegisterIdAndUserId(@Param("registerId") UUID registerId, @Param("userId") UUID userId);
}
