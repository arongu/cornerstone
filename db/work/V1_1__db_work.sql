----------------------------------------------------------------------------
-- schema security
----------------------------------------------------------------------------
-- table security.pubkeys
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS security.public_keys(
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
CREATE INDEX IF NOT EXISTS index_uuid ON security.public_keys(uuid);

----------------------------------------------------------------------------
-- trigger functions
----------------------------------------------------------------------------
-- trigger function to re-calculate timestamps
CREATE OR REPLACE FUNCTION security.calculate_time_stamps() RETURNS TRIGGER AS $$
BEGIN
    NEW.creation_ts = NOW();
    NEW.expire_ts = NEW.creation_ts + (NEW.ttl * interval '1' second);
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- trigger function to re-calculate expire_ts
CREATE OR REPLACE FUNCTION security.recalculate_expire_ts() RETURNS TRIGGER AS $$
BEGIN
    NEW.expire_ts = OLD.creation_ts + (NEW.ttl * interval '1' second);
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

----------------------------------------------------------------------------
-- triggers
----------------------------------------------------------------------------
-- trigger insert
DROP TRIGGER IF EXISTS trigger_insert_table_public_keys ON security.public_keys;
CREATE TRIGGER trigger_insert_table_public_keys
    BEFORE INSERT ON security.public_keys
    FOR EACH ROW
    EXECUTE PROCEDURE security.calculate_time_stamps();

-- trigger update ttl
DROP TRIGGER IF EXISTS trigger_update_ttl ON security.public_keys;
CREATE TRIGGER trigger_update_ttl
    BEFORE UPDATE OF ttl ON security.public_keys
    FOR EACH ROW
    EXECUTE PROCEDURE security.recalculate_expire_ts();

-- trigger update base64_key
DROP TRIGGER IF EXISTS trigger_update_base64_key ON security.public_keys;
CREATE TRIGGER trigger_update_base64_key
    BEFORE UPDATE OF base64_key ON security.public_keys
    FOR EACH ROW
    EXECUTE PROCEDURE security.calculate_time_stamps();

----------------------------------------------------------------------------
-- permissions of schema security
----------------------------------------------------------------------------
DROP ROLE IF EXISTS ${db.user};
CREATE USER ${db_user} WITH ENCRYPTED PASSWORD '${db_password}';
GRANT USAGE ON SCHEMA security TO ${db_user};
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA security TO ${db_user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON security.public_keys TO ${db_user};
