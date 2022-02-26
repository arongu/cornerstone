----------------------------------------------------------------------------
-- SCHEMA accounts
----------------------------------------------------------------------------
-- CREATION OF TABLE accounts.groups
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS accounts.account_groups
(
    account_group_created              timestamptz                                           NOT NULL DEFAULT NOW(),
    account_group_id                   uuid                                                  NOT NULL,
    account_group_owner_id             uuid                                                  NOT NULL UNIQUE,
    account_group_owner_id_ts          timestamptz                                           NOT NULL DEFAULT NOW(),
    account_group_name                 character varying(120)                                NOT NULL UNIQUE,
    account_group_name_ts              timestamptz                                           NOT NULL DEFAULT NOW(),
    account_group_name_alternative     character varying(120)                                UNIQUE,
    account_group_name_alternative_ts  timestamptz,
    account_group_notes                character varying(4096),
    account_group_notes_ts             character varying(4096)                               NOT NULL DEFAULT NOW(),

    -- account enable / disable
    account_group_locked               boolean                                               NOT NULL DEFAULT false,
    account_group_locked_ts            timestamptz                                           NOT NULL DEFAULT NOW(),
    account_group_lock_reason          character varying(2048),
    account_group_lock_reason_ts       character varying(2048),

    -- for multiple accounts
    account_group_max_members                  integer                                             NOT NULL DEFAULT 1,
    account_group_max_members_ts               timestamptz                                         NOT NULL DEFAULT NOW(),
    account_group_current_members              integer                                             NOT NULL DEFAULT 1,
    account_group_current_members_ts           timestamptz                                         NOT NULL DEFAULT NOW(),

    -- constraints
    CONSTRAINT pkey__group_id  PRIMARY KEY (account_group_id)
);

-- indices
CREATE INDEX IF NOT EXISTS group_id       ON accounts.account_groups (account_group_id);
CREATE INDEX IF NOT EXISTS group_owner_id ON accounts.account_groups (account_group_owner_id);

-- functions && triggers
-- account_group_owner_id update timestamp on change
CREATE OR REPLACE FUNCTION accounts.account_groups__account_group_owner_id_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.account_group_owner_id_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_account_group_owner_id_ts ON accounts.account_groups;
        CREATE TRIGGER update_account_group_owner_id_ts BEFORE UPDATE OF account_group_owner_id ON accounts.account_groups
            FOR EACH ROW EXECUTE PROCEDURE accounts.account_groups__account_group_owner_id_ts();

-- account_group_name update timestamp on change
CREATE OR REPLACE FUNCTION accounts.account_groups__account_group_name_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.account_group_name_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_account_group_name_ts ON accounts.account_groups;
        CREATE TRIGGER update_account_group_name_ts BEFORE UPDATE OF account_group_name ON accounts.account_groups
            FOR EACH ROW EXECUTE PROCEDURE accounts.account_groups__account_group_name_ts();

-- account_group_name_alternative update timestamp on change
CREATE OR REPLACE FUNCTION accounts.account_groups__account_group_name_alternative_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.account_group_name_alternative_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_account_group_name_alternative_ts ON accounts.account_groups;
        CREATE TRIGGER update_account_group_name_alternative_ts BEFORE UPDATE OF account_group_name ON accounts.account_groups
            FOR EACH ROW EXECUTE PROCEDURE accounts.account_groups__account_group_name_alternative_ts();

-- account_group_notes update timestamp on change
CREATE OR REPLACE FUNCTION accounts.account_groups__account_group_notes_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.account_group_notes_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_account_group_notes_ts ON accounts.account_groups;
        CREATE TRIGGER update_account_group_notes_ts BEFORE UPDATE OF account_group_notes ON accounts.account_groups
            FOR EACH ROW EXECUTE PROCEDURE accounts.account_groups__account_group_notes_ts();

-- account_group_locked update timestamp on change
CREATE OR REPLACE FUNCTION accounts.account_groups__account_group_locked_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.account_group_locked_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_account_group_locked_ts ON accounts.account_groups;
        CREATE TRIGGER update_account_group_locked_ts BEFORE UPDATE OF account_group_locked ON accounts.account_groups
            FOR EACH ROW EXECUTE PROCEDURE accounts.account_groups__account_group_locked_ts();

-- account_group_lock_reason update timestamp on change
CREATE OR REPLACE FUNCTION accounts.account_groups__account_group_lock_reason_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.account_group_lock_reason_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_account_group_lock_reason_ts ON accounts.account_groups;
        CREATE TRIGGER update_account_group_lock_reason_ts BEFORE UPDATE OF account_group_lock_reason ON accounts.account_groups
            FOR EACH ROW EXECUTE PROCEDURE accounts.account_groups__account_group_lock_reason_ts();

