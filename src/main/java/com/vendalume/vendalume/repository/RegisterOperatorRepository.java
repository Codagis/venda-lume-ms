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

@Repository
public interface RegisterOperatorRepository extends JpaRepository<RegisterOperator, RegisterOperatorId> {

    List<RegisterOperator> findByRegisterIdOrderByCreatedAtAsc(UUID registerId);

    @Modifying
    @Query("DELETE FROM RegisterOperator ro WHERE ro.registerId = :registerId")
    void deleteByRegisterId(@Param("registerId") UUID registerId);

    boolean existsByRegisterIdAndUserId(UUID registerId, UUID userId);
}
