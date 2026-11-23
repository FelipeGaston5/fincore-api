
package com.fincore.fincore_api.modules.budget.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fincore.fincore_api.modules.budget.dto.*;
import com.fincore.fincore_api.modules.budget.entity.Budget;
import com.fincore.fincore_api.modules.budget.enums.BudgetPeriod;
import com.fincore.fincore_api.modules.budget.repository.BudgetRepository;
import com.fincore.fincore_api.modules.category.entity.Category;
import com.fincore.fincore_api.modules.category.enums.CategoryType;
import com.fincore.fincore_api.modules.category.repository.CategoryRepository;
import com.fincore.fincore_api.modules.transaction.enums.TransactionStatus;
import com.fincore.fincore_api.modules.transaction.enums.TransactionType;
import com.fincore.fincore_api.modules.transaction.repository.TransactionRepository;
import com.fincore.fincore_api.modules.user.entity.User;
import com.fincore.fincore_api.modules.user.repository.UserRepository;

@Service
public class BudgetService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal DEFAULT_ALERT = new BigDecimal("80.00");

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public BudgetService(
        BudgetRepository budgetRepository,
        CategoryRepository categoryRepository,
        TransactionRepository transactionRepository,
        UserRepository userRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BudgetResponse create(CreateBudgetRequest request, String email) {
        User user = findUser(email);

        validateDates(
            request.periodType(),
            request.startDate(),
            request.endDate()
        );

        validateCategory(request.categoryId(), user.getId());

        Budget budget = new Budget();
        budget.setUserId(user.getId());

        apply(
            budget,
            request.categoryId(),
            request.name(),
            request.amountLimit(),
            request.periodType(),
            request.startDate(),
            request.endDate(),
            request.alertPercentage()
        );

        return toResponse(budgetRepository.save(budget));
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> findAll(String email) {
        UUID userId = findUser(email).getId();

        return budgetRepository
            .findAllByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public BudgetResponse findById(UUID id, String email) {
        UUID userId = findUser(email).getId();

        return toResponse(findBudget(id, userId));
    }

    @Transactional
    public BudgetResponse update(
        UUID id,
        UpdateBudgetRequest request,
        String email
    ) {
        UUID userId = findUser(email).getId();
        Budget budget = findBudget(id, userId);

        validateDates(
            request.periodType(),
            request.startDate(),
            request.endDate()
        );

        validateCategory(request.categoryId(), userId);

        apply(
            budget,
            request.categoryId(),
            request.name(),
            request.amountLimit(),
            request.periodType(),
            request.startDate(),
            request.endDate(),
            request.alertPercentage()
        );

        budget.setActive(request.active());

        return toResponse(budgetRepository.save(budget));
    }

    @Transactional
    public void delete(UUID id, String email) {
        UUID userId = findUser(email).getId();
        Budget budget = findBudget(id, userId);

        budget.setActive(false);
        budgetRepository.save(budget);
    }

    private User findUser(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        return userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Usuário não encontrado"
            ));
    }

    private Budget findBudget(UUID id, UUID userId) {
        return budgetRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Orçamento não encontrado"
            ));
    }

    private void validateCategory(UUID categoryId, UUID userId) {
        Category category = categoryRepository
            .findByIdAndUserIdAndActiveTrue(categoryId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Categoria inválida ou inativa"
            ));

        if (category.getCategoryType() != CategoryType.EXPENSE) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Orçamentos só podem utilizar categorias de despesa"
            );
        }
    }

    private void validateDates(
        BudgetPeriod period,
        LocalDate start,
        LocalDate end
    ) {
        if (end != null && end.isBefore(start)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A data final não pode ser anterior à data inicial"
            );
        }

        if (period == BudgetPeriod.CUSTOM && end == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O período CUSTOM exige uma data final"
            );
        }

        if (period != BudgetPeriod.CUSTOM && end != null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Somente o período CUSTOM aceita data final"
            );
        }
    }

    private void apply(
        Budget budget,
        UUID categoryId,
        String name,
        BigDecimal limit,
        BudgetPeriod period,
        LocalDate start,
        LocalDate end,
        BigDecimal alert
    ) {
        budget.setCategoryId(categoryId);
        budget.setName(name.trim());
        budget.setAmountLimit(limit);
        budget.setPeriodType(period);
        budget.setStartDate(start);
        budget.setEndDate(end);
        budget.setAlertPercentage(
            alert != null ? alert : DEFAULT_ALERT
        );
    }

    private BudgetResponse toResponse(Budget budget) {
        LocalDate today = LocalDate.now();

        LocalDate effectiveStart = periodStart(budget, today);
        LocalDate effectiveEnd = periodEnd(budget, today);

        BigDecimal spent = transactionRepository.sumCompletedExpenses(
            budget.getUserId(),
            budget.getCategoryId(),
            effectiveStart,
            effectiveEnd,
            TransactionType.EXPENSE,
            TransactionStatus.COMPLETED
        );

        if (spent == null) {
            spent = ZERO;
        }

        BigDecimal remaining = budget.getAmountLimit().subtract(spent);

        BigDecimal percentage = spent
            .multiply(HUNDRED)
            .divide(
                budget.getAmountLimit(),
                2,
                RoundingMode.HALF_UP
            );

        boolean alert = percentage.compareTo(
            budget.getAlertPercentage()
        ) >= 0;

        boolean exceeded = spent.compareTo(
            budget.getAmountLimit()
        ) > 0;

        return new BudgetResponse(
            budget.getId(),
            budget.getCategoryId(),
            budget.getName(),
            budget.getAmountLimit(),
            budget.getPeriodType(),
            budget.getStartDate(),
            budget.getEndDate(),
            budget.getAlertPercentage(),
            budget.isActive(),
            spent,
            remaining,
            percentage,
            alert,
            exceeded,
            budget.getCreatedAt(),
            budget.getUpdatedAt()
        );
    }

    private LocalDate periodStart(Budget budget, LocalDate today) {
        LocalDate anchor = budget.getStartDate();

        if (today.isBefore(anchor)) {
            return anchor;
        }

        long index = cycleIndex(budget, today);

        return switch (budget.getPeriodType()) {
            case WEEKLY -> anchor.plusWeeks(index);
            case MONTHLY -> anchor.plusMonths(index);
            case YEARLY -> anchor.plusYears(index);
            case CUSTOM -> anchor;
        };
    }

    private LocalDate periodEnd(Budget budget, LocalDate today) {
        LocalDate anchor = budget.getStartDate();

        if (budget.getPeriodType() == BudgetPeriod.CUSTOM) {
            return budget.getEndDate();
        }

        long index = cycleIndex(budget, today);

        return switch (budget.getPeriodType()) {
            case WEEKLY -> anchor.plusWeeks(index + 1).minusDays(1);
            case MONTHLY -> anchor.plusMonths(index + 1).minusDays(1);
            case YEARLY -> anchor.plusYears(index + 1).minusDays(1);
            case CUSTOM -> budget.getEndDate();
        };
    }

    private long cycleIndex(Budget budget, LocalDate today) {
        LocalDate anchor = budget.getStartDate();

        if (today.isBefore(anchor)) {
            return 0;
        }

        long index = switch (budget.getPeriodType()) {
            case WEEKLY ->
                ChronoUnit.DAYS.between(anchor, today) / 7;

            case MONTHLY ->
                ChronoUnit.MONTHS.between(
                    YearMonth.from(anchor),
                    YearMonth.from(today)
                );

            case YEARLY ->
                today.getYear() - anchor.getYear();

            case CUSTOM -> 0;
        };

        LocalDate candidate = switch (budget.getPeriodType()) {
            case WEEKLY -> anchor.plusWeeks(index);
            case MONTHLY -> anchor.plusMonths(index);
            case YEARLY -> anchor.plusYears(index);
            case CUSTOM -> anchor;
        };

        if (candidate.isAfter(today) && index > 0) {
            index--;
        }

        return index;
    }
}
