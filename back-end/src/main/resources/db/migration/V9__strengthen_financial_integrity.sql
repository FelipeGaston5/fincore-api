
-- =====================================================
-- V9 - REFORCO DA INTEGRIDADE FINANCEIRA
-- =====================================================

-- 1. Impede nomes duplicados entre contas ativas
-- do mesmo usuário, ignorando maiúsculas e espaços externos.

CREATE UNIQUE INDEX uq_accounts_active_user_name
ON accounts (
    user_id,
    LOWER(BTRIM(name))
)
WHERE is_active = TRUE;


-- 2. Impede nomes duplicados entre categorias ativas
-- do mesmo usuário e do mesmo tipo.

CREATE UNIQUE INDEX uq_categories_active_user_type_name
ON categories (
    user_id,
    category_type,
    LOWER(BTRIM(name))
)
WHERE is_active = TRUE;


-- 3. Permite que uma FK valide simultaneamente:
-- ID da categoria, proprietário e tipo.

ALTER TABLE categories
ADD CONSTRAINT uq_categories_id_user_type
UNIQUE (id, user_id, category_type);


-- 4. Remove a FK antiga que validava somente
-- a existência da categoria-pai.

ALTER TABLE categories
DROP CONSTRAINT fk_categories_parent;


-- 5. Cria uma FK composta.
-- A categoria-pai precisa pertencer ao mesmo usuário
-- e possuir o mesmo tipo da categoria filha.

ALTER TABLE categories
ADD CONSTRAINT fk_categories_parent_owner_type
FOREIGN KEY (
    parent_category_id,
    user_id,
    category_type
)
REFERENCES categories (
    id,
    user_id,
    category_type
);
