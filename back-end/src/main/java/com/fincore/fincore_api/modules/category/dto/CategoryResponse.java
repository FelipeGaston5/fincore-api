
package com.fincore.fincore_api.modules.category.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fincore.fincore_api.modules.category.enums.CategoryType;

public record CategoryResponse(

    UUID id,
    String name,
    String description,
    CategoryType categoryType,
    String color,
    String icon,
    UUID parentCategoryId,
    boolean system,
    boolean active,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt

) {}
