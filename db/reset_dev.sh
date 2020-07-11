#!/bin/bash
# Remove all sql files and re-create SQL files for dev
rm -v *.sql
python3 bootstrap_sql_generator.py dev

# Bootstrap dev DBs
# echo 'db' is the password for the postgres user
echo 'db' | su -c 'psql -f bootstrap__dev_work.sql' postgres
echo 'db' | su -c 'psql -f bootstrap__dev_users.sql' postgres

# cleanup generated SQL files
whoami
rm -v *.sql

# Run maven migrate on DB modules (users, work)
cd ./users && pwd
mvn validate -Pdev
mvn flyway:migrate -Pdev

cd ..
cd ./work && pwd
mvn validate -Pdev
mvn flyway:migrate -Pdev
