package com.fincore.fincore_api.modules.transfer.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fincore.fincore_api.modules.transfer.dto.CreateTransferRequest;
import com.fincore.fincore_api.modules.transfer.dto.TransferResponse;
import com.fincore.fincore_api.modules.transfer.dto.UpdateTransferRequest;
import com.fincore.fincore_api.modules.transfer.service.TransferService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/transfers")
@Validated
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> create(
        @Valid @RequestBody CreateTransferRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        TransferResponse response = transferService.create(
            request,
            jwt.getSubject()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TransferResponse>> findAll(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            transferService.findAll(jwt.getSubject())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponse> findById(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            transferService.findById(id, jwt.getSubject())
        );
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransferResponse>> findByAccount(
        @PathVariable UUID accountId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            transferService.findByAccount(accountId, jwt.getSubject())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransferResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateTransferRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            transferService.update(id, request, jwt.getSubject())
        );
    }
}
