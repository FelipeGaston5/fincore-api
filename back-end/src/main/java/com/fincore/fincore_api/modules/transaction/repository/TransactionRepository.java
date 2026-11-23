package com.fincore.fincore_api.modules.transaction.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fincore.fincore_api.modules.transaction.entity.Transaction;
import com.fincore.fincore_api.modules.transaction.enums.TransactionStatus;
import com.fincore.fincore_api.modules.transaction.enums.TransactionType;
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

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.userId = :userId
        AND t.categoryId = :categoryId
        AND t.transactionType = :type
        AND t.status = :status
        AND t.deletedAt IS NULL
        AND t.transactionDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal sumCompletedExpenses(
        @Param("userId") UUID userId,
        @Param("categoryId") UUID categoryId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("type") TransactionType type,
        @Param("status") TransactionStatus status
    );
}