----------------------------------------------------------------------------
-- Table for accounts
----------------------------------------------------------------------------
CREATE sequence IF NOT EXISTS accounts_account_id_seq;
CREATE TABLE IF NOT EXISTS public.accounts(
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
CREATE INDEX IF NOT EXISTS index_email_address ON public.accounts(email_address);
CREATE INDEX IF NOT EXISTS index_account_id ON public.accounts(account_id);

-- trigger for account_enabled
CREATE OR REPLACE FUNCTION update_account_enabled_ts()
    RETURNS TRIGGER AS $$
    BEGIN
        RETURN NEW.account_enabled_ts = NOW();
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_account_enabled ON public.accounts;
CREATE TRIGGER trigger_account_enabled
    AFTER UPDATE OF account_enabled ON public.accounts
    FOR EACH ROW
    EXECUTE PROCEDURE update_account_enabled_ts();
-- end of account_enabled

-- trigger for email_address
CREATE OR REPLACE FUNCTION update_email_address_ts()
    RETURNS TRIGGER AS $$
    BEGIN
        RETURN NEW.email_address_ts = NOW();
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_email_address ON public.accounts;
CREATE TRIGGER trigger_email_address
    AFTER UPDATE OF email_address ON public.accounts
    FOR EACH ROW
    EXECUTE PROCEDURE update_email_address_ts();
-- end of email_address

-- trigger for password_hash
CREATE OR REPLACE FUNCTION update_password_hash_ts()
    RETURNS TRIGGER AS $$
    BEGIN
        RETURN NEW.password_hash_ts = NOW();
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_password_hash ON public.accounts;
CREATE TRIGGER trigger_password_hash
    AFTER UPDATE OF password_hash ON public.accounts
    FOR EACH ROW
    EXECUTE PROCEDURE update_password_hash_ts();
-- end of password_hash_last

-- trigger for email_address_verified
CREATE OR REPLACE FUNCTION update_email_address_verified_ts()
    RETURNS TRIGGER AS $$
    BEGIN
        RETURN NEW.email_address_verified_ts = NOW();
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_email_address_verified ON public.accounts;
CREATE TRIGGER trigger_email_address_verified
    AFTER UPDATE OF email_address_verified ON public.accounts
    FOR EACH ROW
    EXECUTE PROCEDURE update_email_address_verified_ts();
-- end of email_address_verified
----------------------------------------------------------------------------


----------------------------------------------------------------------------
-- Table for account features
----------------------------------------------------------------------------
-- table accounts_features
CREATE TABLE IF NOT EXISTS public.accounts_features(
    account_id integer NOT NULL,
    administrator boolean NOT NULL DEFAULT false,
    administrator_ts timestamptz NOT NULL DEFAULT NOW(),
    -- constraints
    CONSTRAINT fk_account_id FOREIGN KEY (account_id)
        REFERENCES public.accounts (account_id)
);

-- trigger for administrator
CREATE OR REPLACE FUNCTION update_administrator_ts()
    RETURNS TRIGGER AS $$
    BEGIN
        RETURN NEW.administrator_ts = NOW();
    END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_administrator ON public.accounts;
CREATE TRIGGER trigger_administrator
    AFTER UPDATE OF administrator ON public.accounts_features
    FOR EACH ROW
EXECUTE PROCEDURE update_administrator_ts();
----------------------------------------------------------------------------