package com.fincore.fincore_api.modules.recurring_transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.fincore.fincore_api.modules.recurring_transaction.enums.RecurrenceFrequency;
import com.fincore.fincore_api.modules.recurring_transaction.enums.RecurringTransactionStatus;
import com.fincore.fincore_api.modules.transaction.enums.TransactionType;

public record RecurringTransactionResponse(
    UUID id,
    UUID accountId,
    UUID categoryId,
    TransactionType transactionType,
    String description,
    BigDecimal amount,
    RecurrenceFrequency frequency,
    Integer intervalValue,
    LocalDate startDate,
    LocalDate endDate,
    LocalDate nextExecutionDate,
    LocalDate lastExecutionDate,
    Integer dayOfMonth,
    RecurringTransactionStatus status,
    String notes,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
