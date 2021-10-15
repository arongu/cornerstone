#!/bin/bash
# names of the 'work' dbs
readonly WORK_DBS=(work dev_work)
# names of the 'user' dbs
readonly USERS_DBS=(users dev_users)
# maven profiles used for migration
readonly MAVEN_PROFILES=(live dev)
readonly PROJECT_ROOT_DIR="$(pwd)"
# password for the postgresql
readonly POSTGRES_PASSWORD='db'

function reset_work_dbs() {
    for wdb in "${WORK_DBS[@]}"; do
        echo "... Resetting ${wdb} ...";
        ./01_reset_db.sh --type='work' --name="${wdb}" --password="${POSTGRES_PASSWORD}";
    done
}

function reset_user_dbs() {
    for udb in "${USERS_DBS[@]}"; do
        echo "... Resetting ${udb} ...";
        ./01_reset_db.sh --type='users' --name="${udb}" --password="${POSTGRES_PASSWORD}";
    done
}

function migrate_users() {
    for profile in "${MAVEN_PROFILES[@]}"; do
        mvn -f "${PROJECT_ROOT_DIR}/users/pom.xml" flyway:migrate -P"${profile}";
    done
}

function migrate_works() {
    for profile in "${MAVEN_PROFILES[@]}"; do
        mvn -f "${PROJECT_ROOT_DIR}/work/pom.xml" flyway:migrate -P"${profile}";
    done
}

function reset_all() {
    reset_work_dbs
    migrate_works
    reset_user_dbs
    migrate_users
}

# run script
echo -e "... work dir: ${PROJECT_ROOT_DIR}\nDO YOU WANT RESET ALL DBs? THIS WILL DESTROY ALL DATA!!!";
echo "Press 1 for YES, press 2 for NO!"
select yn in YES NO; do
    case $yn in
        YES)
            reset_all;
            break;
        ;;

        NO)
            exit;
        ;;
    esac
done
