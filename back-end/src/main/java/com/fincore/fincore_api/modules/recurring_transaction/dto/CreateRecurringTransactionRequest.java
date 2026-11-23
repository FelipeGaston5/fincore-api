package com.fincore.fincore_api.modules.recurring_transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.fincore.fincore_api.modules.recurring_transaction.enums.RecurrenceFrequency;
import com.fincore.fincore_api.modules.transaction.enums.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateRecurringTransactionRequest(
    @NotNull UUID accountId,
    UUID categoryId,
    @NotNull TransactionType transactionType,
    @NotBlank @Size(max = 255) String description,
    @NotNull @DecimalMin("0.01") @Digits(integer = 13, fraction = 2) BigDecimal amount,
    @NotNull RecurrenceFrequency frequency,
    @NotNull @Positive Integer intervalValue,
    @NotNull LocalDate startDate,
    LocalDate endDate,
    @Min(1) @Max(31) Integer dayOfMonth,
    @Size(max = 10000) String notes
) {}
