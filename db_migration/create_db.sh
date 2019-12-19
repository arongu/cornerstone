#!/bin/bash

readonly db_accounts='accounts'
readonly db_accounts_user='robot'
# schemas
readonly db_accounts_schema_info='info'
readonly db_accounts_schema_history='history'

if [ -z "${1}" ]; then
  echo "Argument 1 must be set! It will be used as ${db_accounts_user}'s password!"
  exit 1
else
  db_accounts_user_password="${1}"
fi

cat <<EOT> db_accounts.sql
CREATE DATABASE ${db_accounts};
\connect ${db_accounts};
CREATE SCHEMA "${db_accounts_schema_info}";
CREATE SCHEMA "${db_accounts_schema_history}";
CREATE USER ${db_accounts_user} WITH ENCRYPTED PASSWORD '${db_accounts_user_password}';
EOT
