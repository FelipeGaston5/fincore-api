package com.fincore.fincore_api.modules.account.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fincore.fincore_api.modules.account.dto.AccountResponse;
import com.fincore.fincore_api.modules.account.dto.CreateAccountRequest;
import com.fincore.fincore_api.modules.account.dto.UpdateAccountRequest;
import com.fincore.fincore_api.modules.account.entity.Account;
import com.fincore.fincore_api.modules.account.repository.AccountRepository;
import com.fincore.fincore_api.modules.user.entity.User;
import com.fincore.fincore_api.modules.user.repository.UserRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(
        AccountRepository accountRepository,
        UserRepository userRepository
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest request, String email) {
        User user = findUser(email);

        String name = request.name().trim();

        if (accountRepository.existsByNameIgnoreCaseAndUserIdAndActiveTrue(
                name, user.getId())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Já existe uma conta ativa com esse nome."
            );
        }

        Account account = new Account();
        account.setUserId(user.getId());
        account.setName(name);
        account.setAccountType(request.accountType());
        account.setInstitution(request.institution());
        account.setInitialBalance(
            request.initialBalance() != null
                ? request.initialBalance()
                : java.math.BigDecimal.ZERO
        );
        account.setCurrency(
            request.currency() != null
                ? request.currency().toUpperCase(Locale.ROOT)
                : "BRL"
        );
        account.setColor(request.color());
        account.setIcon(request.icon());

        return toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAll(String email) {
        UUID userId = findUser(email).getId();

        return accountRepository
            .findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse findById(UUID accountId, String email) {
        UUID userId = findUser(email).getId();

        return toResponse(findAccount(accountId, userId));
    }

    @Transactional
    public AccountResponse update(
        UUID accountId,
        UpdateAccountRequest request,
        String email
    ) {
        UUID userId = findUser(email).getId();
        Account account = findAccount(accountId, userId);

        String newName = request.name().trim();

        if (!account.getName().equalsIgnoreCase(newName)
            && accountRepository.existsByNameIgnoreCaseAndUserIdAndActiveTrue(
                newName, userId)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Já existe uma conta ativa com esse nome."
            );
        }

        account.setName(newName);
        account.setAccountType(request.accountType());
        account.setInstitution(request.institution());

        if (request.currency() != null) {
            account.setCurrency(
                request.currency().toUpperCase(Locale.ROOT)
            );
        }

        account.setColor(request.color());
        account.setIcon(request.icon());

        return toResponse(accountRepository.save(account));
    }

    @Transactional
    public void deactivate(UUID accountId, String email) {
        UUID userId = findUser(email).getId();
        Account account = findAccount(accountId, userId);

        account.setActive(false);
        accountRepository.save(account);
    }

    private User findUser(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        return userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuário autenticado não encontrado."
            ));
    }

    private Account findAccount(UUID accountId, UUID userId) {
        return accountRepository
            .findByIdAndUserIdAndActiveTrue(accountId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Conta não encontrada."
            ));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getName(),
            account.getAccountType(),
            account.getInstitution(),
            account.getInitialBalance(),
            account.getCurrentBalance(),
            account.getCurrency(),
            account.getColor(),
            account.getIcon(),
            account.isActive(),
            account.getCreatedAt(),
            account.getUpdatedAt()
        );
    }
}