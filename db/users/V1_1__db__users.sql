----------------------------------------------------------------------------
-- SCHEMA users
----------------------------------------------------------------------------
-- CREATION OF TABLE users.groups
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users.groups
(
    group_id                   uuid                                                  NOT NULL,
    group_id_ts                timestamptz                                           NOT NULL DEFAULT NOW(),
    group_creation_ts          timestamptz                                           NOT NULL DEFAULT NOW(),
    group_owner_id             uuid                                                  NOT NULL UNIQUE,
    group_owner_id_ts          timestamptz                                           NOT NULL DEFAULT NOW(),
    group_name                 character varying(120)                                NOT NULL UNIQUE,
    group_name_ts              timestamptz                                           NOT NULL DEFAULT NOW(),
    group_notes                character varying(4096),
    group_notes_ts             character varying(4096)                               NOT NULL DEFAULT NOW(),

    -- account enable / disable
    group_locked               boolean                                               NOT NULL DEFAULT false,
    group_locked_ts            timestamptz                                           NOT NULL DEFAULT NOW(),
    group_lock_reason          character varying(2048),
    group_lock_reason_ts       character varying(2048),

    -- for multiple accounts
    max_users                  integer                                               NOT NULL DEFAULT 1,
    max_users_ts               timestamptz                                           NOT NULL DEFAULT NOW(),
    current_users              integer                                               NOT NULL DEFAULT 0,
    current_users_ts           timestamptz                                           NOT NULL DEFAULT NOW(),

    -- constraints
    CONSTRAINT pkey__group_id  PRIMARY KEY (group_id)
);

-- indices
CREATE INDEX IF NOT EXISTS group_id       ON users.groups(group_id);
CREATE INDEX IF NOT EXISTS group_owner_id ON users.groups(group_owner_id);

-- functions
-- group_id
CREATE OR REPLACE FUNCTION users.groups__group_id_ts() RETURNS TRIGGER AS $$
BEGIN
    NEW.group_id_ts = NOW();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- group_owner_id
CREATE OR REPLACE FUNCTION users.groups__owner_id_ts() RETURNS TRIGGER AS $$
BEGIN
    NEW.group_owner_id_ts = NOW();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- group_name
CREATE OR REPLACE FUNCTION users.groups__group_name_ts() RETURNS TRIGGER AS $$
BEGIN
    NEW.group_owner_name_ts = NOW();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- group_name
CREATE OR REPLACE FUNCTION users.groups__group_notes_ts() RETURNS TRIGGER AS $$
BEGIN
    NEW.group_notes_ts = NOW();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- group_locked
CREATE OR REPLACE FUNCTION users.groups__group_locked_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.group_locked_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

-- group_lock_reason
CREATE OR REPLACE FUNCTION users.groups__group_lock_reason_ts() RETURNS TRIGGER AS $$
BEGIN
    NEW.group_lock_reason_ts = NOW();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- max_users
CREATE OR REPLACE FUNCTION users.groups__max_users_ts() RETURNS TRIGGER AS $$
BEGIN
    NEW.max_users_ts = NOW();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- current_users
CREATE OR REPLACE FUNCTION users.groups__current_users_ts() RETURNS TRIGGER AS $$
BEGIN
    NEW.current_users_ts = NOW();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- triggers
DROP TRIGGER IF EXISTS update_group_locked ON users.groups;
CREATE TRIGGER         update_group_locked BEFORE UPDATE OF group_locked ON users.groups FOR EACH ROW EXECUTE PROCEDURE users.groups__group_locked_ts();
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE users.groups
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- CREATION OF TABLE users.accounts
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users.accounts
(
    group_id                        uuid,
    group_id_ts                     timestamptz                                             NOT NULL DEFAULT NOW(),
    -- account
    account_id                      uuid                                                    NOT NULL UNIQUE,
    account_id_ts                   timestamptz                                             NOT NULL DEFAULT NOW(),
    account_creation_ts             timestamptz                                             NOT NULL DEFAULT NOW(),
    account_locked                  boolean                                                 NOT NULL DEFAULT false,
    account_locked_ts               timestamptz,
    account_lock_reason             character varying(2048),

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
    password_hash_ts                timestamptz                                             NOT NULL DEFAULT NOW(),

    -- superuser
    superuser                       boolean                                                 NOT NULL DEFAULT false,
    superuser_ts                    timestamptz                                             NOT NULL DEFAULT NOW(),

    -- constraints
    CONSTRAINT pkey__id             PRIMARY KEY (account_id),
    CONSTRAINT fkey__group_id       FOREIGN KEY (group_id)  REFERENCES users.groups(group_id)
);
-- back reference group -> owner (back reference must be done with alter, because the two tables does not exist at creation time)
ALTER TABLE users.groups ADD CONSTRAINT fkey__owner_id FOREIGN KEY (group_owner_id) REFERENCES users.accounts(account_id);

-- indices --
CREATE INDEX IF NOT EXISTS account_id    ON users.accounts(account_id);
CREATE INDEX IF NOT EXISTS email_address ON users.accounts(email_address);
CREATE INDEX IF NOT EXISTS group_id      ON users.accounts(group_id);

-- functions
-- group_id_ts
CREATE OR REPLACE FUNCTION users.accounts__group_id_ts() RETURNS TRIGGER AS $$
BEGIN
    NEW.group_id_ts = NOW();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- account_locked_ts
