-- STEP 3 :: drop and create db_user
DROP ROLE test_robot;
CREATE USER test_robot WITH ENCRYPTED PASSWORD 'test';
--
-- connect to db
--
\connect test_data
--
-- STEP 4 :: info secure
GRANT USAGE ON SCHEMA secure TO test_robot;
--
--
-- STEP 5 :: grants onf secure schema functions
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA secure TO test_robot;
--
--
-- STEP 6 :: secure schema all tables
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON secure.pubkyes TO test_robot;
