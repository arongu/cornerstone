----------------------------------------------------------------------------
-- SCHEMA users
----------------------------------------------------------------------------
-- CREATION OF TABLE users.groups
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users.groups
(
    group_id                   uuid                                                  NOT NULL,
    group_owner_id             uuid                                                  NOT NULL,
    group_name                 character varying(120),
    group_notes                character varying(4096),
    registration_ts            timestamptz                                           NOT NULL DEFAULT NOW(),
    -- account enable / disable
    group_locked               boolean                                               NOT NULL DEFAULT false,
    locked_ts                  timestamptz                                           NOT NULL DEFAULT NOW(),
    lock_reason                character varying(2048)                               NULL,
    -- for multiple accounts
    max_users                  integer                                               NOT NULL DEFAULT 1,
    current_users              integer                                               NOT NULL DEFAULT 0,
    -- constraints
    CONSTRAINT pkey__group_id  PRIMARY KEY (group_id),
    CONSTRAINT uniq__user_id   UNIQUE      (group_owner_id)
);

-- indices
CREATE INDEX IF NOT EXISTS group_id       ON users.groups(group_id);
CREATE INDEX IF NOT EXISTS group_owner_id ON users.groups(group_owner_id);

-- function && trigger for account_enabled update
CREATE OR REPLACE FUNCTION users.group_locked_ts() RETURNS TRIGGER AS $$
    BEGIN
        NEW.locked_ts = NOW();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

DROP   TRIGGER IF EXISTS group_locked_ts ON users.groups;
CREATE TRIGGER group_locked_ts
    BEFORE UPDATE OF group_locked ON users.groups
    FOR EACH ROW
    EXECUTE PROCEDURE users.group_locked_ts();
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE users.groups
----------------------------------------------------------------------------
-- CREATION OF TABLE users.accounts
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users.accounts
(
    -- account where the this member belongs
    group_id                        uuid                                           NOT NULL UNIQUE,
    id                              uuid                                           NOT NULL,
    locked                          boolean                                        NOT NULL DEFAULT false,
    locked_ts                       timestamptz                                    NOT NULL DEFAULT NOW(),
    lock_reason                     character varying(2048)                        NULL,

    login_attempts                  integer                                        NOT NULL,
    last_login_attempt_ip           character varying (32),
    last_login_attempt_ts           timestamptz,
    last_successful_login_ip        character varying(32),
    last_successful_login_ip_ts     timestamptz,


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
    CONSTRAINT pkey__id                PRIMARY KEY (id),
    CONSTRAINT fkey__group_id          FOREIGN KEY (group_id)       REFERENCES users.groups(group_id),
    CONSTRAINT uniq__email_address     UNIQUE      (email_address),
    CONSTRAINT uniq__password_hash     UNIQUE      (password_hash)
);
-- indices --
CREATE INDEX IF NOT EXISTS account_id    ON users.accounts(group_id);
CREATE INDEX IF NOT EXISTS member_id     ON users.accounts(id);
CREATE INDEX IF NOT EXISTS email_address ON users.accounts(email_address);

-- back reference group -> owner (back reference must be done with alter, because the two tables does not exist at creation time)
ALTER TABLE users.groups ADD CONSTRAINT fkey__owner_id FOREIGN KEY (group_owner_id) REFERENCES users.accounts(id);
----------------------------------------------------------------------------
-- END OF CREATION OF TABLE users.accounts
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
    CONSTRAINT fkey__member_id FOREIGN KEY (member_id) REFERENCES users.accounts(id)
);
-- indices --
CREATE INDEX IF NOT EXISTS member_id ON users.account_member_permissions(member_id);

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
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE users. TO ${db_user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE users.accounts TO ${db_user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER, REFERENCES ON TABLE users.account_member_permissions TO ${db_user};
----------------------------------------------------------------------------
-- END OF PERMISSIONS OF SCHEMA users
----------------------------------------------------------------------------
