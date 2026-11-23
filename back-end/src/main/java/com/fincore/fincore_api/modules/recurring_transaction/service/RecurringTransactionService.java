package com.fincore.fincore_api.modules.recurring_transaction.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fincore.fincore_api.modules.account.repository.AccountRepository;
import com.fincore.fincore_api.modules.category.entity.Category;
import com.fincore.fincore_api.modules.category.enums.CategoryType;
import com.fincore.fincore_api.modules.category.repository.CategoryRepository;
import com.fincore.fincore_api.modules.recurring_transaction.dto.CreateRecurringTransactionRequest;
import com.fincore.fincore_api.modules.recurring_transaction.dto.RecurringTransactionResponse;
import com.fincore.fincore_api.modules.recurring_transaction.dto.UpdateRecurringTransactionRequest;
import com.fincore.fincore_api.modules.recurring_transaction.entity.RecurringTransaction;
import com.fincore.fincore_api.modules.recurring_transaction.enums.RecurrenceFrequency;
import com.fincore.fincore_api.modules.recurring_transaction.enums.RecurringTransactionStatus;
import com.fincore.fincore_api.modules.recurring_transaction.repository.RecurringTransactionRepository;
import com.fincore.fincore_api.modules.transaction.dto.TransactionResponse;
import com.fincore.fincore_api.modules.transaction.enums.TransactionStatus;
import com.fincore.fincore_api.modules.transaction.enums.TransactionType;
import com.fincore.fincore_api.modules.transaction.service.TransactionService;
import com.fincore.fincore_api.modules.user.entity.User;
import com.fincore.fincore_api.modules.user.repository.UserRepository;

@Service
public class RecurringTransactionService {

    private static final int MAX_CATCH_UP_EXECUTIONS = 100;

    private final RecurringTransactionRepository recurringRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public RecurringTransactionService(
        RecurringTransactionRepository recurringRepository,
        AccountRepository accountRepository,
        CategoryRepository categoryRepository,
        UserRepository userRepository,
        TransactionService transactionService
    ) {
        this.recurringRepository = recurringRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
    }

    @Transactional
    public RecurringTransactionResponse create(
        CreateRecurringTransactionRequest request,
        String email
    ) {
        User user = findUser(email);
        validateRequest(
            request.accountId(), request.categoryId(), request.transactionType(),
            request.frequency(), request.startDate(), request.endDate(),
            request.dayOfMonth(), user.getId()
        );

        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setUserId(user.getId());
        apply(
            recurring, request.accountId(), request.categoryId(),
            request.transactionType(), request.description(), request.amount(),
            request.frequency(), request.intervalValue(), request.startDate(),
            request.endDate(), request.dayOfMonth(), request.notes()
        );
        recurring.setNextExecutionDate(request.startDate());
        recurring.setStatus(RecurringTransactionStatus.ACTIVE);

        return toResponse(recurringRepository.save(recurring));
    }

    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> findAll(String email) {
        UUID userId = findUser(email).getId();
        return recurringRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RecurringTransactionResponse findById(UUID id, String email) {
        UUID userId = findUser(email).getId();
        return toResponse(findOwned(id, userId));
    }

    @Transactional
    public RecurringTransactionResponse update(
        UUID id,
        UpdateRecurringTransactionRequest request,
        String email
    ) {
        UUID userId = findUser(email).getId();
        RecurringTransaction recurring = findOwned(id, userId);

        validateRequest(
            request.accountId(), request.categoryId(), request.transactionType(),
            request.frequency(), request.startDate(), request.endDate(),
            request.dayOfMonth(), userId
        );

        boolean scheduleChanged =
            recurring.getFrequency() != request.frequency()
            || !recurring.getIntervalValue().equals(request.intervalValue())
            || !recurring.getStartDate().equals(request.startDate())
            || !java.util.Objects.equals(recurring.getEndDate(), request.endDate())
            || !java.util.Objects.equals(recurring.getDayOfMonth(), request.dayOfMonth());

        if (scheduleChanged && recurring.getLastExecutionDate() != null) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A programação não pode ser alterada depois da primeira execução."
            );
        }

