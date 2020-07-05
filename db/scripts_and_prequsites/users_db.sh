#!/bin/bash
# TO BOOTSTRAP/CREATE users DB !
# DESTRUCTIVE IT WILL DESTROY THE PREVIOUS DB!

readonly schema_user_data='user_data'

declare db_name=
declare sql_file_name=

function setup_variables() {
  local p_db_name="${1}"
  shift

  db_name="${p_db_name}"

  if [ -z "${db_name}" ]; then
    echo "'db_name' is not set!"
    exit 1
  fi

  sql_file_name="sql/bootstrap_db__${db_name}.sql"
}

function generate_bootstrap_sql_file() {
cat <<EOT >"${sql_file_name}"
-- STEP 0 :: terminate all connections and drop database
SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE datname='${db_name}';
DROP DATABASE IF EXISTS ${db_name};
--
--
-- STEP 1 :: create database ${db_name} and its schemas: ${schema_user_data}
CREATE DATABASE ${db_name};
\connect ${db_name};
CREATE SCHEMA ${schema_user_data};
--
--
-- STEP 2 :: drop schema public
DROP SCHEMA public;
EOT
}

case "${1}" in
  dev)
    setup_variables 'dev_users'
    generate_bootstrap_sql_file
    echo "psql -f ${sql_file_name} (as postgres user)"
  ;;

  live)
    setup_variables 'users'
    generate_bootstrap_sql_file
    echo "psql -f ${sql_file_name} (as postgres user)"
  ;;

  *)
    echo -e "Usage:\n${0} dev|live <db_password>"
    exit 10
  ;;
esac
