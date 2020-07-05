#!/bin/bash

# Generates the sql file to re-create the user_accounts database
# DESTRUCTIVE IT WILL DESTROY THE PREVIOUS DB!

# ----------------------------------------
# Schemas
# ----------------------------------------
readonly schema_user_data='user_data'
# ----------------------------------------
# Prod
# ----------------------------------------
readonly db_live_name='users'
readonly db_live_user='robot'
# ----------------------------------------
# Test
# ----------------------------------------
readonly db_dev_name='dev_users'
readonly db_dev_user='dev_robot'
# ----------------------------------------
# Running variable to be used
# ----------------------------------------
declare db_name=
declare db_user=
declare db_password=
# ----------------------------------------
declare SQL_FILE_NAME=
declare SQL_SETUP_FILE_NAME=
# ----------------------------------------

function setup_variables() {
  local p_db_name="${1}"
  local p_db_user="${2}"
  local p_db_password="${3}"
  shift
  shift
  shift

  db_name="${p_db_name}"
  db_user="${p_db_user}"
  db_password="${p_db_password}"

  if [ -z "${db_name}" ]; then
    echo "'db_name' is not set!"
    exit 1
  fi

  if [ -z "${db_user}" ]; then
    echo "'db_user' is not set!"
    exit 2
  fi

  if [ -z "${db_password}" ]; then
    echo "'db_password' is not set!"
    exit 3
  fi

  SQL_FILE_NAME="sql/create_db__${db_name}.sql"
  SQL_SETUP_FILE_NAME="sql/setup_db__${db_name}.sql"
}

function generate_create_sql_file() {
cat <<EOT >"${SQL_FILE_NAME}"
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
--
EOT
}

function generate_setup_sql_file(){
cat <<EOF >"${SQL_SETUP_FILE_NAME}"
-- STEP 3 :: create roles/users ${db_user}
DROP ROLE ${db_user};
CREATE USER ${db_user} WITH ENCRYPTED PASSWORD '${db_password}';
--
-- connect to db
\connect ${db_name}
--
-- STEP 4 :: schema ${schema_user_data} GRANTS for ${db_user}
GRANT USAGE ON SCHEMA ${schema_user_data} TO ${db_user};
--
--
-- STEP 5 :: schema ${schema_user_data} sequences, functions (usage is required to call nextval function) GRANTS for ${db_user}
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA ${schema_user_data} TO ${db_user};
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schema_user_data} TO ${db_user};
--
--
-- STEP 6 :: schema ${schema_user_data} GRANTS for ${db_user}
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON ALL TABLES IN SCHEMA ${schema_user_data} TO ${db_user};
--
--
-- STEP 7 :: schema ${schema_user_data} GRANTS for ${db_user}
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA ${schema_user_data} TO ${db_user};
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schema_user_data} TO ${db_user};
GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER ON ALL TABLES IN SCHEMA ${schema_user_data} TO ${db_user};
GRANT CREATE ON SCHEMA ${schema_user_data} TO ${db_user};
EOF
}

case "${1}" in
  dev)
    setup_variables "${db_dev_name}" "${db_dev_user}" "${2}"
    generate_create_sql_file
    generate_setup_sql_file
    echo "psql -f ${SQL_FILE_NAME} (as postgres user)"
    echo "psql -f ${SQL_SETUP_FILE_NAME} (as postgres user)"
  ;;

  live)
    setup_variables "${db_live_name}" "${db_live_user}" "${2}"
    generate_create_sql_file
    generate_setup_sql_file
    echo "psql -f ${SQL_FILE_NAME} (as postgres user)"
    echo "psql -f ${SQL_SETUP_FILE_NAME} (as postgres user)"
  ;;

  *)
    echo -e "Usage:\n${0} dev|live <db_password>"
    exit 10
  ;;
esac
