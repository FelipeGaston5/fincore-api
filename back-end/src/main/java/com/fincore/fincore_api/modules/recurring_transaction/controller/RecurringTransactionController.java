package com.fincore.fincore_api.modules.recurring_transaction.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fincore.fincore_api.modules.recurring_transaction.dto.CreateRecurringTransactionRequest;
import com.fincore.fincore_api.modules.recurring_transaction.dto.RecurringTransactionResponse;
import com.fincore.fincore_api.modules.recurring_transaction.dto.UpdateRecurringTransactionRequest;
import com.fincore.fincore_api.modules.recurring_transaction.service.RecurringTransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/recurring-transactions")
@Validated
public class RecurringTransactionController {

    private final RecurringTransactionService service;

    public RecurringTransactionController(RecurringTransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RecurringTransactionResponse> create(
        @Valid @RequestBody CreateRecurringTransactionRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.create(request, jwt.getSubject()));
    }

    @GetMapping
    public ResponseEntity<List<RecurringTransactionResponse>> findAll(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(service.findAll(jwt.getSubject()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> findById(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(service.findById(id, jwt.getSubject()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateRecurringTransactionRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(service.update(id, request, jwt.getSubject()));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<RecurringTransactionResponse> pause(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(service.pause(id, jwt.getSubject()));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<RecurringTransactionResponse> resume(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(service.resume(id, jwt.getSubject()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<RecurringTransactionResponse> cancel(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(service.cancel(id, jwt.getSubject()));
    }
}
