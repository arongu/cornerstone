----------------------------------------------------------------------------
-- Schema user_data
----------------------------------------------------------------------------
-- Table user_data.roles
----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_data.roles(
    role_id integer PRIMARY KEY,
    role_name varchar(20),
    -- constraints
    CONSTRAINT uniq_role_name UNIQUE (role_name)
);

----------------------------------------------------------------------------
-- CREATE ROLES
----------------------------------------------------------------------------
INSERT INTO user_data.roles VALUES (0, 'NO_ROLE');
INSERT INTO user_data.roles VALUES (1, 'USER');
INSERT INTO user_data.roles VALUES (5, 'SUPER');
INSERT INTO user_data.roles VALUES (7, 'ADMIN');

----------------------------------------------------------------------------
-- ADD NEW COLUMN TO ACCOUNT AND REFERENCE user_data
----------------------------------------------------------------------------
ALTER TABLE user_data.accounts ADD COLUMN role_id smallint NOT NULL DEFAULT 0;
ALTER TABLE user_data.accounts ADD CONSTRAINT fg_key_role_id FOREIGN KEY (role_id) REFERENCES user_data.roles (role_id) MATCH FULL;