-- account_group_current_members update timestamp on change
CREATE OR REPLACE FUNCTION accounts.account_groups__account_group_current_members_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.account_group_current_members_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_account_current_members_ts ON accounts.account_groups;
        CREATE TRIGGER update_account_current_members_ts BEFORE UPDATE OF account_group_current_members ON accounts.account_groups
            FOR EACH ROW EXECUTE PROCEDURE accounts.account_groups__account_group_current_members_ts();

-- account_group_max_members_ts update timestamp on change
CREATE OR REPLACE FUNCTION accounts.account_groups__account_group_max_members_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.account_group_max_members_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_account_group_members_max_ts ON accounts.account_groups;
        CREATE TRIGGER update_account_group_members_max_ts BEFORE UPDATE OF account_group_max_members ON accounts.account_groups
            FOR EACH ROW EXECUTE PROCEDURE accounts.account_groups__account_group_max_members_ts();
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE accounts.groups
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- CREATION OF TABLE accounts.accounts
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS accounts.accounts
(
    account_group_id                uuid,
    -- account
    account_id                      uuid                                                    NOT NULL UNIQUE,
    account_creation_ts             timestamptz                                             NOT NULL DEFAULT NOW(),
    account_locked                  boolean                                                 NOT NULL DEFAULT false,
    account_locked_ts               timestamptz,
    account_lock_reason             character varying(2048),
    account_lock_reason_ts          timestamptz,

    -- login
    login_attempts                  integer                                                 NOT NULL DEFAULT 0,
    last_login_attempt_ip           character varying (32) COLLATE pg_catalog."default",
    last_login_attempt_ip_ts        timestamptz,
    last_successful_login_ip        character varying(32) COLLATE pg_catalog."default",
    last_successful_login_ip_ts     timestamptz,

    -- email
    email_address                   character varying(250) COLLATE pg_catalog."default"     NOT NULL UNIQUE,
    email_address_ts                timestamptz,
    email_address_verified          boolean                                                 NOT NULL DEFAULT false,
    email_address_verified_ts       timestamptz,

    -- password
    password_hash                   character varying(128)                                  NOT NULL UNIQUE,
    password_hash_ts                timestamptz,

    -- superuser
    superuser                       boolean                                                 NOT NULL DEFAULT false,
    superuser_ts                    timestamptz,

    -- constraints
    CONSTRAINT pkey__id             PRIMARY KEY (account_id),
    CONSTRAINT fkey__group_id       FOREIGN KEY (account_group_id)  REFERENCES accounts.account_groups(account_group_id)
);
-- back reference group -> owner (back reference must be done with alter, because the two tables does not exist at creation time)
ALTER TABLE accounts.account_groups ADD CONSTRAINT fkey__owner_id FOREIGN KEY (account_group_owner_id) REFERENCES accounts.accounts(account_id);

-- indices --
CREATE INDEX IF NOT EXISTS account_id    ON accounts.accounts(account_id);
CREATE INDEX IF NOT EXISTS email_address ON accounts.accounts(email_address);

-- functions
-- account_locked
CREATE OR REPLACE FUNCTION accounts.accounts__account_locked_ts() RETURNS TRIGGER AS $$
    BEGIN NEW.account_locked_ts = NOW();
        RETURN NEW;
    END $$ LANGUAGE plpgsql;

-- account_lock_reason
CREATE OR REPLACE FUNCTION accounts.accounts__account_lock_reason_ts() RETURNS TRIGGER AS $$
    BEGIN NEW.account_lock_reason_ts = NOW();
        RETURN NEW;
    END $$ LANGUAGE plpgsql;

-- last_login_attempt_ip
CREATE OR REPLACE FUNCTION accounts.accounts__last_login_attempt_ip_ts() RETURNS TRIGGER AS $$
    BEGIN NEW.last_login_attempt_ip_ts = NOW();
        RETURN NEW;
    END $$ LANGUAGE plpgsql;

-- last_successful_login_ip
CREATE OR REPLACE FUNCTION accounts.accounts__last_successful_login_ip_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.last_successful_login_ip_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

-- email_address
CREATE OR REPLACE FUNCTION accounts.accounts__email_address_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.email_address_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

-- email_address_verified
CREATE OR REPLACE FUNCTION accounts.accounts__email_address_verified_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.email_address_verified_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

-- password_hash
CREATE OR REPLACE FUNCTION accounts.accounts__password_hash_ts() RETURNS TRIGGER AS $$
BEGIN
    NEW.password_hash_ts = NOW();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- superuser
CREATE OR REPLACE FUNCTION accounts.accounts__superuser_ts() RETURNS TRIGGER AS $$
BEGIN
    NEW.superuser_ts = NOW();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- triggers
-- account_locked
DROP TRIGGER IF EXISTS update_account_locked ON accounts.accounts;
CREATE TRIGGER         update_account_update BEFORE UPDATE OF account_locked ON accounts.accounts FOR EACH ROW EXECUTE PROCEDURE accounts.accounts__account_locked_ts();

