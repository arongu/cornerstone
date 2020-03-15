-- STEP 3 :: drop and create db_user
DROP ROLE test_robot;
CREATE USER test_robot WITH ENCRYPTED PASSWORD 'test';
--
-- connect to db
--
\connect test_accounts
--
-- STEP 4 :: info schema
GRANT USAGE ON SCHEMA info TO test_robot;
--
--
-- STEP 5 :: info schema sequences, functions (usage is required to call nextval function)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA info TO test_robot;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA info TO test_robot;
--
--
-- STEP 6 :: info schema all tables
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON ALL TABLES IN SCHEMA info TO test_robot;
--
--
-- STEP 7 :: history schema
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA history TO test_robot;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA history TO test_robot;
GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER ON ALL TABLES IN SCHEMA history TO test_robot;
GRANT CREATE ON SCHEMA history TO test_robot;
