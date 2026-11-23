package com.fincore.fincore_api.modules.recurring_transaction.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fincore.fincore_api.modules.recurring_transaction.entity.RecurringTransaction;
import com.fincore.fincore_api.modules.recurring_transaction.enums.RecurringTransactionStatus;
import com.fincore.fincore_api.modules.recurring_transaction.repository.RecurringTransactionRepository;

@Component
public class RecurringTransactionProcessor {

    private static final Logger log =
        LoggerFactory.getLogger(RecurringTransactionProcessor.class);

    private final RecurringTransactionRepository repository;
    private final RecurringTransactionService service;

    public RecurringTransactionProcessor(
        RecurringTransactionRepository repository,
        RecurringTransactionService service
    ) {
        this.repository = repository;
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${fincore.recurring-transaction.processor-interval-ms:60000}")
    public void processDueRecurringTransactions() {
        List<UUID> dueIds = repository
            .findTop100ByStatusAndNextExecutionDateLessThanEqualOrderByNextExecutionDateAsc(
                RecurringTransactionStatus.ACTIVE,
                LocalDate.now()
            )
            .stream()
            .map(RecurringTransaction::getId)
            .toList();

        for (UUID id : dueIds) {
            try {
                int count = service.processDue(id);
                if (count > 0) {
                    log.info("Recorrência {} processada: {} ocorrência(s).", id, count);
                }
            } catch (Exception exception) {
                log.error("Falha ao processar recorrência {}. Será tentada novamente.", id, exception);
            }
        }
    }
}
