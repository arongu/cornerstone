----------------------------------------------------------------------------
-- Table for accounts
----------------------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS account_id_seq;
CREATE TABLE IF NOT EXISTS info.accounts(
    account_id integer NOT NULL DEFAULT nextval('account_id_seq'::regclass),
    account_registration_ts timestamptz NOT NULL DEFAULT NOW(),
    -- account enable / disable
    account_available boolean NOT NULL,
    account_available_ts timestamptz NOT NULL DEFAULT NOW(),
    account_disable_reason character varying(128),
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
CREATE INDEX IF NOT EXISTS index_email_address ON info.accounts(email_address);
CREATE INDEX IF NOT EXISTS index_account_id ON info.accounts(account_id);

-- function  && trigger for account_enabled update
CREATE OR REPLACE FUNCTION update_account_enabled_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.account_available_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_account_enabled ON info.accounts;
CREATE TRIGGER trigger_account_enabled
    BEFORE UPDATE OF account_available ON info.accounts
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

DROP TRIGGER IF EXISTS trigger_email_address ON info.accounts;
CREATE TRIGGER trigger_email_address
    BEFORE UPDATE OF email_address ON info.accounts
    FOR EACH ROW EXECUTE PROCEDURE update_email_address_ts();
-- end of email_address

-- function && trigger for password_hash update
CREATE OR REPLACE FUNCTION update_password_hash_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.password_hash_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_password_hash ON info.accounts;
CREATE TRIGGER trigger_password_hash BEFORE UPDATE OF password_hash ON info.accounts
FOR EACH ROW EXECUTE PROCEDURE update_password_hash_ts();
-- end of password_hash_last

-- function && trigger for email_address_verified update
CREATE OR REPLACE FUNCTION update_email_address_verified_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.email_address_verified_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_email_address_verified ON info.accounts;
CREATE TRIGGER trigger_email_address_verified
    BEFORE UPDATE OF email_address_verified ON info.accounts
    FOR EACH ROW
    EXECUTE PROCEDURE update_email_address_verified_ts();
-- end of email_address_verified
----------------------------------------------------------------------------


----------------------------------------------------------------------------
-- Table for account features
----------------------------------------------------------------------------
-- table accounts_features
CREATE TABLE IF NOT EXISTS info.accounts_features(
    account_id integer NOT NULL,
    -- constraints
    CONSTRAINT fk_account_id FOREIGN KEY (account_id)
        REFERENCES info.accounts (account_id)
);
----------------------------------------------------------------------------


----------------------------------------------------------------------------
-- POST CONFIG : grant access to 'robot' after the database structure created
----------------------------------------------------------------------------

-- ACCOUNTS_SCHEMA -----------------------------------------------------------------------------
GRANT USAGE ON SCHEMA info TO robot;
-- sequences, function grants (usage is required to call nextval function)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA info TO robot;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA info TO robot;
-- Grant SELECT, INSERT, UPDATE, DELETE, TRIGGER on all tables in accounts_schema
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON ALL TABLES IN SCHEMA info TO robot;
-- END OF ACCOUNTS_SCHEMA ----------------------------------------------------------------------

-- ACCOUNTS_HISTORY_SCHEMA ---------------------------------------------------------------------
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA history TO robot;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA history TO robot;
GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER ON ALL TABLES IN SCHEMA history TO robot;
GRANT CREATE ON SCHEMA history TO robot;
