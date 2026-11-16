package com.fincore.fincore_api.modules.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.fincore.fincore_api.modules.transaction.enums.TransactionStatus;
import com.fincore.fincore_api.modules.transaction.enums.TransactionType;

public record TransactionResponse(

    UUID id,

    UUID accountId,

    UUID categoryId,

    TransactionType transactionType,

    String description,

    BigDecimal amount,

    LocalDate transactionDate,

    LocalDate dueDate,

    OffsetDateTime paidAt,

    TransactionStatus status,

    String notes,

    OffsetDateTime createdAt,

    OffsetDateTime updatedAt

) {
}