-- account_lock_reason
DROP TRIGGER IF EXISTS update_account_lock_reason ON accounts.accounts;
CREATE TRIGGER         update_account_lock_reason BEFORE UPDATE OF account_lock_reason ON accounts.accounts FOR EACH ROW EXECUTE PROCEDURE accounts.accounts__account_lock_reason_ts();

-- last_login_attempt_ip
DROP TRIGGER IF EXISTS update_last_login_attempt_ip ON accounts.accounts;
CREATE TRIGGER         update_last_login_attempt_ip BEFORE UPDATE OF last_login_attempt_ip ON accounts.accounts FOR ROW EXECUTE PROCEDURE accounts.accounts__last_login_attempt_ip_ts();

-- last_successful_login_ip
DROP TRIGGER IF EXISTS update_last_successful_login_ip ON accounts.accounts;
CREATE TRIGGER         update_last_successful_login_ip BEFORE UPDATE OF last_successful_login_ip ON accounts.accounts FOR ROW EXECUTE PROCEDURE accounts.accounts__last_successful_login_ip_ts();

-- email_address
DROP TRIGGER IF EXISTS update_email_address ON accounts.accounts;
CREATE TRIGGER         update_email_address BEFORE UPDATE OF email_address ON accounts.accounts FOR ROW EXECUTE PROCEDURE accounts.accounts__email_address_ts();

-- email_address_verified
DROP TRIGGER IF EXISTS update_email_address_verified ON accounts.accounts;
CREATE TRIGGER         update_email_address_verified BEFORE UPDATE OF email_address_verified ON accounts.accounts FOR ROW EXECUTE PROCEDURE accounts.accounts__email_address_verified_ts();

-- password_hash
DROP TRIGGER IF EXISTS update_password_hash ON accounts.accounts;
CREATE TRIGGER         update_password_hash BEFORE UPDATE OF password_hash ON accounts.accounts FOR ROW EXECUTE PROCEDURE accounts.accounts__password_hash_ts();

-- superuser
DROP TRIGGER IF EXISTS update_superuser ON accounts.accounts;
CREATE TRIGGER         update_superuser BEFORE UPDATE OF password_hash ON accounts.accounts FOR ROW EXECUTE PROCEDURE accounts.accounts__superuser_ts();
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE accounts.accounts
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- CREATION OF TABLE accounts.group_permissions
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS accounts.group_permissions
(
    -- unique 1 account can only have 1 set of permission
    account_id      uuid        NOT NULL UNIQUE ,

    -- group permissions, which group of functions/services can be used roughly translates to (http methods)
    read            boolean     NOT NULL DEFAULT true, -- get, head, options
    write           boolean     NOT NULL DEFAULT true, -- patch, post, put
    delete          boolean     NOT NULL DEFAULT true, -- delete

    -- group level permissions
    group_admin     boolean     NOT NULL DEFAULT false,

    -- constraints
    CONSTRAINT fkey__account_id FOREIGN KEY (account_id) REFERENCES accounts.accounts(account_id)
);
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE accounts.group_permissions
----------------------------------------------------------------------------


----------------------------------------------------------------------------
-- CREATE ROLES/accounts
----------------------------------------------------------------------------
DROP ROLE IF EXISTS ${db_user};
CREATE USER ${db_user} WITH ENCRYPTED PASSWORD '${db_password}';
----------------------------------------------------------------------------
-- END OF CREATION OF ROLES/accounts
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- ____  _____ ____  __  __ ___ ____ ____ ___ ___  _   _ ____
-- |  _ \| ____|  _ \|  \/  |_ _/ ___/ ___|_ _/ _ \| \ | / ___|
-- | |_) |  _| | |_) | |\/| || |\___ \___ \| | | | |  \| \___ \
-- |  __/| |___|  _ <| |  | || | ___) |__) | | |_| | |\  |___) |
-- |_|   |_____|_| \_\_|  |_|___|____/____/___\___/|_| \_|____/
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- PERMISSIONS OF SCHEMA accounts
----------------------------------------------------------------------------
GRANT USAGE ON SCHEMA accounts TO ${db_user};
-- sequences, functions (usage is required to call nextval function)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA accounts TO ${db_user};
GRANT EXECUTE       ON ALL FUNCTIONS IN SCHEMA accounts TO ${db_user};
GRANT REFERENCES    ON ALL TABLES    IN SCHEMA accounts TO ${db_user};

-- TABLES
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE accounts.accounts          TO ${db_user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE accounts.account_groups    TO ${db_user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE accounts.group_permissions TO ${db_user};
----------------------------------------------------------------------------
-- END OF PERMISSIONS OF SCHEMA accounts
----------------------------------------------------------------------------
