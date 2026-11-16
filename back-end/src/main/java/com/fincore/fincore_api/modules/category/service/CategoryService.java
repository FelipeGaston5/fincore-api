
package com.fincore.fincore_api.modules.category.service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fincore.fincore_api.modules.category.dto.CategoryResponse;
import com.fincore.fincore_api.modules.category.dto.CreateCategoryRequest;
import com.fincore.fincore_api.modules.category.dto.UpdateCategoryRequest;
import com.fincore.fincore_api.modules.category.entity.Category;
import com.fincore.fincore_api.modules.category.enums.CategoryType;
import com.fincore.fincore_api.modules.category.repository.CategoryRepository;
import com.fincore.fincore_api.modules.user.entity.User;
import com.fincore.fincore_api.modules.user.repository.UserRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(
        CategoryRepository categoryRepository,
        UserRepository userRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // CREATE
    @Transactional
    public CategoryResponse create(
        CreateCategoryRequest request,
        String email
    ) {
        User user = findUser(email);

        String normalizedName = normalizeName(request.name());

        validateDuplicateName(
            normalizedName,
            user.getId(),
            request.categoryType(),
            null
        );

        validateParentCategory(
            request.parentCategoryId(),
            user.getId(),
            request.categoryType(),
            null
        );

        Category category = new Category();

        category.setUserId(user.getId());
        category.setName(normalizedName);
        category.setDescription(request.description());
        category.setCategoryType(request.categoryType());
        category.setColor(request.color());
        category.setIcon(request.icon());
        category.setParentCategoryId(request.parentCategoryId());

        Category savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    // FIND ALL
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll(String email) {

        User user = findUser(email);

        return categoryRepository
            .findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(user.getId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    // FIND BY ID
    @Transactional(readOnly = true)
    public CategoryResponse findById(
        UUID id,
        String email
    ) {
        User user = findUser(email);

        Category category = findCategory(id, user.getId());

        return toResponse(category);
    }

    // UPDATE
    @Transactional
    public CategoryResponse update(
        UUID id,
        UpdateCategoryRequest request,
        String email
    ) {
        User user = findUser(email);

        Category category = findCategory(id, user.getId());

        validateSystemCategory(category);

        // Impede alterar o tipo se a categoria possuir subcategorias ativas.
        if (
            category.getCategoryType() != request.categoryType()
            && categoryRepository.existsByParentCategoryIdAndUserIdAndActiveTrue(
                id,
                user.getId()
            )
        ) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Não é possível alterar o tipo de uma categoria que possui subcategorias ativas."
            );
        }

        String normalizedName = normalizeName(request.name());

        validateDuplicateName(
            normalizedName,
            user.getId(),
            request.categoryType(),
            id
        );

        validateParentCategory(
            request.parentCategoryId(),
            user.getId(),
            request.categoryType(),
            id
        );

        category.setName(normalizedName);
        category.setDescription(request.description());
        category.setCategoryType(request.categoryType());
        category.setColor(request.color());
        category.setIcon(request.icon());
        category.setParentCategoryId(request.parentCategoryId());

        Category updatedCategory = categoryRepository.save(category);

        return toResponse(updatedCategory);
    }

    // DEACTIVATE
    @Transactional
    public void deactivate(
        UUID id,
        String email
    ) {
        User user = findUser(email);

        Category category = findCategory(id, user.getId());

        validateSystemCategory(category);

        boolean hasActiveChildren =
            categoryRepository.existsByParentCategoryIdAndUserIdAndActiveTrue(
                id,
                user.getId()
            );

        if (hasActiveChildren) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Não é possível desativar uma categoria que possui subcategorias ativas."
            );
        }

        category.setActive(false);

        categoryRepository.save(category);
    }

    // FIND AUTHENTICATED USER
    private User findUser(String email) {

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        return userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuário autenticado não encontrado."
            ));
    }

    // FIND CATEGORY BY ID AND USER
    private Category findCategory(
        UUID id,
        UUID userId
    ) {
        return categoryRepository
            .findByIdAndUserIdAndActiveTrue(id, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Categoria não encontrada."
            ));
    }

    // VALIDATE DUPLICATE NAME
    private void validateDuplicateName(
        String name,
        UUID userId,
        CategoryType categoryType,
        UUID categoryId
    ) {

        boolean duplicate;

        if (categoryId == null) {
            duplicate =
                categoryRepository
                    .existsByNameIgnoreCaseAndUserIdAndCategoryTypeAndActiveTrue(
                        name,
                        userId,
                        categoryType
                    );
        } else {
            duplicate =
                categoryRepository
                    .existsByNameIgnoreCaseAndUserIdAndCategoryTypeAndActiveTrueAndIdNot(
                        name,
                        userId,
                        categoryType,
                        categoryId
                    );
        }

        if (duplicate) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Já existe uma categoria com esse nome e tipo."
            );
        }
    }

    // VALIDATE PARENT CATEGORY AND HIERARCHY
    private void validateParentCategory(
        UUID parentCategoryId,
        UUID userId,
        CategoryType categoryType,
        UUID currentCategoryId
    ) {

        if (parentCategoryId == null) {
            return;
        }

        Category parentCategory = findCategory(
            parentCategoryId,
            userId
        );

        if (parentCategory.getCategoryType() != categoryType) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A categoria-pai deve possuir o mesmo tipo da categoria."
            );
        }

        Set<UUID> visitedCategories = new HashSet<>();

        Category currentCategory = parentCategory;

        while (currentCategory != null) {

            if (
                currentCategoryId != null
                && currentCategory.getId().equals(currentCategoryId)
            ) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é permitido criar uma hierarquia circular de categorias."
                );
            }

            if (!visitedCategories.add(currentCategory.getId())) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Foi identificada uma hierarquia circular existente."
                );
            }

            UUID nextParentId = currentCategory.getParentCategoryId();

            currentCategory = nextParentId == null
                ? null
                : findCategory(nextParentId, userId);
        }
    }

    // PROTECT SYSTEM CATEGORIES
    private void validateSystemCategory(Category category) {

        if (category.isSystem()) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Categorias do sistema não podem ser alteradas ou desativadas."
            );
        }
    }

    // NORMALIZE CATEGORY NAME
    private String normalizeName(String name) {
        return name.trim();
    }

    // CONVERT ENTITY TO RESPONSE DTO
    private CategoryResponse toResponse(Category category) {

        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getCategoryType(),
            category.getColor(),
            category.getIcon(),
            category.getParentCategoryId(),
            category.isSystem(),
            category.isActive(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}
