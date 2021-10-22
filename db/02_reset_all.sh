#!/bin/bash
set -e

# pom files
readonly PROJECT_ROOT_DIR="$(pwd)"
readonly USERS_POM_FILE="${PROJECT_ROOT_DIR}/users/pom.xml";
readonly WORK_POM_FILE="${PROJECT_ROOT_DIR}/work/pom.xml";

# password for the postgresql
readonly POSTGRES_PASSWORD='db';

function reset_db() {
    t="${1}";
    shift;
    n="${2}";
    shift;
    p="${3}";
    shift;

    ./01_reset_db.sh --type="${t}" --name="${n}" --password="${p}";
}

function migrate_db() {
    proj_dir="${1}";
    profile="${2}";
    shift;
    shift;

    mvn -f "${proj_dir}" flyway:migrate -P"${profile}";
}

# reset dbs
function reset_dev_dbs() {
    ./01_reset_db.sh --type=work  --name=dev_work  --password="${POSTGRES_PASSWORD}";
    ./01_reset_db.sh --type=users --name=dev_users --password="${POSTGRES_PASSWORD}";
}

function reset_live_dbs() {
    ./01_reset_db.sh --type=work  --name=work  --password="${POSTGRES_PASSWORD}";
    ./01_reset_db.sh --type=users --name=users --password="${POSTGRES_PASSWORD}";
}

# migrate dbs
function migrate_dev_dbs() {
    migrate_db "${USERS_POM_FILE}" "dev";
    migrate_db "${WORK_POM_FILE}"  "dev";
}

function migrate_live_dbs() {
    migrate_db "${USERS_POM_FILE}" "live";
    migrate_db "${WORK_POM_FILE}"  "live";
}

function reset_all() {
    reset_dev_dbs
    reset_live_dbs
    migrate_dev_dbs
    migrate_live_dbs
}

# run script
echo -e "... work dir: ${PROJECT_ROOT_DIR}\nDO YOU WANT RESET DBs? THIS WILL DESTROY ALL DATA!!!";
echo "Enter 1 for 'dev', enter 2 for 'live', enter 3 for 'all' (anything else to quit)!"
select choice in dev live all; do
    case ${choice} in
        all)
            reset_all;
            break;
        ;;

        dev)
            reset_dev_dbs;
            migrate_dev_dbs
            break;
        ;;

        live)
            reset_live_dbs;
            migrate_live_dbs
            break;
        ;;

        *)
            exit;
        ;;
    esac
done
