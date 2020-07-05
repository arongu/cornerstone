----------------------------------------------------------------------------
-- Schema user_data
----------------------------------------------------------------------------
-- Table user_data.accounts
----------------------------------------------------------------------------

CREATE SEQUENCE IF NOT EXISTS user_data.account_id_seq;
CREATE TABLE IF NOT EXISTS user_data.accounts(
    account_id integer NOT NULL DEFAULT nextval('account_id_seq'::regclass),
    account_registration_ts timestamptz NOT NULL DEFAULT NOW(),
    -- account enable / disable
    account_locked boolean NOT NULL,
    account_locked_ts timestamptz NOT NULL DEFAULT NOW(),
    account_lock_reason character varying(128),
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
    -- constraints
    CONSTRAINT pkey_account_id PRIMARY KEY (account_id),
    CONSTRAINT uniq_email_address UNIQUE (email_address)
);

-- indices
CREATE INDEX IF NOT EXISTS index_email_address ON user_data.accounts(email_address);
CREATE INDEX IF NOT EXISTS index_account_id ON user_data.accounts(account_id);

-- function  && trigger for account_enabled update
CREATE OR REPLACE FUNCTION user_data.update_account_enabled_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.account_locked_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_account_enabled ON user_data.accounts;
CREATE TRIGGER trigger_account_enabled
    BEFORE UPDATE OF account_locked ON user_data.accounts
    FOR EACH ROW
    EXECUTE PROCEDURE user_data.update_account_enabled_ts();
-- end of account_enabled

-- function && trigger for email_address update
CREATE OR REPLACE FUNCTION user_data.update_email_address_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.email_address_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_email_address ON user_data.accounts;
CREATE TRIGGER trigger_email_address
    BEFORE UPDATE OF email_address ON user_data.accounts
    FOR EACH ROW EXECUTE PROCEDURE user_data.update_email_address_ts();
-- end of email_address

-- function && trigger for password_hash update
CREATE OR REPLACE FUNCTION user_data.update_password_hash_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.password_hash_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_password_hash ON user_data.accounts;
CREATE TRIGGER trigger_password_hash BEFORE UPDATE OF password_hash ON user_data.accounts
FOR EACH ROW EXECUTE PROCEDURE user_data.update_password_hash_ts();
-- end of password_hash_last

-- function && trigger for email_address_verified update
CREATE OR REPLACE FUNCTION user_data.update_email_address_verified_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.email_address_verified_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_email_address_verified ON user_data.accounts;
CREATE TRIGGER trigger_email_address_verified
    BEFORE UPDATE OF email_address_verified ON user_data.accounts
    FOR EACH ROW
    EXECUTE PROCEDURE user_data.update_email_address_verified_ts();
-- end of email_address_verified

----------------------------------------------------------------------------
-- End of table user_data.accounts
----------------------------------------------------------------------------
-- Permissions of schema user_data
----------------------------------------------------------------------------

-- create roles/users
DROP ROLE IF EXISTS ${db.user};
CREATE USER ${db.user} WITH ENCRYPTED PASSWORD '${db_password}';

GRANT USAGE ON SCHEMA ${schema_user_data} TO ${db.user};
-- sequences, functions (usage is required to call nextval function)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA ${schema_user_data} TO ${db.user};
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schema_user_data} TO ${db.user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON ALL TABLES IN SCHEMA ${schema_user_data} TO ${db.user};
--
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA ${schema_user_data} TO ${db.user};
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schema_user_data} TO ${db.user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER ON ALL TABLES IN SCHEMA ${schema_user_data} TO ${db.user};
GRANT CREATE ON SCHEMA ${schema_user_data} TO ${db.user};

-- CLI TEST
-- CMD: psql --host=localhost --dbname=dev_users --username=dev
-- SET search_path TO user_data;
-- SELECT * FROM accounts;
----------------------------------------------------------------------------
-- End of permissions schema user_data
----------------------------------------------------------------------------
-- End of schema user_data
----------------------------------------------------------------------------

