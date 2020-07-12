----------------------------------------------------------------------------
-- Schema secure
----------------------------------------------------------------------------
-- Table secure.pubkeys
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS secure.public_keys(
    uuid uuid,
    node_name character varying(64) COLLATE pg_catalog."default" NOT NULL,
    ttl integer NOT NULL,
    creation_ts timestamptz NOT NULL DEFAULT NOW(),
    expire_ts timestamptz,
    base64_key character varying(736) COLLATE pg_catalog."default" NOT NULL,
    -- constraints
    CONSTRAINT pkey_uuid PRIMARY KEY (uuid)
);

-- indices
CREATE INDEX IF NOT EXISTS index_uuid ON secure.public_keys(uuid);

-- trigger function to re-calculate expire_ts
CREATE OR REPLACE FUNCTION secure.calculate_expire_ts() RETURNS TRIGGER AS $$
BEGIN
    --NEW.expire_ts = (SELECT(NOW() + interval ttl second));
    NEW.expire_ts = NOW();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- trigger on ttl
DROP TRIGGER IF EXISTS trigger_ttl ON secure.public_keys;
CREATE TRIGGER trigger_ttl
    BEFORE UPDATE OF ttl ON secure.public_keys
    FOR EACH ROW
EXECUTE PROCEDURE secure.calculate_expire_ts();

-- trigger on creation_ts
DROP TRIGGER IF EXISTS trigger_creation_ts ON secure.public_keys;
CREATE TRIGGER trigger_creation_ts
    BEFORE UPDATE OF creation_ts ON secure.public_keys
    FOR EACH ROW
    EXECUTE PROCEDURE secure.calculate_expire_ts();

----------------------------------------------------------------------------
-- Permissions of schema secure
----------------------------------------------------------------------------
DROP ROLE IF EXISTS ${db.user};
CREATE USER ${db.user} WITH ENCRYPTED PASSWORD '${db_password}';
GRANT USAGE ON SCHEMA ${schema_secure} TO ${db_user};
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schema_secure} TO ${db_user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON secure.public_keys TO ${db_user};
