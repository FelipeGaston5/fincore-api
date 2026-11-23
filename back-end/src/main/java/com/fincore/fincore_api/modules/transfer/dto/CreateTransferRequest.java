package com.fincore.fincore_api.modules.transfer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.fincore.fincore_api.modules.transfer.enums.TransferStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTransferRequest(

    @NotNull
    UUID sourceAccountId,

    @NotNull
    UUID destinationAccountId,

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 13, fraction = 2)
    BigDecimal amount,

    @Size(max = 255)
    String description,

    @NotNull
    LocalDate transferDate,

    TransferStatus status,

    @Size(max = 10000)
    String notes

) {
}
