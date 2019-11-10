CREATE sequence IF NOT EXISTS users_account_id_seq;
CREATE TABLE IF NOT EXISTS public.users
(
    account_id integer NOT NULL DEFAULT nextval('users_account_id_seq'::regclass),
    account_created timestamptz NOT NULL DEFAULT NOW(),
    account_enabled boolean NOT NULL,
    account_enabled_updated timestamptz NOT NULL DEFAULT NOW(),
    email_address character varying(250) COLLATE pg_catalog."default",
    email_address_updated timestamptz NOT NULL DEFAULT NOW(),
    email_address_verified boolean NOT NULL,
    password_hash character varying(128) NOT NULL UNIQUE,
    password_hash_updated timestamptz NOT NULL DEFAULT NOW(),
    CONSTRAINT users_pkey PRIMARY KEY (account_id),
    CONSTRAINT email_address UNIQUE (email_address)

);
-- CREATE INDEX IF NOT EXISTS index_email_address ON public.users(email_address);
-- CREATE INDEX IF NOT EXISTS index_account_id ON public.users(account_id);

-- NO NEED FOR Indexes PSQL creates automatically for columns with constraints, and primary keys
-- Verify with describe \d users


/*
ALTER sequence users_account_id_seq OWNED BY public.users.account_id;
TABLESPACE pg_default;
ALTER TABLE public.users OWNER to postgres;
 */