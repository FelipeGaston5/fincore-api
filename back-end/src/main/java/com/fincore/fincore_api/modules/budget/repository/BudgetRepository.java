package com.fincore.fincore_api.modules.budget.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fincore.fincore_api.modules.budget.entity.Budget;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Budget> findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(UUID userId);

    Optional<Budget> findByIdAndUserId(UUID id, UUID userId);
}
