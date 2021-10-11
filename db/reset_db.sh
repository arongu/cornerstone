#!/bin/bash

DB_TYPE=''

function echoHelp() {
  echo -e "Examples:\n${0} -t=work\n${0} -t=users"
}

function parseArguments() {
    for keyword in "${@}"; do
        local keyword="${1}"
        case ${keyword} in
            --type=*|-t=*)
                DB_TYPE="${keyword#*=}"
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

function genResetSQL() {
    case ${DB_TYPE} in
        work)
          ./gen_reset_sql.sh -db=work -sch=secure
        ;;

        users)
          ./gen_reset_sql.sh -db=users -sch=user_data
        ;;

        *)
          echoHelp
          exit 2
        ;;
    esac
}

# parse arguments if all is OK, generate SQL
if [ ${#} -eq 1 ]; then
  parseArguments "${@:1}"
  genResetSQL
else
  echoHelp
fi
