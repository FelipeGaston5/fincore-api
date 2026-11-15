package com.fincore.fincore_api.modules.account.controller;

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

import com.fincore.fincore_api.modules.account.dto.AccountResponse;
import com.fincore.fincore_api.modules.account.dto.CreateAccountRequest;
import com.fincore.fincore_api.modules.account.dto.UpdateAccountRequest;
import com.fincore.fincore_api.modules.account.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(
        @Valid @RequestBody CreateAccountRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        AccountResponse response =
            accountService.create(request, jwt.getSubject());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> findAll(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            accountService.findAll(jwt.getSubject())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findById(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            accountService.findById(id, jwt.getSubject())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateAccountRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            accountService.update(id, request, jwt.getSubject())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        accountService.deactivate(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}