        apply(
            recurring, request.accountId(), request.categoryId(),
            request.transactionType(), request.description(), request.amount(),
            request.frequency(), request.intervalValue(), request.startDate(),
            request.endDate(), request.dayOfMonth(), request.notes()
        );

        if (scheduleChanged || recurring.getLastExecutionDate() == null) {
            recurring.setNextExecutionDate(request.startDate());
        }

        return toResponse(recurringRepository.save(recurring));
    }

    @Transactional
    public RecurringTransactionResponse pause(UUID id, String email) {
        RecurringTransaction recurring = findOwned(id, findUser(email).getId());
        requireStatus(recurring, RecurringTransactionStatus.ACTIVE);
        recurring.setStatus(RecurringTransactionStatus.PAUSED);
        return toResponse(recurringRepository.save(recurring));
    }

    @Transactional
    public RecurringTransactionResponse resume(UUID id, String email) {
        RecurringTransaction recurring = findOwned(id, findUser(email).getId());
        requireStatus(recurring, RecurringTransactionStatus.PAUSED);
        recurring.setStatus(
            recurring.getEndDate() != null
                && recurring.getNextExecutionDate().isAfter(recurring.getEndDate())
                ? RecurringTransactionStatus.COMPLETED
                : RecurringTransactionStatus.ACTIVE
        );
        return toResponse(recurringRepository.save(recurring));
    }

    @Transactional
    public RecurringTransactionResponse cancel(UUID id, String email) {
        RecurringTransaction recurring = findOwned(id, findUser(email).getId());

        if (recurring.getStatus() == RecurringTransactionStatus.COMPLETED
            || recurring.getStatus() == RecurringTransactionStatus.CANCELLED) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Esta recorrência já foi finalizada ou cancelada."
            );
        }

        recurring.setStatus(RecurringTransactionStatus.CANCELLED);
        return toResponse(recurringRepository.save(recurring));
    }

    @Transactional
    public int processDue(UUID id) {
        RecurringTransaction recurring = recurringRepository.findByIdForUpdate(id)
            .orElse(null);

        if (recurring == null
            || recurring.getStatus() != RecurringTransactionStatus.ACTIVE) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        int processed = 0;

        while (processed < MAX_CATCH_UP_EXECUTIONS
            && !recurring.getNextExecutionDate().isAfter(today)) {

            LocalDate executionDate = recurring.getNextExecutionDate();

            if (recurring.getEndDate() != null
                && executionDate.isAfter(recurring.getEndDate())) {
                recurring.setStatus(RecurringTransactionStatus.COMPLETED);
                break;
            }

            TransactionResponse generated = transactionService.createFromRecurring(
                recurring.getUserId(),
                recurring.getId(),
                recurring.getAccountId(),
                recurring.getCategoryId(),
                recurring.getTransactionType(),
                recurring.getDescription(),
                recurring.getAmount(),
                executionDate,
                recurring.getNotes()
            );

            if (generated.status() != TransactionStatus.PENDING) {
                throw new IllegalStateException(
                    "A transação recorrente deveria ser criada como PENDING."
                );
            }

            recurring.setLastExecutionDate(executionDate);
            recurring.setNextExecutionDate(nextExecutionDate(recurring, executionDate));
            processed++;
        }

        if (recurring.getEndDate() != null
            && recurring.getNextExecutionDate().isAfter(recurring.getEndDate())) {
            recurring.setStatus(RecurringTransactionStatus.COMPLETED);
        }

        recurringRepository.save(recurring);
        return processed;
    }

    private LocalDate nextExecutionDate(
        RecurringTransaction recurring,
        LocalDate current
    ) {
        int interval = recurring.getIntervalValue();

        return switch (recurring.getFrequency()) {
            case DAILY -> current.plusDays(interval);
            case WEEKLY -> current.plusWeeks(interval);
            case MONTHLY -> {
                YearMonth month = YearMonth.from(current).plusMonths(interval);
                int day = recurring.getDayOfMonth() != null
                    ? recurring.getDayOfMonth()
                    : recurring.getStartDate().getDayOfMonth();
                yield month.atDay(Math.min(day, month.lengthOfMonth()));
            }
            case YEARLY -> {
                int year = current.getYear() + interval;
                YearMonth month = YearMonth.of(
                    year,
                    recurring.getStartDate().getMonth()
                );
                yield month.atDay(
                    Math.min(recurring.getStartDate().getDayOfMonth(), month.lengthOfMonth())
                );
            }
        };
    }

    private void validateRequest(
        UUID accountId,
        UUID categoryId,
        TransactionType type,
        RecurrenceFrequency frequency,
        LocalDate startDate,
        LocalDate endDate,
        Integer dayOfMonth,
        UUID userId
    ) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A data final não pode ser anterior à data inicial."
            );
        }

        if (dayOfMonth != null && frequency != RecurrenceFrequency.MONTHLY) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "dayOfMonth só pode ser informado para recorrências mensais."
            );
        }

        if (accountRepository.findByIdAndUserIdAndActiveTrue(accountId, userId).isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Conta não encontrada ou inativa."
            );
        }

        if (categoryId == null) {
            return;
        }

        Category category = categoryRepository
            .findByIdAndUserIdAndActiveTrue(categoryId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Categoria não encontrada ou inativa."
            ));

        CategoryType expected = type == TransactionType.INCOME
            ? CategoryType.INCOME : CategoryType.EXPENSE;

        if (category.getCategoryType() != expected) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A categoria não corresponde ao tipo da recorrência."
            );
        }
    }

    private void apply(
        RecurringTransaction recurring,
        UUID accountId,
        UUID categoryId,
        TransactionType type,
        String description,
        java.math.BigDecimal amount,
        RecurrenceFrequency frequency,
        Integer interval,
        LocalDate startDate,
        LocalDate endDate,
        Integer dayOfMonth,
        String notes
    ) {
        recurring.setAccountId(accountId);
        recurring.setCategoryId(categoryId);
        recurring.setTransactionType(type);
        recurring.setDescription(description.trim());
        recurring.setAmount(amount);
        recurring.setFrequency(frequency);
        recurring.setIntervalValue(interval);
        recurring.setStartDate(startDate);
        recurring.setEndDate(endDate);
        recurring.setDayOfMonth(
            frequency == RecurrenceFrequency.MONTHLY
                ? (dayOfMonth != null ? dayOfMonth : startDate.getDayOfMonth())
                : null
        );
        recurring.setNotes(notes);
    }

    private void requireStatus(
        RecurringTransaction recurring,
        RecurringTransactionStatus expected
    ) {
        if (recurring.getStatus() != expected) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A operação não é permitida para o status atual."
            );
        }
    }

    private User findUser(String email) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return userRepository.findByEmail(normalized)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuário autenticado não encontrado."
            ));
    }

    private RecurringTransaction findOwned(UUID id, UUID userId) {
        return recurringRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Recorrência não encontrada."
            ));
    }

    private RecurringTransactionResponse toResponse(RecurringTransaction r) {
        return new RecurringTransactionResponse(
            r.getId(), r.getAccountId(), r.getCategoryId(),
            r.getTransactionType(), r.getDescription(), r.getAmount(),
            r.getFrequency(), r.getIntervalValue(), r.getStartDate(),
            r.getEndDate(), r.getNextExecutionDate(), r.getLastExecutionDate(),
            r.getDayOfMonth(), r.getStatus(), r.getNotes(),
            r.getCreatedAt(), r.getUpdatedAt()
        );
    }
}
