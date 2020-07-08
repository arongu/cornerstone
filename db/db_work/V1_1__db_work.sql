----------------------------------------------------------------------------
-- Schema secure
----------------------------------------------------------------------------
-- Table secure.pubkeys
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS secure.pubkeys(
    uuid uuid,
    ts timestamptz NOT NULL DEFAULT NOW(),
    key_string character varying(1500) COLLATE pg_catalog."default" NOT NULL,
    -- constraints
    CONSTRAINT pkey_uuid PRIMARY KEY (uuid),
    CONSTRAINT uniq_key_string UNIQUE (key_string)
);

-- indices
CREATE INDEX IF NOT EXISTS index_uuid ON secure.pubkeys(uuid);

-- function && trigger
CREATE OR REPLACE FUNCTION secure.update_ts() RETURNS TRIGGER AS $$
BEGIN
    NEW.ts = NOW();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_uuid ON secure.pubkeys;
CREATE TRIGGER trigger_uuid
    BEFORE UPDATE OF uuid ON secure.pubkeys
    FOR EACH ROW
EXECUTE PROCEDURE secure.update_ts();

----------------------------------------------------------------------------
-- Permissions of schema secure
----------------------------------------------------------------------------
DROP ROLE IF EXISTS ${db.user};
CREATE USER ${db.user} WITH ENCRYPTED PASSWORD '${db_password}';

\connect ${db_name}
GRANT USAGE ON SCHEMA ${schema_secure} TO ${db_user};
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schema_secure} TO ${db_user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON secure.pubkyes TO ${db_user};

