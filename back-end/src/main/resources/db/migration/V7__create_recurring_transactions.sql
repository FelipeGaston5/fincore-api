CREATE TABLE recurring_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    account_id UUID NOT NULL,

    category_id UUID,

    transaction_type VARCHAR(20) NOT NULL,

    description VARCHAR(255) NOT NULL,

    amount NUMERIC(15,2) NOT NULL,

    frequency VARCHAR(20) NOT NULL,

    interval_value INTEGER NOT NULL DEFAULT 1,

    start_date DATE NOT NULL,

    end_date DATE,

    next_execution_date DATE NOT NULL,

    last_execution_date DATE,

    day_of_month INTEGER,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    notes TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_recurring_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_recurring_account
        FOREIGN KEY (account_id, user_id)
        REFERENCES accounts(id, user_id),

    CONSTRAINT fk_recurring_category
        FOREIGN KEY (category_id, user_id)
        REFERENCES categories(id, user_id),

    CONSTRAINT chk_recurring_type
        CHECK (transaction_type IN ('INCOME', 'EXPENSE')),

    CONSTRAINT chk_recurring_amount
        CHECK (amount > 0),

    CONSTRAINT chk_recurring_frequency
        CHECK (frequency IN (
            'DAILY',
            'WEEKLY',
            'MONTHLY',
            'YEARLY'
        )),

    CONSTRAINT chk_recurring_interval
        CHECK (interval_value > 0),

    CONSTRAINT chk_recurring_day
        CHECK (day_of_month IS NULL OR day_of_month BETWEEN 1 AND 31),

    CONSTRAINT chk_recurring_status
        CHECK (status IN ('ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED')),

    CONSTRAINT chk_recurring_dates
        CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_recurring_user
    ON recurring_transactions(user_id);

CREATE INDEX idx_recurring_execution
    ON recurring_transactions(next_execution_date)
    WHERE status = 'ACTIVE';