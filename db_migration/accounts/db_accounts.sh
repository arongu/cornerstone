#!/bin/bash

# Script to geneare the "accounts" database for cornerstone!
# Variables to be substituted
declare db_password
declare db_name
declare db_user

# Common
declare schema_info="info"
declare schema_history="history"

# prod/dev settings ------------------------------------------------------------------------
readonly prod_db_name='accounts'
readonly prod_db_user='robot'
# test settings ------------------------------------------------------------------------
readonly test_db_name='test_accounts'
readonly test_db_user='test'
readonly test_db_password='test'
# ------------------------------------------------------------------------------------------

function setup_prod() {
  local password="${1}"
  shift

  if [ -z "${password}" ]; then
      echo "Argument 2 must be set! It will be used as '${prod_db_user}'s password!"
      exit 2
  else
    db_password="${password}"
  fi

  db_name="${prod_db_name}"
  db_user="${prod_db_user}"
  db_password="${test_db_password}"
}

function setup_test() {
  db_name="${test_db_name}"
  db_password="test"
  db_user="${test_db_user}"
}


case "${1}" in
  test)
    setup_test
    db_password="${1}"
  ;;

  prod|dev)
    setup_prod "${2}"
  ;;

  *)
    echo -e "Usage:\n${0} test\n${0} prod|dev <db_password>"
    exit 1
  ;;
esac

# Generate sql
cat <<EOT> "${db_name}.sql"
-- STEP 1 :: terminate all connection to TEST DATABASE !!!
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE datname='${test_db_name}';
--
-- STEP 2 :: drop TEST DATABASE if exists !!!
DROP DATABASE IF EXISTS ${test_db_name};
--
--
-- STEP 3 :: create database and its schemas
CREATE DATABASE ${db_name};
\connect ${db_name};
CREATE SCHEMA "${schema_info}";
CREATE SCHEMA "${schema_history}";
--
--
-- STEP 4 :: drop schema public
DROP SCHEMA public;
--
--
-- STEP 5 :: drop and create db_user
DROP ROLE ${db_user};
CREATE ROLE ${db_user} WITH ENCRYPTED PASSWORD '${db_password}';
--
--
EOT
