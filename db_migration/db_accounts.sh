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
readonly test_db_user='tester'
# ------------------------------------------------------------------------------------------

function setup_prod() {
  local password="${1}"
  shift

  if [ -z "${password}" ]; then
      echo "Argument 2 must be set! It will be used as ${db_accounts_user}'s password!"
      exit 2
  else
    db_password="${password}"
  fi

  db_name="${prod_db_name}"
  db_user="${prod_db_user}"
}

function setup_test() {
  db_name="${test_db_name}"
  db_password="tester"
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
CREATE DATABASE ${db_name};
\connect ${db_name};
CREATE SCHEMA "${schema_info}";
CREATE SCHEMA "${schema_history}";
CREATE USER ${db_user} WITH ENCRYPTED PASSWORD '${db_password}';
EOT
