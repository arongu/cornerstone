CREATE TABLE IF NOT EXISTS admindb.accounts(
    id SERIAL,
    `account_created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `account_enabled` BOOLEAN NOT NULL,
    `account_enabled_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `email_address` VARCHAR(100) NOT NULL UNIQUE,
    `email_address_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `email_address_verified` BOOLEAN NOT NULL,
    `password_hash` VARCHAR(128) NOT NULL UNIQUE,
    `password_hash_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    INDEX `email_address_idx` (email_address),
    INDEX `id_idx` (id)
);
