-- STEP 0 :: !! ONLY FOR TEST DB :: terminate all connections and drop test database
SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE datname='test_data';
DROP DATABASE IF EXISTS test_data;
--
--
-- STEP 1 :: create database and its schemas
CREATE DATABASE test_data;
\connect test_data;
CREATE SCHEMA secure;
--
--
-- STEP 2 :: drop schema public
DROP SCHEMA public;
--
