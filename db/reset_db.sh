#!/bin/bash

DB_TYPE=''
DB_PASSWORD=''

function echoHelp() {
  echo -e "BE CAREFUL! This scripts DESTROYS then RE-CREATES the given DATABASE!\n\tNote: -p / --password is the password of the 'postgres' user\n"
  echo -e "Examples:\n${0} -t=work -p=postgres_password\n${0} -t=users -p=postgres_password"
  echo -e "\n${0} --type=work --password=postgres_password\n${0} --type=users --password=postgres_password"
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

function resetDB() {
    echo "${DB_PASSWORD}" | su -c "psql -f ${1}" postgres
}

function genResetSQL() {
    local fileName=''
    case ${DB_TYPE} in
        work)
            fileName='work_reset.sql'
            ./gen_reset_sql.sh -db=work -sch=secure > "${fileName}"
            resetDB "${fileName}"
        ;;

        users)
            fileName='users_reset.sql'
            ./gen_reset_sql.sh -db=users -sch=user_data > "${fileName}"
            resetDB "${fileName}"
        ;;

        *)
          echoHelp
          exit 2
        ;;
    esac
}

# parse arguments if all is OK, generate SQL
if [ ${#} -eq 2 ]; then
  parseArguments "${@:1}"
  genResetSQL
else
  echoHelp
fi
