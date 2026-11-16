
package com.fincore.fincore_api.modules.category.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fincore.fincore_api.modules.category.entity.Category;
import com.fincore.fincore_api.modules.category.enums.CategoryType;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(
        UUID userId
    );

    Optional<Category> findByIdAndUserIdAndActiveTrue(
        UUID id,
        UUID userId
    );

    boolean existsByNameIgnoreCaseAndUserIdAndCategoryTypeAndActiveTrue(
        String name,
        UUID userId,
        CategoryType categoryType
    );

    boolean existsByNameIgnoreCaseAndUserIdAndCategoryTypeAndActiveTrueAndIdNot(
        String name,
        UUID userId,
        CategoryType categoryType,
        UUID id
    );

    boolean existsByParentCategoryIdAndUserIdAndActiveTrue(
        UUID parentCategoryId,
        UUID userId
    );
}
