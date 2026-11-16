
package com.fincore.fincore_api.modules.category.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fincore.fincore_api.modules.category.dto.CategoryResponse;
import com.fincore.fincore_api.modules.category.dto.CreateCategoryRequest;
import com.fincore.fincore_api.modules.category.dto.UpdateCategoryRequest;
import com.fincore.fincore_api.modules.category.service.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
        @Valid @RequestBody CreateCategoryRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        CategoryResponse response =
            categoryService.create(request, jwt.getSubject());

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            categoryService.findAll(jwt.getSubject())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            categoryService.findById(id, jwt.getSubject())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateCategoryRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            categoryService.update(id, request, jwt.getSubject())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        categoryService.deactivate(id, jwt.getSubject());

        return ResponseEntity.noContent().build();
    }
}
