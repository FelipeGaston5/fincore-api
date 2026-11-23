package com.fincore.fincore_api.modules.transfer.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fincore.fincore_api.modules.account.entity.Account;
import com.fincore.fincore_api.modules.account.repository.AccountRepository;
import com.fincore.fincore_api.modules.transfer.dto.CreateTransferRequest;
import com.fincore.fincore_api.modules.transfer.dto.TransferResponse;
import com.fincore.fincore_api.modules.transfer.dto.UpdateTransferRequest;
import com.fincore.fincore_api.modules.transfer.entity.Transfer;
import com.fincore.fincore_api.modules.transfer.enums.TransferStatus;
import com.fincore.fincore_api.modules.transfer.repository.TransferRepository;
import com.fincore.fincore_api.modules.user.entity.User;
import com.fincore.fincore_api.modules.user.repository.UserRepository;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransferService(
        TransferRepository transferRepository,
        AccountRepository accountRepository,
        UserRepository userRepository
    ) {
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransferResponse create(
        CreateTransferRequest request,
        String email
    ) {
        User user = findUser(email);

        Map<UUID, Account> accounts = lockAccounts(
            request.sourceAccountId(),
            request.destinationAccountId(),
            user.getId()
        );

        Account source = accounts.get(request.sourceAccountId());
        Account destination = accounts.get(request.destinationAccountId());

        validateAccounts(source, destination);

        Transfer transfer = new Transfer();

        transfer.setUserId(user.getId());
        transfer.setSourceAccountId(source.getId());
        transfer.setDestinationAccountId(destination.getId());
        transfer.setAmount(request.amount());
        transfer.setDescription(normalizeDescription(request.description()));
        transfer.setTransferDate(request.transferDate());
        transfer.setNotes(request.notes());

        TransferStatus status = request.status() != null
            ? request.status()
            : TransferStatus.COMPLETED;

        transfer.setStatus(status);

        if (status == TransferStatus.COMPLETED) {
            applyBalance(source, destination, request.amount(), false);
        }

        return toResponse(transferRepository.save(transfer));
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> findAll(String email) {
        UUID userId = findUser(email).getId();

        return transferRepository
            .findAllByUserIdOrderByTransferDateDescCreatedAtDesc(userId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TransferResponse findById(UUID transferId, String email) {
        UUID userId = findUser(email).getId();

        return toResponse(findTransfer(transferId, userId));
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> findByAccount(
        UUID accountId,
        String email
    ) {
        UUID userId = findUser(email).getId();

        if (accountRepository.findByIdAndUserIdAndActiveTrue(
            accountId,
            userId
        ).isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Conta não encontrada."
            );
        }

        return transferRepository
            .findAllByAccount(userId, accountId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public TransferResponse update(
        UUID transferId,
        UpdateTransferRequest request,
        String email
    ) {
        UUID userId = findUser(email).getId();

        Transfer transfer = findTransfer(transferId, userId);

        if (transfer.getStatus() == TransferStatus.CANCELLED) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Uma transferência cancelada não pode ser alterada."
            );
        }

        Map<UUID, Account> accounts = lockAccounts(
            transfer.getSourceAccountId(),
            transfer.getDestinationAccountId(),
            request.sourceAccountId(),
            request.destinationAccountId(),
            userId
        );

        Account oldSource = accounts.get(transfer.getSourceAccountId());
        Account oldDestination = accounts.get(transfer.getDestinationAccountId());

        Account newSource = accounts.get(request.sourceAccountId());
        Account newDestination = accounts.get(request.destinationAccountId());

        validateAccounts(newSource, newDestination);

        // Desfaz o efeito financeiro anterior, se já estava concluída.
        if (transfer.getStatus() == TransferStatus.COMPLETED) {
            applyBalance(
                oldSource,
                oldDestination,
                transfer.getAmount(),
                true
            );
        }

        transfer.setSourceAccountId(newSource.getId());
        transfer.setDestinationAccountId(newDestination.getId());
        transfer.setAmount(request.amount());
        transfer.setDescription(normalizeDescription(request.description()));
        transfer.setTransferDate(request.transferDate());
        transfer.setStatus(request.status());
        transfer.setNotes(request.notes());

        // Aplica o novo efeito financeiro somente se estiver concluída.
        if (request.status() == TransferStatus.COMPLETED) {
            applyBalance(
                newSource,
                newDestination,
                request.amount(),
                false
            );
        }

        return toResponse(transferRepository.save(transfer));
    }

    private User findUser(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        return userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuário autenticado não encontrado."
            ));
    }

    private Transfer findTransfer(UUID transferId, UUID userId) {
        return transferRepository.findByIdAndUserId(transferId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Transferência não encontrada."
            ));
    }

    private Map<UUID, Account> lockAccounts(
        UUID first,
        UUID second,
        UUID userId
    ) {
        return lockAccounts(first, second, null, null, userId);
    }

    private Map<UUID, Account> lockAccounts(
        UUID first,
        UUID second,
        UUID third,
        UUID fourth,
        UUID userId
    ) {
        Map<UUID, Account> accounts = new HashMap<>();

        List<UUID> ids = java.util.stream.Stream.of(
            first, second, third, fourth
        )
            .filter(java.util.Objects::nonNull)
            .distinct()
            .sorted()
            .toList();

        for (UUID id : ids) {
            Account account = accountRepository
                .findByIdAndUserIdForUpdate(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Uma das contas informadas não foi encontrada."
                ));

            accounts.put(id, account);
        }

        return accounts;
    }

    private void validateAccounts(Account source, Account destination) {
        if (source.getId().equals(destination.getId())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A conta de origem e destino devem ser diferentes."
            );
        }

        if (!source.isActive() || !destination.isActive()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Não é possível transferir usando uma conta inativa."
            );
        }

        if (!source.getCurrency().equalsIgnoreCase(destination.getCurrency())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "As contas precisam utilizar a mesma moeda."
            );
        }
    }

    private void applyBalance(
        Account source,
        Account destination,
        BigDecimal amount,
        boolean reverse
    ) {
        BigDecimal sourceDelta = amount.negate();
        BigDecimal destinationDelta = amount;

        if (reverse) {
            sourceDelta = sourceDelta.negate();
            destinationDelta = destinationDelta.negate();
        }

        source.setCurrentBalance(
            source.getCurrentBalance().add(sourceDelta)
        );

        destination.setCurrentBalance(
            destination.getCurrentBalance().add(destinationDelta)
        );
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }

    private TransferResponse toResponse(Transfer transfer) {
        return new TransferResponse(
            transfer.getId(),
            transfer.getSourceAccountId(),
            transfer.getDestinationAccountId(),
            transfer.getAmount(),
            transfer.getDescription(),
            transfer.getTransferDate(),
            transfer.getStatus(),
            transfer.getNotes(),
            transfer.getCreatedAt(),
            transfer.getUpdatedAt()
        );
    }
}
