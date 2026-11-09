ALTER TABLE accounts
ADD CONSTRAINT uq_accounts_id_user
UNIQUE (id, user_id);

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    account_id UUID NOT NULL,

    category_id UUID,

    transaction_type VARCHAR(20) NOT NULL,

    description VARCHAR(255) NOT NULL,

    amount NUMERIC(15,2) NOT NULL,

    transaction_date DATE NOT NULL,

    due_date DATE,

    paid_at TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',

    notes TEXT,

    recurring_transaction_id UUID,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_transactions_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id, user_id)
        REFERENCES accounts(id, user_id),

    CONSTRAINT fk_transactions_category
        FOREIGN KEY (category_id, user_id)
        REFERENCES categories(id, user_id),

    CONSTRAINT chk_transactions_type
        CHECK (transaction_type IN ('INCOME', 'EXPENSE')),

    CONSTRAINT chk_transactions_status
        CHECK (status IN (
            'PENDING',
            'COMPLETED',
            'CANCELLED',
            'OVERDUE'
        )),

    CONSTRAINT chk_transactions_amount
        CHECK (amount > 0)
);

CREATE INDEX idx_transactions_user
    ON transactions(user_id);

CREATE INDEX idx_transactions_account
    ON transactions(account_id);

CREATE INDEX idx_transactions_category
    ON transactions(category_id);

CREATE INDEX idx_transactions_date
    ON transactions(user_id, transaction_date);

CREATE INDEX idx_transactions_status
    ON transactions(user_id, status);
