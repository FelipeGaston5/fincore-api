package com.fincore.fincore_api.modules.transaction.controller;

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

import com.fincore.fincore_api.modules.transaction.dto.CreateTransactionRequest;
import com.fincore.fincore_api.modules.transaction.dto.TransactionResponse;
import com.fincore.fincore_api.modules.transaction.dto.UpdateTransactionRequest;
import com.fincore.fincore_api.modules.transaction.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/transactions")
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
        TransactionService transactionService
    ) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
        @Valid @RequestBody CreateTransactionRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        TransactionResponse response = transactionService.create(
            request,
            jwt.getSubject()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> findAll(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            transactionService.findAll(jwt.getSubject())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            transactionService.findById(id, jwt.getSubject())
        );
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> findByAccount(
        @PathVariable UUID accountId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            transactionService.findByAccount(
                accountId,
                jwt.getSubject()
            )
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<TransactionResponse>> findByCategory(
        @PathVariable UUID categoryId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            transactionService.findByCategory(
                categoryId,
                jwt.getSubject()
            )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateTransactionRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            transactionService.update(
                id,
                request,
                jwt.getSubject()
            )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        transactionService.delete(id, jwt.getSubject());

        return ResponseEntity.noContent().build();
    }
}