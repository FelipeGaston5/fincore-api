package com.fincore.fincore_api.modules.account.dto;

import java.math.BigDecimal;

import com.fincore.fincore_api.modules.account.enums.AccountType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(

    @NotBlank(message = "O nome da conta é obrigatório.")
    @Size(max = 100)
    String name,

    @NotNull(message = "O tipo da conta é obrigatório.")
    AccountType accountType,

    @Size(max = 120)
    String institution,

    @DecimalMin(value = "0.00", message = "O saldo inicial não pode ser negativo.")
    BigDecimal initialBalance,

    @Pattern(regexp = "^[A-Za-z]{3}$", message = "A moeda deve conter três letras.")
    String currency,

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Informe uma cor hexadecimal válida.")
    String color,

    @Size(max = 50)
    String icon

) {}