CREATE OR REPLACE FUNCTION users.accounts__account_locked_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.account_locked_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

-- last_login_attempt_ip_ts
CREATE OR REPLACE FUNCTION users.accounts__last_login_attempt_ip_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.last_login_attempt_ip_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

-- last_successful_login_ip_ts
CREATE OR REPLACE FUNCTION users.accounts__last_successful_login_ip_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.last_successful_login_ip_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

-- email_address_ts
CREATE OR REPLACE FUNCTION users.accounts__email_address_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.email_address_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

-- email_address_verified_ts
CREATE OR REPLACE FUNCTION users.accounts__email_address_verified_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.email_address_verified_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

-- password_hash_ts
CREATE OR REPLACE FUNCTION users.accounts__password_hash_ts() RETURNS TRIGGER AS $$
BEGIN
    NEW.password_hash_ts = NOW();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- password_hash_ts
CREATE OR REPLACE FUNCTION users.accounts__superuser_ts() RETURNS TRIGGER AS $$
BEGIN
    NEW.superuser_ts = NOW();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- triggers
-- group_id
DROP TRIGGER IF EXISTS update_group_id ON users.accounts;
CREATE TRIGGER         update_group_id BEFORE UPDATE OF account_locked ON users.accounts FOR EACH ROW EXECUTE PROCEDURE users.accounts__group_id_ts();

-- account_locked
DROP TRIGGER IF EXISTS update_account_locked ON users.accounts;
CREATE TRIGGER         update_account_update BEFORE UPDATE OF account_locked ON users.accounts FOR EACH ROW EXECUTE PROCEDURE users.accounts__account_locked_ts();

-- last_login_attempt_ip
DROP TRIGGER IF EXISTS update_last_login_attempt_ip ON users.accounts;
CREATE TRIGGER         update_last_login_attempt_ip BEFORE UPDATE OF last_login_attempt_ip ON users.accounts FOR ROW EXECUTE PROCEDURE users.accounts__last_login_attempt_ip_ts();

-- last_successful_login_ip
DROP TRIGGER IF EXISTS update_last_successful_login_ip ON users.accounts;
CREATE TRIGGER         update_last_successful_login_ip BEFORE UPDATE OF last_successful_login_ip ON users.accounts FOR ROW EXECUTE PROCEDURE users.accounts__last_successful_login_ip_ts();

-- email_address
DROP TRIGGER IF EXISTS update_email_address ON users.accounts;
CREATE TRIGGER         update_email_address BEFORE UPDATE OF email_address ON users.accounts FOR ROW EXECUTE PROCEDURE users.accounts__email_address_ts();

-- email_address_verified
DROP TRIGGER IF EXISTS update_email_address_verified ON users.accounts;
CREATE TRIGGER         update_email_address_verified BEFORE UPDATE OF email_address_verified ON users.accounts FOR ROW EXECUTE PROCEDURE users.accounts__email_address_verified_ts();

-- password_hash
DROP TRIGGER IF EXISTS update_password_hash ON users.accounts;
CREATE TRIGGER         update_password_hash BEFORE UPDATE OF password_hash ON users.accounts FOR ROW EXECUTE PROCEDURE users.accounts__password_hash_ts();

-- superuser
DROP TRIGGER IF EXISTS update_superuser ON users.accounts;
CREATE TRIGGER         update_superuser BEFORE UPDATE OF password_hash ON users.accounts FOR ROW EXECUTE PROCEDURE users.accounts__superuser_ts();
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE users.accounts
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- CREATION OF TABLE users.group_permissions
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users.group_permissions
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
    CONSTRAINT fkey__account_id FOREIGN KEY (account_id) REFERENCES users.accounts(account_id)
);
-- indices --
CREATE INDEX IF NOT EXISTS account_id ON users.group_permissions(account_id);
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE users.group_permissions
----------------------------------------------------------------------------


----------------------------------------------------------------------------
-- CREATE ROLES/USERS
----------------------------------------------------------------------------
DROP ROLE IF EXISTS ${db_user};
CREATE USER ${db_user} WITH ENCRYPTED PASSWORD '${db_password}';
----------------------------------------------------------------------------
-- END OF CREATION OF ROLES/USERS
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- ____  _____ ____  __  __ ___ ____ ____ ___ ___  _   _ ____
-- |  _ \| ____|  _ \|  \/  |_ _/ ___/ ___|_ _/ _ \| \ | / ___|
-- | |_) |  _| | |_) | |\/| || |\___ \___ \| | | | |  \| \___ \
-- |  __/| |___|  _ <| |  | || | ___) |__) | | |_| | |\  |___) |
-- |_|   |_____|_| \_\_|  |_|___|____/____/___\___/|_| \_|____/
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- PERMISSIONS OF SCHEMA users
----------------------------------------------------------------------------
GRANT USAGE ON SCHEMA users TO ${db_user};
-- sequences, functions (usage is required to call nextval function)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA users TO ${db_user};
GRANT EXECUTE       ON ALL FUNCTIONS IN SCHEMA users TO ${db_user};
GRANT REFERENCES    ON ALL TABLES    IN SCHEMA users TO ${db_user};

-- TABLES
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE users. TO ${db_user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE users.accounts TO ${db_user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE users.group_permissions TO ${db_user};
----------------------------------------------------------------------------
-- END OF PERMISSIONS OF SCHEMA users
----------------------------------------------------------------------------
