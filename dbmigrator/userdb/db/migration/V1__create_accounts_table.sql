CREATE sequence IF NOT EXISTS users_account_id_seq;
CREATE TABLE IF NOT EXISTS public.users
(
    account_id integer NOT NULL DEFAULT nextval('users_account_id_seq'::regclass),
    account_created timestamptz NOT NULL DEFAULT NOW(),
    -- account enable / disable
    account_enabled boolean NOT NULL,
    account_enabled_ts timestamptz NOT NULL DEFAULT NOW(),
    -- email address
    email_address character varying(250) COLLATE pg_catalog."default",
    email_address_ts timestamptz NOT NULL DEFAULT NOW(),
    -- email address verification
    email_address_verified boolean NOT NULL,
    email_address_verified_ts timestamptz NOT NULL,
    -- password change
    password_hash character varying(128) NOT NULL UNIQUE,
    password_hash_ts timestamptz NOT NULL DEFAULT NOW(),
    -- constraints
    CONSTRAINT users_pkey PRIMARY KEY (account_id),
    CONSTRAINT email_address UNIQUE (email_address)
);

-- trigger for account_enabled
CREATE FUNCTION update_account_enabled_last_modified()
    RETURNS TRIGGER AS $$
    BEGIN
        RETURN NEW.account_enabled_ts = NOW();
    END
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_account_enabled
    AFTER UPDATE OF account_enabled ON public.users
    FOR EACH ROW
    EXECUTE PROCEDURE update_account_enabled_last_modified();
-- end of account_enabled

-- trigger for email_address
CREATE FUNCTION update_email_address_last_modified()
    RETURNS TRIGGER AS $$
    BEGIN
        RETURN NEW.email_address_ts = NOW();
    END
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_email_address
    AFTER UPDATE OF email_address ON public.users
    FOR EACH ROW
    EXECUTE PROCEDURE update_email_address_last_modified();
-- end of email_address

-- trigger for password_hash
CREATE FUNCTION update_password_hash_last_modified()
    RETURNS TRIGGER AS $$
    BEGIN
        RETURN NEW.password_hash_ts = NOW();
    END
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_password_hash
    AFTER UPDATE OF password_hash ON public.users
    FOR EACH ROW
    EXECUTE PROCEDURE update_password_hash_last_modified();
-- end of password_hash_last

-- trigger for email_address_verified
CREATE FUNCTION update_email_address_verified_date()
    RETURNS TRIGGER AS $$
    BEGIN
        RETURN NEW.email_address_verified_ts = NOW();
    END
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_email_address_verified
    AFTER UPDATE OF email_address_verified ON public.users
    FOR EACH ROW
    EXECUTE PROCEDURE update_email_address_verified_date();
-- end of email_address_verified
