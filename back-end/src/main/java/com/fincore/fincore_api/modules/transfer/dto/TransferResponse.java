package com.fincore.fincore_api.modules.transfer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.fincore.fincore_api.modules.transfer.enums.TransferStatus;

public record TransferResponse(

    UUID id,

    UUID sourceAccountId,

    UUID destinationAccountId,

    BigDecimal amount,

    String description,

    LocalDate transferDate,

    TransferStatus status,

    String notes,

    OffsetDateTime createdAt,

    OffsetDateTime updatedAt

) {
}
