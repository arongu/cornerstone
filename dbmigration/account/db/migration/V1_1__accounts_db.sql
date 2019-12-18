----------------------------------------------------------------------------
-- Table for accounts
----------------------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS accounts_account_id_seq;
CREATE TABLE IF NOT EXISTS accounts_schema.accounts(
    account_id integer NOT NULL DEFAULT nextval('accounts_account_id_seq'::regclass),
    account_registration_ts timestamptz NOT NULL DEFAULT NOW(),
    -- account enable / disable
    account_enabled boolean NOT NULL,
    account_enabled_ts timestamptz NOT NULL DEFAULT NOW(),
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
CREATE INDEX IF NOT EXISTS index_email_address ON accounts_schema.accounts(email_address);
CREATE INDEX IF NOT EXISTS index_account_id ON accounts_schema.accounts(account_id);

-- function  && trigger for account_enabled update
CREATE OR REPLACE FUNCTION update_account_enabled_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.account_enabled_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_account_enabled ON accounts_schema.accounts;
CREATE TRIGGER trigger_account_enabled
    BEFORE UPDATE OF account_enabled ON accounts_schema.accounts
    FOR EACH ROW
    EXECUTE PROCEDURE update_account_enabled_ts();
-- end of account_enabled

-- function && trigger for email_address update
CREATE OR REPLACE FUNCTION update_email_address_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.email_address_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_email_address ON accounts_schema.accounts;
CREATE TRIGGER trigger_email_address
    BEFORE UPDATE OF email_address ON accounts_schema.accounts
    FOR EACH ROW EXECUTE PROCEDURE update_email_address_ts();
-- end of email_address

-- function && trigger for password_hash update
CREATE OR REPLACE FUNCTION update_password_hash_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.password_hash_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_password_hash ON accounts_schema.accounts;
CREATE TRIGGER trigger_password_hash BEFORE UPDATE OF password_hash ON accounts_schema.accounts
FOR EACH ROW EXECUTE PROCEDURE update_password_hash_ts();
-- end of password_hash_last

-- function && trigger for email_address_verified update
CREATE OR REPLACE FUNCTION update_email_address_verified_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.email_address_verified_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_email_address_verified ON accounts_schema.accounts;
CREATE TRIGGER trigger_email_address_verified
    BEFORE UPDATE OF email_address_verified ON accounts_schema.accounts
    FOR EACH ROW
    EXECUTE PROCEDURE update_email_address_verified_ts();
-- end of email_address_verified
----------------------------------------------------------------------------


----------------------------------------------------------------------------
-- Table for account features
----------------------------------------------------------------------------
-- table accounts_features
CREATE TABLE IF NOT EXISTS accounts_schema.accounts_features(
    account_id integer NOT NULL,
    administrator boolean NOT NULL DEFAULT false,
    administrator_ts timestamptz NOT NULL DEFAULT NOW(),
    -- constraints
    CONSTRAINT fk_account_id FOREIGN KEY (account_id)
        REFERENCES accounts_schema.accounts (account_id)
);

-- function && trigger for administrator update
CREATE OR REPLACE FUNCTION update_administrator_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.administrator_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_administrator ON accounts_schema.accounts;
CREATE TRIGGER trigger_administrator
    BEFORE UPDATE OF administrator ON accounts_schema.accounts_features
    FOR EACH ROW
    EXECUTE PROCEDURE update_administrator_ts();
----------------------------------------------------------------------------


----------------------------------------------------------------------------
-- POST CONFIG : grant access to 'robot' after the database structure created
----------------------------------------------------------------------------
GRANT USAGE ON SCHEMA accounts_schema TO robot;
-- sequences, function grants
-- usage is required to call nextval function
GRANT USAGE, SELECT ON SEQUENCE accounts_account_id_seq TO robot;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA accounts_schema TO robot;
-- tables
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON accounts_schema.accounts TO robot;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON accounts_schema.accounts_features TO robot;

-- TODO create trigger when new account is created history table should be generated
-- TODO account enabled reason
-- TODO password reset email
-- TODO email chang email with password confirmation
-- TODO no admin users!!!! that is not a feature
-- TODO tokens with settable rights as many as they want
-- TODO the root user password comes from the config !!!!

