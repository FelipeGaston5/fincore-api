CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    name VARCHAR(100) NOT NULL,

    account_type VARCHAR(30) NOT NULL,

    institution VARCHAR(120),

    initial_balance NUMERIC(15,2) NOT NULL DEFAULT 0.00,

    current_balance NUMERIC(15,2) NOT NULL DEFAULT 0.00,

    currency CHAR(3) NOT NULL DEFAULT 'BRL',

    color VARCHAR(7),

    icon VARCHAR(50),

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_accounts_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT chk_accounts_type
        CHECK (account_type IN (
            'CHECKING',
            'SAVINGS',
            'CASH',
            'WALLET',
            'PREPAID',
            'INVESTMENT',
            'OTHER'
        ))
);

CREATE INDEX idx_accounts_user
    ON accounts(user_id);