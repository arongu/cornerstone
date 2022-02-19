----------------------------------------------------------------------------
-- SCHEMA users
----------------------------------------------------------------------------
-- CREATION OF TABLE users.accounts
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users.accounts
(   -- account type 'ADMIN / USER'
    account_type              character varying(5)                                  NOT NULL,
    account_id                uuid                                                  NOT NULL,
    account_owner_id          uuid                                                  NOT NULL,
    account_registration_ts   timestamptz                                           NOT NULL DEFAULT NOW(),
    account_description       character varying(250),
    -- account enable / disable
    account_locked            boolean                                               NOT NULL DEFAULT false,
    account_locked_ts         timestamptz                                           NOT NULL DEFAULT NOW(),
    account_lock_reason       character varying(2048)                               NULL,
    -- by default contact_email will be the owner of the account
    contact_email             character varying(300)                                NOT NULL,
    contact_phone             character varying(50),
    -- if enabled multiple users can be assigned to the account
    organization              boolean                                               NOT NULL DEFAULT  false,
    -- constraints
    CONSTRAINT accounts__account_id__pkey         PRIMARY KEY (account_id),
    CONSTRAINT accounts__account_type__check      CHECK       (account_type in ('ADMIN', 'USER')),
    -- 1 owner 1 account -- an email can only own 1 account
    CONSTRAINT accounts__account_owner_id__uniq   UNIQUE      (account_owner_id),
    CONSTRAINT accounts__contact_email__uniq      UNIQUE      (contact_email)
);

-- indices
CREATE INDEX IF NOT EXISTS accounts__account_id    ON users.accounts (account_id);
CREATE INDEX IF NOT EXISTS accounts__owner_id      ON users.accounts (account_owner_id);
CREATE INDEX IF NOT EXISTS accounts__contact_email ON users.accounts (contact_email);

-- function && trigger for account_enabled update
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
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE users.accounts
----------------------------------------------------------------------------
-- CREATION OF TABLE users.account_members
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users.account_members
(
    -- account where the this member belongs
    account_id         uuid                                           NOT NULL UNIQUE,
    member_id          uuid                                           NOT NULL,
    member_locked      boolean                                        NOT NULL DEFAULT false,
    member_locked_ts   timestamptz                                    NOT NULL DEFAULT NOW(),
    member_lock_reason character varying(2048)                        NULL,

    -- email address
    email_address              character varying(250)   COLLATE pg_catalog."default" NOT NULL,
    email_address_ts           timestamptz                                           NOT NULL DEFAULT NOW(),
    -- email address verification
    email_address_verified     boolean                                               NOT NULL DEFAULT false,
    email_address_verified_ts  timestamptz                                           NULL,

    -- password change
    password_hash              character varying(128)                                NOT NULL,
    password_hash_ts           timestamptz                                           NOT NULL DEFAULT NOW(),

    -- constraints
    CONSTRAINT account_members__account_member_id__pkey PRIMARY KEY (member_id),
    CONSTRAINT account_members__account_id__fg_key      FOREIGN KEY (account_id)     REFERENCES users.accounts(account_id),
    CONSTRAINT account_members__email_address__unique   UNIQUE      (email_address),
    CONSTRAINT account_members__password_hash__unique   UNIQUE      (password_hash)
);
-- indices --
CREATE INDEX IF NOT EXISTS account_members__account_id    ON users.account_members(account_id);
CREATE INDEX IF NOT EXISTS account_members__member_id     ON users.account_members(member_id);
CREATE INDEX IF NOT EXISTS account_members__email_address ON users.account_members(email_address);

-- once account_members table created, the back reference for owner can be created
ALTER TABLE users.accounts ADD CONSTRAINT accounts__owner_id__fgkey FOREIGN KEY (account_owner_id) REFERENCES users.account_members(member_id);
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE users.account_members
----------------------------------------------------------------------------
-- CREATION OF TABLE users.http_method_permissions
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users.account_member_permissions
(
    member_id         uuid      NOT NULL,
    -- http method permissions
    http_read         boolean, -- get, head, options
    http_write        boolean, -- patch, post, put
    http_delete       boolean, -- delete

    -- account level permissions
    account_owner     boolean,
    account_admin     boolean,

    -- constraints
    CONSTRAINT account_permissions__member_id__fg_key    FOREIGN KEY (member_id)   REFERENCES users.account_members(member_id)
);
-- indices --
CREATE INDEX IF NOT EXISTS account_member_permission__member_id    ON users.account_member_permissions(member_id);

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
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE users.accounts TO ${db_user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE users.account_members TO ${db_user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE users.account_member_permissions TO ${db_user};
----------------------------------------------------------------------------
-- END OF PERMISSIONS OF SCHEMA users
----------------------------------------------------------------------------
