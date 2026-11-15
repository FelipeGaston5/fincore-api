package com.fincore.fincore_api.modules.account.dto;


import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.fincore.fincore_api.modules.account.enums.AccountType;

public record AccountResponse(
    UUID id,
    String name,
    AccountType accountType,
    String institution,
    BigDecimal initialBalance,
    BigDecimal currentBalance,
    String currency,
    String color,
    String icon,
    boolean active,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
