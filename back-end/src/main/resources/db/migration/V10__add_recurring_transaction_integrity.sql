-- Garante que o ID da recorrência esteja associado ao usuário correto.
ALTER TABLE recurring_transactions
    ADD CONSTRAINT uq_recurring_transactions_id_user
    UNIQUE (id, user_id);

-- Garante que uma transação só possa referenciar
-- uma recorrência pertencente ao mesmo usuário.
ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_recurring_user
    FOREIGN KEY (recurring_transaction_id, user_id)
    REFERENCES recurring_transactions (id, user_id);

-- Impede a geração de duas transações para a mesma recorrência
-- na mesma data de execução.
CREATE UNIQUE INDEX uq_transactions_recurring_execution
    ON transactions (recurring_transaction_id, transaction_date)
    WHERE recurring_transaction_id IS NOT NULL;
