package com.fincore.fincore_api.modules.recurring_transaction.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fincore.fincore_api.modules.recurring_transaction.entity.RecurringTransaction;
import com.fincore.fincore_api.modules.recurring_transaction.enums.RecurringTransactionStatus;

import jakarta.persistence.LockModeType;

public interface RecurringTransactionRepository
    extends JpaRepository<RecurringTransaction, UUID> {

    List<RecurringTransaction> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<RecurringTransaction> findByIdAndUserId(UUID id, UUID userId);

    List<RecurringTransaction>
        findTop100ByStatusAndNextExecutionDateLessThanEqualOrderByNextExecutionDateAsc(
            RecurringTransactionStatus status,
            LocalDate date
        );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM RecurringTransaction r
        WHERE r.id = :id
    """)
    Optional<RecurringTransaction> findByIdForUpdate(@Param("id") UUID id);
}
