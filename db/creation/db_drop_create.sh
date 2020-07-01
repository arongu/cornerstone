#!/bin/bash
# ----------------------------------------
# Schemas
# ----------------------------------------
readonly schema_info='info'
readonly schema_history='user_history'
# ----------------------------------------
# Prod
# ----------------------------------------
readonly db_prod_name='user_accounts'
readonly db_prod_user='robot'
# ----------------------------------------
# Test
# ----------------------------------------
readonly db_test_name='test_user_accounts'
readonly db_test_user='test_robot'
# ----------------------------------------
# Running variable to be used
# ----------------------------------------
declare db_name=
declare db_user=
declare db_password=
# ----------------------------------------

function setup_variables_for_prod() {
  local password="${1}"
  shift

  db_name="${db_prod_name}"
  db_user="${db_prod_user}"
  db_password="${password}"

  if [ -z "${db_name}" ]; then
    echo "'db_name' is not set for production!"
    exit 1
  fi

  if [ -z "${db_user}" ]; then
    echo "'db_user' is not set for production!"
    exit 2
  fi

  if [ -z "${db_password}" ]; then
    echo "'db_password' is not set for production!"
    exit 3
  fi
}

function setup_variables_for_test() {
  local password="${1}"
  shift

  db_name="${db_test_name}"
  db_user="${db_test_user}"
  db_password="${password}"

  if [ -z "${db_name}" ]; then
    echo "'db_name' is not set for test!"
    exit 4
  fi

  if [ -z "${db_user}" ]; then
    echo "'db_user' is not set for test!"
    exit 5
  fi

  if [ -z "${db_password}" ]; then
    echo "'db_password' is not set for test!"
    exit 6
  fi
}

function generate_drop_create_sql() {
cat <<EOT >"01_drop_and_create_${db_name}.sql"
-- STEP 0 :: terminate all connections and drop database
SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE datname='${db_name}';
DROP DATABASE IF EXISTS ${db_name};
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
}


case "${1}" in
  test)
    setup_variables_for_test "${2}"
    generate_drop_create_sql
  ;;

  prod)
    setup_variables_for_prod "${2}"
    generate_drop_create_sql
  ;;

  *)
    echo -e "Usage:\n${0} test|prod <db_password>"
    exit 10
  ;;
esac
