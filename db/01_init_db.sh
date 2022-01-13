#!/bin/bash

DB_TYPE=''
POSTGRES_PASSWORD=''
DB_NAME=''
HOST=''
RESULT=''

function echoHelp(){
    echo -e "BE CAREFUL! This scripts DESTROYS then RE-CREATES the given DATABASE!\nNote: 'password' is the password of the 'postgres' user\n"
    echo -e "Examples:\n${0} host=10.10.10.55 type=work name=work password=postgres_password"
    echo -e "${0} host=db100 type=users name=users password=postgres_password"
}

function echoFinalStep(){
    echo -e "\n\nComplete!\nNow run flyway to start the database migration!\nGo inside the project dir db/users or db/work then execute the following command:\nmvn flyway:migrate -P<profile>"
}

function parseArguments(){
    for keyword in "${@}"; do
        local keyword="${1}"
        case ${keyword} in
            type=*)
                DB_TYPE="${keyword#*=}"
                shift
            ;;

            name=*)
                DB_NAME="${keyword#*=}"
                shift
            ;;

            host=*)
                HOST="${keyword#*=}"
                shift
            ;;

            password=*)
                POSTGRES_PASSWORD="${keyword#*=}"
                shift
            ;;

            *)
                >&2 echo "Unknown argument: '${keyword}'"
                echoHelp
                exit 1
            ;;
        esac
    done
}

function executePsqlFile(){
    export PGPASSWORD="${POSTGRES_PASSWORD}"
    psql --username='postgres' --host="${HOST}" --file="${1}"
    RESULT="${?}"
    unset PGPASSWORD
}

function resetDB(){
    local fileName=''
    case ${DB_TYPE} in
        work)
            fileName='work_init.sql'
            ./00_gen_init_sql.sh db="${DB_NAME}" sch=security > "${fileName}"
            executePsqlFile "${fileName}"
        ;;

        users)
            fileName='users_init.sql'
            ./00_gen_init_sql.sh db="${DB_NAME}" sch=system,users > "${fileName}"
            executePsqlFile "${fileName}"
        ;;

        *)
          >&2 echo "Invalid database type: '${keyword}' (users/work)"
          exit 2
        ;;
    esac

    rm  "${fileName}"
    if [ ${RESULT} -eq 0 ]; then
        echoFinalStep
    fi
}

# parse arguments if all is OK, reset DB
if [ ${#} -eq 4 ]; then
    parseArguments "${@:1}"
    resetDB
else
    >&2 echo "Not enough arguments! required=4, given=${#}"
    echoHelp
fi