package com.fincore.fincore_api.modules.budget.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fincore.fincore_api.modules.budget.dto.*;
import com.fincore.fincore_api.modules.budget.service.BudgetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/budgets")
@Validated
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> create(
        @Valid @RequestBody CreateBudgetRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(budgetService.create(request, jwt.getSubject()));
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> findAll(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(budgetService.findAll(jwt.getSubject()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> findById(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            budgetService.findById(id, jwt.getSubject())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateBudgetRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            budgetService.update(id, request, jwt.getSubject())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        budgetService.delete(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
