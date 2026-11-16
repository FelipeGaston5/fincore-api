package com.fincore.fincore_api.modules.account.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fincore.fincore_api.modules.account.entity.Account;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(
        UUID userId
    );

    Optional<Account> findByIdAndUserIdAndActiveTrue(
        UUID id,
        UUID userId
    );

    boolean existsByNameIgnoreCaseAndUserIdAndActiveTrue(
        String name,
        UUID userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT a
        FROM Account a
        WHERE a.id = :accountId
          AND a.userId = :userId
    """)
    Optional<Account> findByIdAndUserIdForUpdate(
        @Param("accountId") UUID accountId,
        @Param("userId") UUID userId
    );
}