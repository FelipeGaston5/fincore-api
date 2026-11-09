CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    name VARCHAR(100) NOT NULL,

    description VARCHAR(255),

    category_type VARCHAR(20) NOT NULL,

    color VARCHAR(7),

    icon VARCHAR(50),

    parent_category_id UUID,

    is_system BOOLEAN NOT NULL DEFAULT FALSE,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_categories_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_category_id) REFERENCES categories(id),

    CONSTRAINT chk_categories_type
        CHECK (category_type IN ('INCOME', 'EXPENSE')),

    CONSTRAINT uq_categories_id_user
        UNIQUE (id, user_id)
);

CREATE INDEX idx_categories_user
    ON categories(user_id);

CREATE INDEX idx_categories_parent
    ON categories(parent_category_id);