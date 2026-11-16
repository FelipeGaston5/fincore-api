package com.fincore.fincore_api.modules.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.fincore.fincore_api.modules.transaction.enums.TransactionStatus;
import com.fincore.fincore_api.modules.transaction.enums.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTransactionRequest(

    @NotNull
    UUID accountId,

    UUID categoryId,

    @NotNull
    TransactionType transactionType,

    @NotBlank
    @Size(max = 255)
    String description,

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 13, fraction = 2)
    BigDecimal amount,

    @NotNull
    LocalDate transactionDate,

    LocalDate dueDate,

    @NotNull
    TransactionStatus status,

    @Size(max = 10000)
    String notes

) {
}