----------------------------------------------------------------------------
-- SCHEMA system
----------------------------------------------------------------------------
-- CREATION OF TABLE system.system_roles
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS system.system_roles
(
    system_role_id   integer,
    system_role_name varchar(20) NOT NULL,
    -- constraints
    CONSTRAINT system_roles__system_role_id__pkey     PRIMARY KEY (system_role_id),
    CONSTRAINT system_roles__system_role_name__unique UNIQUE      (system_role_name)
);

INSERT INTO system.system_roles VALUES (0, 'NONE');
INSERT INTO system.system_roles VALUES (1, 'USER');
INSERT INTO system.system_roles VALUES (2, 'ADMIN');
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE system.system_roles
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- CREATION OF TABLE system.multi_account_roles
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS system.multi_account_roles
(
    multi_account_role_id   integer,
    multi_account_role_name varchar(20) NOT NULL,
    -- constraints
    CONSTRAINT multi_account_roles__multi_account_role_id__pkey     PRIMARY KEY (multi_account_role_id),
    CONSTRAINT multi_account_roles__multi_account_role_name__unique UNIQUE      (multi_account_role_name)
);

INSERT INTO system.multi_account_roles VALUES (0, 'NOT_APPLICABLE');
INSERT INTO system.multi_account_roles VALUES (1, 'MULTI_ACCOUNT_USER');
INSERT INTO system.multi_account_roles VALUES (2, 'MULTI_ACCOUNT_ADMIN');
INSERT INTO system.multi_account_roles VALUES (3, 'MULTI_ACCOUNT_OWNER');

----------------------------------------------------------------------------
-- CREATION OF TABLE system.account_types
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS system.account_types
(
    account_type_id   integer,
    account_type_name varchar(20) NOT NULL,
    -- constraints
    CONSTRAINT account_types__account_type_id__pkey      PRIMARY KEY (account_type_id),
    CONSTRAINT account_types__account_type_name__unique  UNIQUE      (account_type_name)
);

INSERT INTO system.account_types VALUES (0, 'SUB_ACCOUNT');
INSERT INTO system.account_types VALUES (1, 'SINGLE_ACCOUNT');
INSERT INTO system.account_types VALUES (2, 'MULTI_ACCOUNT');
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE users.account_types
----------------------------------------------------------------------------


----------------------------------------------------------------------------
-- SCHEMA users
----------------------------------------------------------------------------
-- CREATION OF TABLE users.accounts
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users.accounts
(
    system_role_id            integer                                               NOT NULL,
    -- account
    account_id                uuid                                                  NOT NULL,
    account_registration_ts   timestamptz                                           NOT NULL DEFAULT NOW(),
    account_type_id           integer                                               NOT NULL,
    account_type_id_ts        timestamptz                                           NOT NULL DEFAULT NOW(),

    -- account enable / disable
    account_locked            boolean                                               NOT NULL DEFAULT false,
    account_locked_ts         timestamptz                                           NOT NULL DEFAULT NOW(),
    account_lock_reason       character varying(2048)                               NULL,
    account_login_attempts    integer                                               NOT NULL DEFAULT 0,

    -- email address
    email_address             character varying(250)   COLLATE pg_catalog."default" NOT NULL,
    email_address_ts          timestamptz                                           NOT NULL DEFAULT NOW(),
    -- email address verification
    email_address_verified    boolean                                               NOT NULL DEFAULT false,
    email_address_verified_ts timestamptz                                           NULL,

    -- password change
    password_hash             character varying(128)                                NOT NULL,
    password_hash_ts          timestamptz                                           NOT NULL DEFAULT NOW(),
    -- references another uuid where account_type is set to 'multi'
    multi_account_role_id     int                                                   NULL,
    parent_account_id         uuid                                                  NULL,

    -- constraints
    CONSTRAINT accounts__account_id__pkey       PRIMARY KEY (account_id),
    CONSTRAINT accounts__email_address__unique  UNIQUE      (email_address),
    CONSTRAINT accounts__password_hash__unique  UNIQUE      (password_hash),
    -- system role
    CONSTRAINT accounts__system_role_id__fg_key  FOREIGN KEY (system_role_id)   REFERENCES system.system_roles(system_role_id),
    CONSTRAINT accounts__account_type_id__fg_key FOREIGN KEY (account_type_id)  REFERENCES system.account_types(account_type_id),
    -- multi account
    CONSTRAINT accounts__parent_account__fg_key  FOREIGN KEY (parent_account_id)   REFERENCES users.accounts(account_id)
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
-- END OF CREATION OF TABLE users.accounts
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- CREATION OF TABLE users.account_http_method_permissions
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users.account_http_method_permissions
(
    account_id uuid,
    delete  boolean NOT NULL,
    get     boolean NOT NULL,
    head    boolean NOT NULL,
    options boolean NOT NULL,
    patch   boolean NOT NULL,
    post    boolean NOT NULL,
    put     boolean NOT NULL,
    -- constraints
    CONSTRAINT account_http_method_permissions__account_id__pkey   PRIMARY KEY (account_id),
    CONSTRAINT account_http_method_permissions__account_id__fg_key FOREIGN KEY (account_id) REFERENCES users.accounts(account_id)
);
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE users.account_http_method_permissions
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
-- PERMISSIONS OF SCHEMA system
----------------------------------------------------------------------------
GRANT SELECT ON TABLE system.system_roles TO ${db_user};
GRANT SELECT ON TABLE system.account_types TO ${db_user};
----------------------------------------------------------------------------
-- END OF PERMISSIONS OF SCHEMA system
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
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE users.accounts TO ${db_user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE users.account_http_method_permissions TO ${db_user};
----------------------------------------------------------------------------
-- END OF PERMISSIONS OF SCHEMA users
----------------------------------------------------------------------------
