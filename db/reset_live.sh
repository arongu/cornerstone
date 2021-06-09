#!/bin/bash
# Remove all sql files and re-create SQL files for dev
rm -v *.sql
python3 bootstrap_sql_generator.py live

# Bootstrap live DBs
# echo 'db' is the password for the postgres user
echo 'db' | su -c 'psql -f bootstrap__work.sql' postgres
echo 'db' | su -c 'psql -f bootstrap__users.sql' postgres

# cleanup generated SQL files
whoami
rm -v *.sql

# Run maven migrate on DB modules (users, work)
cd ./users && pwd
mvn validate -Plive
mvn flyway:migrate -Plive

cd ..
cd ./work && pwd
mvn validate -Plive
mvn flyway:migrate -Plive
