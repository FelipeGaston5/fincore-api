package com.fincore.fincore_api.modules.account.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fincore.fincore_api.modules.account.entity.Account;


public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(UUID userId);

    Optional<Account> findByIdAndUserIdAndActiveTrue(UUID id, UUID userId);

    boolean existsByNameIgnoreCaseAndUserIdAndActiveTrue(
        String name,
        UUID userId
    );
}
