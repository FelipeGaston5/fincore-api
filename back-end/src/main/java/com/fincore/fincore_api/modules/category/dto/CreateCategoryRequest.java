
package com.fincore.fincore_api.modules.category.dto;

import java.util.UUID;

import com.fincore.fincore_api.modules.category.enums.CategoryType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(

    @NotBlank(message = "O nome da categoria é obrigatório.")
    @Size(max = 100)
    String name,

    @Size(max = 255)
    String description,

    @NotNull(message = "O tipo da categoria é obrigatório.")
    CategoryType categoryType,

    @Pattern(
        regexp = "^#[0-9A-Fa-f]{6}$",
        message = "Informe uma cor hexadecimal válida."
    )
    String color,

    @Size(max = 50)
    String icon,

    UUID parentCategoryId

) {}
