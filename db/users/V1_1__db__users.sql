----------------------------------------------------------------------------
-- SCHEMA system
----------------------------------------------------------------------------
-- CREATION OF TABLE system.system_roles
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS system.roles(
    id   integer,
    name varchar(20) NOT NULL,
    -- constraints
    CONSTRAINT roles__role_id__pkey     PRIMARY KEY (id),
    CONSTRAINT roles__role_name__unique UNIQUE (name)
);

INSERT INTO system.roles VALUES (0, 'NO-ROLE');
INSERT INTO system.roles VALUES (1, 'USER');
INSERT INTO system.roles VALUES (2, 'ADMIN');
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE system.system_roles
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- CREATION OF TABLE users.accounts
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users.accounts(
    account_id                uuid                     NOT NULL DEFAULT gen_random_uuid(),
    account_registration_ts   timestamptz              NOT NULL DEFAULT NOW(),
    -- account enable / disable
    account_locked            boolean                  NOT NULL,
    account_locked_ts         timestamptz              NOT NULL DEFAULT NOW(),
    account_lock_reason       character varying(2048),
    account_login_attempts    integer                  NOT NULL DEFAULT 0,
    -- email address
    email_address             character varying(250)   COLLATE pg_catalog."default" NOT NULL,
    email_address_ts          timestamptz              NOT NULL DEFAULT NOW(),
    -- email address verification
    email_address_verified    boolean                  NOT NULL DEFAULT false,
    email_address_verified_ts timestamptz,
    -- password change
    password_hash             character varying(128)   NOT NULL,
    password_hash_ts          timestamptz              NOT NULL DEFAULT NOW(),
    -- user role in the system
    system_role_id            integer                  NOT NULL DEFAULT 1,
    -- constraints
    CONSTRAINT accounts__account_id__pkey       PRIMARY KEY (account_id),
    CONSTRAINT accounts__email_address__unique  UNIQUE      (email_address),
    CONSTRAINT accounts__password_hash__unique  UNIQUE      (password_hash),
    CONSTRAINT accounts__system_role_id__fg_key FOREIGN KEY (system_role_id) REFERENCES system.roles(id)
);

-- indices
CREATE INDEX IF NOT EXISTS account_id    ON users.accounts (account_id);
CREATE INDEX IF NOT EXISTS email_address ON users.accounts (email_address);

-- function  && trigger for account_enabled update
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
-- end of account_enabled

-- function && trigger for email_address update
CREATE OR REPLACE FUNCTION users.update_email_address_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.email_address_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP   TRIGGER IF EXISTS email_address ON users.accounts;
CREATE TRIGGER           email_address
    BEFORE UPDATE OF email_address ON users.accounts
    FOR EACH ROW EXECUTE PROCEDURE users.update_email_address_ts();
-- end of email_address

-- function && trigger for password_hash update
CREATE OR REPLACE FUNCTION users.update_password_hash_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.password_hash_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP   TRIGGER IF EXISTS password_hash ON users.accounts;
CREATE TRIGGER           password_hash BEFORE UPDATE OF password_hash ON users.accounts
FOR EACH ROW EXECUTE PROCEDURE users.update_password_hash_ts();
-- end of password_hash_last

-- function && trigger for email_address_verified update
CREATE OR REPLACE FUNCTION users.update_email_address_verified_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.email_address_verified_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP  TRIGGER IF EXISTS email_address_verified ON users.accounts;
CREATE TRIGGER          email_address_verified
    BEFORE UPDATE OF email_address_verified ON users.accounts
    FOR EACH ROW
    EXECUTE PROCEDURE users.update_email_address_verified_ts();
----------------------------------------------------------------------------
-- end of email_address_verified
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE users.accounts
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- CREATION OF TABLE users.account_http_method_permissions
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users.account_http_method_permissions(
    account_id uuid,
    delete  boolean NOT NULL DEFAULT TRUE,
    get     boolean NOT NULL DEFAULT TRUE,
    head    boolean NOT NULL DEFAULT TRUE,
    options boolean NOT NULL DEFAULT TRUE,
    patch   boolean NOT NULL DEFAULT TRUE,
    post    boolean NOT NULL DEFAULT TRUE,
    put     boolean NOT NULL DEFAULT TRUE,
    -- constraints
    CONSTRAINT account_http_method_permissions__account_id__pkey  PRIMARY KEY (account_id),
    CONSTRAINT account_http_method_permissions__account_id__fgkey FOREIGN KEY (account_id) REFERENCES users.accounts(account_id)
);
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE users.account_http_method_permissions
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- CREATE ROLES/USERS
----------------------------------------------------------------------------
DROP ROLE IF EXISTS ${db.user};
CREATE USER ${db.user} WITH ENCRYPTED PASSWORD '${db_password}';
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
-- PERMISSIONS OF SCHEMA system
----------------------------------------------------------------------------
GRANT SELECT ON TABLE system.roles TO ${db.user};
----------------------------------------------------------------------------
-- END OF PERMISSIONS OF SCHEMA system
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- PERMISSIONS OF SCHEMA users
----------------------------------------------------------------------------
GRANT USAGE ON SCHEMA users TO ${db.user};
-- sequences, functions (usage is required to call nextval function)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA users TO ${db.user};
GRANT EXECUTE       ON ALL FUNCTIONS IN SCHEMA users TO ${db.user};
GRANT REFERENCES    ON ALL TABLES    IN SCHEMA users TO ${db.user};

-- TABLES
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE users.accounts TO ${db.user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE users.account_http_method_permissions TO ${db.user};
----------------------------------------------------------------------------
-- END OF PERMISSIONS OF SCHEMA users
----------------------------------------------------------------------------
