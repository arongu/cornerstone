#!/bin/bash
# Script to generate the "accounts" database.
declare db_name=
declare db_user=
declare db_password=

# ----------------------------------------
# Schemas
declare schema_info="info"
declare schema_history="history"

# Prod
db_prod_name='accounts'
db_prod_user='robot'

# Test
db_test_name='test_accounts'
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
CREATE SCHEMA ${schema_info};
CREATE SCHEMA ${schema_history};
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
-- STEP 4 :: info schema
GRANT USAGE ON SCHEMA ${schema_info} TO ${db_user};
--
--
-- STEP 5 :: info schema sequences, functions (usage is required to call nextval function)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA ${schema_info} TO ${db_user};
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schema_info} TO ${db_user};
--
--
-- STEP 6 :: info schema all tables
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON ALL TABLES IN SCHEMA ${schema_info} TO ${db_user};
--
--
-- STEP 7 :: history schema
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA ${schema_history} TO ${db_user};
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schema_history} TO ${db_user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER ON ALL TABLES IN SCHEMA ${schema_history} TO ${db_user};
GRANT CREATE ON SCHEMA ${schema_history} TO ${db_user};
EOF
