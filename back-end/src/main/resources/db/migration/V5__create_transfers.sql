CREATE TABLE transfers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    source_account_id UUID NOT NULL,

    destination_account_id UUID NOT NULL,

    amount NUMERIC(15,2) NOT NULL,

    description VARCHAR(255),

    transfer_date DATE NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',

    notes TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transfers_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_transfers_source
        FOREIGN KEY (source_account_id, user_id)
        REFERENCES accounts(id, user_id),

    CONSTRAINT fk_transfers_destination
        FOREIGN KEY (destination_account_id, user_id)
        REFERENCES accounts(id, user_id),

    CONSTRAINT chk_transfers_amount
        CHECK (amount > 0),

    CONSTRAINT chk_transfers_different_accounts
        CHECK (source_account_id <> destination_account_id),

    CONSTRAINT chk_transfers_status
        CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_transfers_user
    ON transfers(user_id);

CREATE INDEX idx_transfers_date
    ON transfers(user_id, transfer_date);