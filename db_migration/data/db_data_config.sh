#!/bin/bash
# Script to generate the basis of "data" database.
# It creates two sql files.
# 01 run preconfig SQL with psql -f as postgres user e.g.: psql -f preconfig.sql
# 02 execute database migration task in accounts: mvn flyway:migrate -P<profile> (test/prod)
# 03 run postconfig SQL with psql -f as postgres user e.g.: psql -f postconfig.sql
#
# preconfig.sql     - creates prequisites (account db, schemas)
# maven flyway      - deploys the actual database structure
# postconfig.sql    - sets the right permissions on the database for the appserver's db user

declare db_name=
declare db_user=
declare db_password=

# ----------------------------------------
# Schemas
declare schema_secure='secure'

# Prod
db_prod_name='data'
db_prod_user='robot'

# Test
db_test_name='test_data'
db_test_user='test_robot'
db_test_password='test'
# ----------------------------------------

function setup_prod() {
  local password="${1}"
  shift

  db_name="${db_prod_name}"
  db_user="${db_prod_user}"

  if [ -z "${password}" ]; then
      echo "Argument 2 must be set! It will be used as password for '${db_user}'!"
      exit 2
  fi

  db_password="${password}"
}

function setup_test() {
  db_name="${db_test_name}"
  db_user="${db_test_user}"
  db_password="${db_test_password}"
}

case "${1}" in
  test)
    setup_test
  ;;

  prod)
    setup_prod "${2}"
  ;;

  *)
    echo -e "Usage:\n${0} test\n${0} prod <db_password>"
    exit 1
  ;;
esac

# Generate sql
cat <<EOT> "01_preconfig_${db_name}.sql"
-- STEP 0 :: !! ONLY FOR TEST DB :: terminate all connections and drop test database
SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE datname='${db_test_name}';
DROP DATABASE IF EXISTS ${db_test_name};
--
--
-- STEP 1 :: create database and its schemas
CREATE DATABASE ${db_name};
\connect ${db_name};
CREATE SCHEMA ${schema_secure};
--
--
-- STEP 2 :: drop schema public
DROP SCHEMA public;
--
EOT

# This can be only executed after ${db_name}_preconfig.sql and the db layout deployment
# by running 'mvn flyway:migrate -P<test/dev>'
cat <<EOF> "02_postconfig_${db_name}.sql"
-- STEP 3 :: drop and create db_user
DROP ROLE ${db_user};
CREATE USER ${db_user} WITH ENCRYPTED PASSWORD '${db_password}';
--
-- connect to db
--
\connect ${db_name}
--
-- STEP 4 :: info secure
GRANT USAGE ON SCHEMA ${schema_secure} TO ${db_user};
--
--
-- STEP 5 :: grants onf secure schema functions
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schema_secure} TO ${db_user};
--
--
-- STEP 6 :: secure schema all tables
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON secure.pubkyes TO ${db_user};
EOF
