----------------------------------------------------------------------------
-- SCHEMA users
----------------------------------------------------------------------------
-- CREATION OF TABLE users.accounts
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users.accounts
(
    account_id                uuid                                                  NOT NULL,
    account_registration_ts   timestamptz                                           NOT NULL DEFAULT NOW(),
    account_type              VARCHAR(5)                                            NOT NULL,

    -- account enable / disable
    account_locked            boolean                                               NOT NULL DEFAULT false,
    account_locked_ts         timestamptz                                           NOT NULL DEFAULT NOW(),
    account_lock_reason       character varying(2048)                               NULL,

    -- constraints
    CONSTRAINT accounts__account_id__pkey       PRIMARY KEY (account_id),
    CONSTRAINT accounts__account_type__check    CHECK (account_type in ('ADMIN', 'USER'))
);

-- indices
CREATE INDEX IF NOT EXISTS account_id    ON users.accounts (account_id);

-- function && trigger for account_enabled update
CREATE OR REPLACE FUNCTION users.update_account_enabled_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.account_locked_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP   TRIGGER IF EXISTS account_enabled ON users.accounts;
CREATE TRIGGER account_enabled
    BEFORE UPDATE OF account_locked ON users.accounts
    FOR EACH ROW
    EXECUTE PROCEDURE users.update_account_enabled_ts();
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE users.accounts
----------------------------------------------------------------------------
-- CREATION OF TABLE users.account_members
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users.account_members
(
    -- account where the this email address belongs
    account_id                 uuid                                                  NOT NULL,
    account_member_locked      boolean                                               NOT NULL DEFAULT false,
    account_member_locked_ts   timestamptz                                           NOT NULL DEFAULT NOW(),
    account_member_lock_reason character varying(2048)                               NULL,

    -- email address
    email_address              character varying(250)   COLLATE pg_catalog."default" NOT NULL,
    email_address_ts           timestamptz                                           NOT NULL DEFAULT NOW(),
    -- email address verification
    email_address_verified     boolean                                               NOT NULL DEFAULT false,
    email_address_verified_ts  timestamptz                                           NULL,

    -- password change
    password_hash              character varying(128)                                NOT NULL,
    password_hash_ts           timestamptz                                           NOT NULL DEFAULT NOW(),

    -- constraints
    CONSTRAINT account_members__account_id__fg_key     FOREIGN KEY (account_id)      REFERENCES users.accounts(account_id),
    CONSTRAINT account_members__email_address__unique  UNIQUE      (email_address),
    CONSTRAINT account_members__password_hash__unique  UNIQUE      (password_hash)
);

