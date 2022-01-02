#!/bin/bash
# This script generates SQL scripts to bootstrap or reset databases for Cornerstone.

# Global variables to be set
DATABASE_NAME=''
SCHEMA_LIST=''

function echoHelp(){
    echo "Example1: ${0} db=mydb sch=sch1"
    echo "Example2: ${0} db=mydb sch=sch1,sch2"
}

function parseArguments(){
    for keyword in "${@}"; do
        local keyword="${1}"
        case ${keyword} in
            db=*)
                DATABASE_NAME="${keyword#*=}"
                shift
            ;;

            sch=*)
                SCHEMA_LIST="${keyword#*=}"
                shift
            ;;

            *)
                echo "Unknown argument: '${keyword}'"
                echoHelp
                exit 2
            ;;
        esac
    done
}

function generateCreateSchemas(){
    readonly max=$((${#} - 1))
    generated=''
    index=0

    for schema in "${@}"; do
        if [ ${index} -eq ${max} ]; then
            generated+="CREATE SCHEMA ${schema};"
        else
            generated+="CREATE SCHEMA ${schema};\n"
        fi
        index=$((index+1))
    done

    echo -e "${generated}"
}

function generateResetSQL(){
    readonly database_name="${1}"
    readonly schemas="${2}"

cat << EOF
SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE datname='${database_name}';
DROP DATABASE IF EXISTS ${database_name};
CREATE DATABASE ${database_name};
\connect ${database_name};
$(generateCreateSchemas $(echo "${schemas}" | tr ',' ' '))
DROP SCHEMA IF EXISTS public;
EOF
}

# parse arguments if all is OK, generate SQL
if [ ${#} -eq 2 ]; then
    parseArguments "${@:1}"
    generateResetSQL "${DATABASE_NAME}" "${SCHEMA_LIST}"
else
    echoHelp
fi
