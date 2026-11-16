package com.fincore.fincore_api.modules.transaction.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fincore.fincore_api.modules.transaction.entity.Transaction;

public interface TransactionRepository
    extends JpaRepository<Transaction, UUID> {

    List<Transaction>
        findAllByUserIdAndDeletedAtIsNullOrderByTransactionDateDescCreatedAtDesc(
            UUID userId
        );

    Optional<Transaction> findByIdAndUserIdAndDeletedAtIsNull(
        UUID id,
        UUID userId
    );

    List<Transaction>
        findAllByUserIdAndAccountIdAndDeletedAtIsNullOrderByTransactionDateDesc(
            UUID userId,
            UUID accountId
        );

    List<Transaction>
        findAllByUserIdAndCategoryIdAndDeletedAtIsNullOrderByTransactionDateDesc(
            UUID userId,
            UUID categoryId
        );
}