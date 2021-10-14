#!/bin/bash

DB_TYPE=''
DB_PASSWORD=''

function echoHelp() {
    echo -e "BE CAREFUL! This scripts DESTROYS then RE-CREATES the given DATABASE!\n\tNote: -p / --password is the password of the 'postgres' user\n"
    echo -e "Examples:\n${0} -t=work -p=postgres_password\n${0} -t=users -p=postgres_password"
    echo -e "\n${0} --type=work --password=postgres_password\n${0} --type=users --password=postgres_password"
}

function echoFinalStep() {
    echo -e "\n\nComplete!\nNow run flyway to start the database migration!\nGo inside the project dir db/users or db/work then execute the following command:\nmvn flyway:migrate -P<profile>"
}

function parseArguments() {
    for keyword in "${@}"; do
        local keyword="${1}"
        case ${keyword} in
            --type=*|-t=*)
                DB_TYPE="${keyword#*=}"
                shift
            ;;

            --password=*|-p=*)
                DB_PASSWORD="${keyword#*=}"
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

function executePsqlFile() {
    echo "${DB_PASSWORD}" | su -c "psql -f ${1}" postgres
}

function resetDB() {
    local fileName=''
    case ${DB_TYPE} in
        work)
            fileName='work_reset.sql'
            ./00_gen_reset_sql.sh -db=work -sch=security > "${fileName}"
            executePsqlFile "${fileName}"
        ;;

        users)
            fileName='users_reset.sql'
            ./00_gen_reset_sql.sh -db=users -sch=system,users > "${fileName}"
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
if [ ${#} -eq 2 ]; then
  parseArguments "${@:1}"
  resetDB
else
  echoHelp
fi
