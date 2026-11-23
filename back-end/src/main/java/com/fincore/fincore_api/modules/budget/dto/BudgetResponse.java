package com.fincore.fincore_api.modules.budget.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.fincore.fincore_api.modules.budget.enums.BudgetPeriod;

public record BudgetResponse(
    UUID id,
    UUID categoryId,
    String name,
    BigDecimal amountLimit,
    BudgetPeriod periodType,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal alertPercentage,
    boolean active,
    BigDecimal spentAmount,
    BigDecimal remainingAmount,
    BigDecimal usedPercentage,
    boolean alertTriggered,
    boolean exceeded,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
