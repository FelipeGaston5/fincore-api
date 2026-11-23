package com.fincore.fincore_api.modules.transaction.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
import com.fincore.fincore_api.modules.category.entity.Category;
import com.fincore.fincore_api.modules.category.enums.CategoryType;
import com.fincore.fincore_api.modules.category.repository.CategoryRepository;
import com.fincore.fincore_api.modules.transaction.dto.CreateTransactionRequest;
import com.fincore.fincore_api.modules.transaction.dto.TransactionResponse;
import com.fincore.fincore_api.modules.transaction.dto.UpdateTransactionRequest;
import com.fincore.fincore_api.modules.transaction.entity.Transaction;
import com.fincore.fincore_api.modules.transaction.enums.TransactionStatus;
import com.fincore.fincore_api.modules.transaction.enums.TransactionType;
import com.fincore.fincore_api.modules.transaction.repository.TransactionRepository;
import com.fincore.fincore_api.modules.user.entity.User;
import com.fincore.fincore_api.modules.user.repository.UserRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionService(
        TransactionRepository transactionRepository,
        AccountRepository accountRepository,
        CategoryRepository categoryRepository,
        UserRepository userRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransactionResponse create(
        CreateTransactionRequest request,
        String email
    ) {
        User user = findUser(email);

        Account account = lockAccount(request.accountId(), user.getId());

        validateActiveAccount(account);
        validateCategory(
            request.categoryId(),
            user.getId(),
            request.transactionType()
        );

        Transaction transaction = new Transaction();

        transaction.setUserId(user.getId());
        transaction.setAccountId(account.getId());
        transaction.setCategoryId(request.categoryId());
        transaction.setTransactionType(request.transactionType());
        transaction.setDescription(request.description().trim());
        transaction.setAmount(request.amount());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setDueDate(request.dueDate());
        transaction.setNotes(request.notes());

        TransactionStatus status = request.status() != null
            ? request.status()
            : TransactionStatus.COMPLETED;

        transaction.setStatus(status);

        if (status == TransactionStatus.COMPLETED) {
            transaction.setPaidAt(OffsetDateTime.now());
            applyBalance(account, transaction, false);
        }

        return toResponse(transactionRepository.save(transaction));
    }

    /**
     * Cria uma transação PENDING vinculada a uma recorrência.
     * O saldo não é alterado até que a transação seja concluída pelo fluxo normal.
     */
    @Transactional
    public TransactionResponse createFromRecurring(
        UUID userId,
        UUID recurringTransactionId,
        UUID accountId,
        UUID categoryId,
        TransactionType transactionType,
        String description,
        BigDecimal amount,
        LocalDate executionDate,
        String notes
    ) {
        Account account = lockAccount(accountId, userId);
        validateActiveAccount(account);
        validateCategory(categoryId, userId, transactionType);

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAccountId(account.getId());
        transaction.setCategoryId(categoryId);
        transaction.setTransactionType(transactionType);
        transaction.setDescription(description.trim());
        transaction.setAmount(amount);
        transaction.setTransactionDate(executionDate);
        transaction.setDueDate(executionDate);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setNotes(notes);
        transaction.setRecurringTransactionId(recurringTransactionId);

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll(String email) {
        UUID userId = findUser(email).getId();

        return transactionRepository
            .findAllByUserIdAndDeletedAtIsNullOrderByTransactionDateDescCreatedAtDesc(
                userId
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(
        UUID transactionId,
        String email
    ) {
        UUID userId = findUser(email).getId();

        return toResponse(findTransaction(transactionId, userId));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findByAccount(
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

        return transactionRepository
            .findAllByUserIdAndAccountIdAndDeletedAtIsNullOrderByTransactionDateDesc(
                userId,
                accountId
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findByCategory(
        UUID categoryId,
        String email
    ) {
        UUID userId = findUser(email).getId();

        if (categoryRepository.findByIdAndUserIdAndActiveTrue(
            categoryId,
            userId
        ).isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Categoria não encontrada."
            );
        }

        return transactionRepository
            .findAllByUserIdAndCategoryIdAndDeletedAtIsNullOrderByTransactionDateDesc(
                userId,
                categoryId
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public TransactionResponse update(
        UUID transactionId,
        UpdateTransactionRequest request,
        String email
    ) {
        UUID userId = findUser(email).getId();

        Transaction transaction = transactionRepository
	    .findByIdAndUserIdForUpdate(transactionId, userId)
	    .orElseThrow(() -> new ResponseStatusException(
	        HttpStatus.NOT_FOUND,
	        "Transação não encontrada."
	    ));

        UUID oldAccountId = transaction.getAccountId();
        UUID newAccountId = request.accountId();

        Map<UUID, Account> lockedAccounts = lockAccounts(
            oldAccountId,
            newAccountId,
            userId
        );

        Account oldAccount = lockedAccounts.get(oldAccountId);
        Account newAccount = lockedAccounts.get(newAccountId);

        validateActiveAccount(newAccount);

        validateCategory(
            request.categoryId(),
            userId,
            request.transactionType()
        );

        boolean wasCompleted =
            transaction.getStatus() == TransactionStatus.COMPLETED;

        OffsetDateTime previousPaidAt = transaction.getPaidAt();

        if (wasCompleted) {
            applyBalance(oldAccount, transaction, true);
        }

        transaction.setAccountId(newAccountId);
        transaction.setCategoryId(request.categoryId());
        transaction.setTransactionType(request.transactionType());
        transaction.setDescription(request.description().trim());
        transaction.setAmount(request.amount());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setDueDate(request.dueDate());
        transaction.setStatus(request.status());
        transaction.setNotes(request.notes());

        if (request.status() == TransactionStatus.COMPLETED) {
            transaction.setPaidAt(
                wasCompleted && previousPaidAt != null
                    ? previousPaidAt
                    : OffsetDateTime.now()
            );

            applyBalance(newAccount, transaction, false);
        } else {
            transaction.setPaidAt(null);
        }

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public void delete(UUID transactionId, String email) {
        UUID userId = findUser(email).getId();

        Transaction transaction = transactionRepository
	    .findByIdAndUserIdForUpdate(transactionId, userId)
	    .orElseThrow(() -> new ResponseStatusException(
	        HttpStatus.NOT_FOUND,
	        "Transação não encontrada."
	    ));

        Account account = lockAccount(
            transaction.getAccountId(),
            userId
        );

        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            applyBalance(account, transaction, true);
        }

        transaction.setDeletedAt(OffsetDateTime.now());

        transactionRepository.save(transaction);
    }

    private User findUser(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        return userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuário autenticado não encontrado."
            ));
    }

    private Transaction findTransaction(
        UUID transactionId,
        UUID userId
    ) {
        return transactionRepository
            .findByIdAndUserIdAndDeletedAtIsNull(
                transactionId,
                userId
            )
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Transação não encontrada."
            ));
    }

    private Account lockAccount(UUID accountId, UUID userId) {
        return accountRepository
            .findByIdAndUserIdForUpdate(accountId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Conta não encontrada."
            ));
    }

    private Map<UUID, Account> lockAccounts(
        UUID firstAccountId,
        UUID secondAccountId,
        UUID userId
    ) {
        Map<UUID, Account> accounts = new HashMap<>();

        UUID lowerId = firstAccountId.compareTo(secondAccountId) <= 0
            ? firstAccountId
            : secondAccountId;

        UUID higherId = firstAccountId.compareTo(secondAccountId) <= 0
            ? secondAccountId
            : firstAccountId;

        accounts.put(lowerId, lockAccount(lowerId, userId));

        if (!lowerId.equals(higherId)) {
            accounts.put(higherId, lockAccount(higherId, userId));
        }

        return accounts;
    }

    private void validateActiveAccount(Account account) {
        if (!account.isActive()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Não é possível utilizar uma conta inativa."
            );
        }
    }

    private void validateCategory(
        UUID categoryId,
        UUID userId,
        TransactionType transactionType
    ) {
        if (categoryId == null) {
            return;
        }

        Category category = categoryRepository
            .findByIdAndUserIdAndActiveTrue(categoryId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Categoria não encontrada."
            ));

        CategoryType expectedType =
            transactionType == TransactionType.INCOME
                ? CategoryType.INCOME
                : CategoryType.EXPENSE;

        if (category.getCategoryType() != expectedType) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A categoria não corresponde ao tipo da transação."
            );
        }
    }

    private void applyBalance(
        Account account,
        Transaction transaction,
        boolean reverse
    ) {
        BigDecimal amount = transaction.getAmount();

        if (transaction.getTransactionType() == TransactionType.EXPENSE) {
            amount = amount.negate();
        }

        if (reverse) {
            amount = amount.negate();
        }

        account.setCurrentBalance(
            account.getCurrentBalance().add(amount)
        );
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getAccountId(),
            transaction.getCategoryId(),
            transaction.getTransactionType(),
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getTransactionDate(),
            transaction.getDueDate(),
            transaction.getPaidAt(),
            transaction.getStatus(),
            transaction.getNotes(),
            transaction.getCreatedAt(),
            transaction.getUpdatedAt()
        );
    }
}
