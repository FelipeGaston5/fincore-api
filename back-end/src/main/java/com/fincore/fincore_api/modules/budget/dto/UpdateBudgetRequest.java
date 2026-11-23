package com.fincore.fincore_api.modules.budget.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.fincore.fincore_api.modules.budget.enums.BudgetPeriod;

import jakarta.validation.constraints.*;

public record UpdateBudgetRequest(
    @NotNull UUID categoryId,
    @NotBlank @Size(max = 120) String name,
    @NotNull @DecimalMin("0.01")
    @Digits(integer = 13, fraction = 2) BigDecimal amountLimit,
    @NotNull BudgetPeriod periodType,
    @NotNull LocalDate startDate,
    LocalDate endDate,
    @NotNull @DecimalMin("0.01") @DecimalMax("100.00")
    @Digits(integer = 3, fraction = 2) BigDecimal alertPercentage,
    @NotNull Boolean active
) {}
