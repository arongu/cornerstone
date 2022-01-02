#!/bin/bash

DB_TYPE=''
POSTGRES_PASSWORD=''
DB_NAME=''

function echoHelp(){
    echo -e "BE CAREFUL! This scripts DESTROYS then RE-CREATES the given DATABASE!\n\tNote: 'password' is the password of the 'postgres' user\n"
    echo -e "Examples:\n${0} type=work name=work password=postgres_password"
    echo -e "${0} type=users name=users password=postgres_password"
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
    echo "${POSTGRES_PASSWORD}" | su -c "psql -f ${1}" postgres
}

function resetDB(){
    local fileName=''
    case ${DB_TYPE} in
        work)
            fileName='work_reset.sql'
            ./00_gen_reset_sql.sh db="${DB_NAME}" sch=security > "${fileName}"
            executePsqlFile "${fileName}"
        ;;

        users)
            fileName='users_reset.sql'
            ./00_gen_reset_sql.sh db="${DB_NAME}" sch=system,users > "${fileName}"
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
if [ ${#} -eq 3 ]; then
    parseArguments "${@:1}"
    resetDB
else
    echo "Not enough arguments! 3 required, given=(${#})'"
    echoHelp
fi
