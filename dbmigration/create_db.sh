#!/bin/bash

readonly db_account='account'
readonly db_account_schema='accounts_schema'
readonly db_user='robot'


if [ -z "${1}" ]; then
  echo "Argument 1 must be set -- this will be db password!"
  exit 1
else
  db_user_password="${1}"
fi

cat <<EOT> robot_account.sql
CREATE DATABASE ${db_account};
\connect ${db_account};
CREATE SCHEMA "${db_account_schema}";
CREATE USER ${db_user} WITH ENCRYPTED PASSWORD '${db_user_password}';
EOT
