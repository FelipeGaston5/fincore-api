
package com.fincore.fincore_api.modules.transfer.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fincore.fincore_api.modules.transfer.entity.Transfer;

import jakarta.persistence.LockModeType;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    List<Transfer> findAllByUserIdOrderByTransferDateDescCreatedAtDesc(
        UUID userId
    );

    Optional<Transfer> findByIdAndUserId(
        UUID id,
        UUID userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t
        FROM Transfer t
        WHERE t.id = :transferId
          AND t.userId = :userId
    """)
    Optional<Transfer> findByIdAndUserIdForUpdate(
        @Param("transferId") UUID transferId,
        @Param("userId") UUID userId
    );

    @Query("""
        SELECT t
        FROM Transfer t
        WHERE t.userId = :userId
          AND (
              t.sourceAccountId = :accountId
              OR t.destinationAccountId = :accountId
          )
        ORDER BY t.transferDate DESC, t.createdAt DESC
    """)
    List<Transfer> findAllByAccount(
        @Param("userId") UUID userId,
        @Param("accountId") UUID accountId
    );
}

