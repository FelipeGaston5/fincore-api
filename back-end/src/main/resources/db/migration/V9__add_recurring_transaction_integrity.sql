ALTER TABLE recurring_transactions
    ADD CONSTRAINT uq_recurring_transactions_id_user
    UNIQUE (id, user_id);

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_recurring_user
    FOREIGN KEY (recurring_transaction_id, user_id)
    REFERENCES recurring_transactions (id, user_id);

CREATE UNIQUE INDEX uq_transactions_recurring_execution
    ON transactions (recurring_transaction_id, transaction_date)
    WHERE recurring_transaction_id IS NOT NULL;
