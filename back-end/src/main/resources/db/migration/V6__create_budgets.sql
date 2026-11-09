CREATE TABLE budgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    category_id UUID NOT NULL,

    name VARCHAR(120) NOT NULL,

    amount_limit NUMERIC(15,2) NOT NULL,

    period_type VARCHAR(20) NOT NULL,

    start_date DATE NOT NULL,

    end_date DATE,

    alert_percentage NUMERIC(5,2) NOT NULL DEFAULT 80.00,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_budgets_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_budgets_category
        FOREIGN KEY (category_id, user_id)
        REFERENCES categories(id, user_id),

    CONSTRAINT chk_budgets_amount
        CHECK (amount_limit > 0),

    CONSTRAINT chk_budgets_period
        CHECK (period_type IN (
            'WEEKLY',
            'MONTHLY',
            'YEARLY',
            'CUSTOM'
        )),

    CONSTRAINT chk_budgets_dates
        CHECK (end_date IS NULL OR end_date >= start_date),

    CONSTRAINT chk_budgets_alert
        CHECK (alert_percentage > 0 AND alert_percentage <= 100)
);

CREATE INDEX idx_budgets_user
    ON budgets(user_id);

CREATE INDEX idx_budgets_category
    ON budgets(category_id);