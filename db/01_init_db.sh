#!/bin/bash

DB_TYPE=''
POSTGRES_PASSWORD=''
DB_NAME=''
HOST=''

function echoHelp(){
    echo -e "BE CAREFUL! This scripts DESTROYS then RE-CREATES the given DATABASE!\n\tNote: 'password' is the password of the 'postgres' user\n"
    echo -e "Examples:\n${0} host=10.10.10.55 type=work name=work password=postgres_password"
    echo -e "${0} host=10.10.10.55 type=users name=users password=postgres_password"
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
                echo "Unknown argument: '${keyword}'"
                echoHelp
                exit 1
            ;;
        esac
    done
}

function executePsqlFile(){
    export PGPASSWORD="${POSTGRES_PASSWORD}"
    psql --username='postgres' --host="${HOST}" --file="${1}"
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
          echoHelp
          exit 2
        ;;
    esac

    rm -v "${fileName}"
    echoFinalStep
}

# parse arguments if all is OK, reset DB
if [ ${#} -eq 4 ]; then
    parseArguments "${@:1}"
    resetDB
else
    echo "Not enough arguments! 4 required, given=(${#})'"
    echoHelp
fi
