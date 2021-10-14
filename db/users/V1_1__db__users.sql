----------------------------------------------------------------------------
-- SCHEMA system
----------------------------------------------------------------------------
-- CREATION OF TABLE system.system_roles
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS system.system_roles(
    id integer,
    name varchar(20) NOT NULL,
    -- constraints
    CONSTRAINT pkey_system_role_id PRIMARY KEY (id),
    CONSTRAINT unique_system_role_name UNIQUE (name)
);

INSERT INTO system.system_roles VALUES (0, 'NONE');
INSERT INTO system.system_roles VALUES (1, 'USER');
INSERT INTO system.system_roles VALUES (2, 'ADMIN');
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE system.system_roles
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS system.user_roles(
    id integer,
    name varchar(20) NOT NULL,
    -- constraints
    CONSTRAINT pkey_user_role_id PRIMARY KEY (id),
    CONSTRAINT unique_user_role_name UNIQUE (name)
);

INSERT INTO system.user_roles VALUES (0, 'NONE');
INSERT INTO system.user_roles VALUES (1, 'READ');
INSERT INTO system.user_roles VALUES (2, 'WRITE');
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE system.user_roles
----------------------------------------------------------------------------
-- CREATION OF TABLE users.accounts
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users.accounts(
    account_id uuid NOT NULL DEFAULT gen_random_uuid(),
    account_registration_ts timestamptz NOT NULL DEFAULT NOW(),
    -- account enable / disable
    account_locked boolean NOT NULL,
    account_locked_ts timestamptz NOT NULL DEFAULT NOW(),
    account_lock_reason character varying(2048),
    account_login_attempts integer NOT NULL DEFAULT 0,
    -- email address
    email_address character varying(250) COLLATE pg_catalog."default" NOT NULL,
    email_address_ts timestamptz NOT NULL DEFAULT NOW(),
    -- email address verification
    email_address_verified boolean NOT NULL DEFAULT false,
    email_address_verified_ts timestamptz,
    -- password change
    password_hash character varying(128) NOT NULL UNIQUE,
    password_hash_ts timestamptz NOT NULL DEFAULT NOW(),
    --
    system_role_id integer NOT NULL DEFAULT 1,
    user_role_id   integer NOT NULL DEFAULT 1,
    -- constraints
    CONSTRAINT pkey_account_id PRIMARY KEY (account_id),
    CONSTRAINT uniq_email_address UNIQUE (email_address),
    CONSTRAINT fg_key_system_role FOREIGN KEY(system_role_id) REFERENCES system.system_roles(id),
    CONSTRAINT fg_key_user_role FOREIGN KEY(user_role_id) REFERENCES system.user_roles(id)
);

-- indices
CREATE INDEX IF NOT EXISTS index_email_address ON users.accounts(email_address);
CREATE INDEX IF NOT EXISTS index_account_id ON users.accounts(account_id);

-- function  && trigger for account_enabled update
CREATE OR REPLACE FUNCTION users.update_account_enabled_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.account_locked_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_account_enabled ON users.accounts;
CREATE TRIGGER trigger_account_enabled
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

DROP TRIGGER IF EXISTS trigger_email_address ON users.accounts;
CREATE TRIGGER trigger_email_address
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

DROP TRIGGER IF EXISTS trigger_password_hash ON users.accounts;
CREATE TRIGGER trigger_password_hash BEFORE UPDATE OF password_hash ON users.accounts
FOR EACH ROW EXECUTE PROCEDURE users.update_password_hash_ts();
-- end of password_hash_last

-- function && trigger for email_address_verified update
CREATE OR REPLACE FUNCTION users.update_email_address_verified_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.email_address_verified_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_email_address_verified ON users.accounts;
CREATE TRIGGER trigger_email_address_verified
    BEFORE UPDATE OF email_address_verified ON users.accounts
    FOR EACH ROW
    EXECUTE PROCEDURE users.update_email_address_verified_ts();
-- end of email_address_verified
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE users.accounts
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- CREATE ROLES/USERS
----------------------------------------------------------------------------
DROP ROLE IF EXISTS ${db.user};
CREATE USER ${db.user} WITH ENCRYPTED PASSWORD '${db_password}';
----------------------------------------------------------------------------

----------------------------------------------------------------------------
-- PERMISSIONS OF SCHEMA users
----------------------------------------------------------------------------
GRANT USAGE ON SCHEMA users TO ${db.user};
-- sequences, functions (usage is required to call nextval function)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA users TO ${db.user};
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA users TO ${db.user};
GRANT REFERENCES ON ALL TABLES IN SCHEMA users TO ${db.user};

-- TABLES
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE users.accounts TO ${db.user};

----------------------------------------------------------------------------
-- PERMISSIONS OF SCHEMA system
----------------------------------------------------------------------------
GRANT SELECT ON TABLE system.system_roles TO ${db.user};
GRANT SELECT ON TABLE system.user_roles TO ${db.user};
