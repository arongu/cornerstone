from string import Template
import sys

# This script generates a bootstrap SQL script which DROPS and RE-CREATES a database with the given schemas!'

tmpl_terminate_connections  = Template('SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE datname=\'$database\';')
tmpl_drop_db                = Template('DROP DATABASE IF EXISTS $database;')
tmpl_create_db              = Template('CREATE DATABASE $database;')
tmpl_connect_db             = Template('\\connect $database;')
tmpl_create_schema          = Template('CREATE SCHEMA $schema;')
tmpl_drop_schema            = Template('DROP SCHEMA IF EXISTS $schema;')


class SQLScriptGenerator:
    def __init__(self, p_db_name, p_schemas):
        self.db_name = p_db_name
        self.schemas = p_schemas


    def gen_create_schemas(self):
        create_schemas = ''
        for sch in self.schemas:
            create_schemas += '\n' + tmpl_create_schema.substitute(schema = sch)

        return create_schemas


    def gen_sql_script(self):
        return tmpl_terminate_connections.substitute(database = self.db_name) + '\n' + \
               tmpl_drop_db.substitute(database = self.db_name) + '\n' + \
               tmpl_create_db.substitute(database = self.db_name) + '\n' + \
               tmpl_connect_db.substitute(database = self.db_name) + \
               self.gen_create_schemas() + '\n' + \
               tmpl_drop_schema.substitute(schema = 'public')



def generate_bootstrap_file(db_name, schemas):
    gen = SQLScriptGenerator(db_name, schemas)
    f_db = open('bootstrap' + '__' + db_name + '.sql', 'w')
    f_db.write(gen.gen_sql_script())
    f_db.close()


if __name__ == '__main__':
    example = 'Example:\n ' + sys.argv[0] + ' dev|live'

    if len(sys.argv) != 2:
        print(example)
        exit(1)
    else:
        if sys.argv[1] == 'dev':
            generate_bootstrap_file('dev_users', ['user_data'])
            generate_bootstrap_file('dev_work', ['secure'])
            exit(0)

        elif sys.argv[1] == 'live':
            generate_bootstrap_file('users', ['user_data'])
            generate_bootstrap_file('work', ['secure'])
            exit(0)

        else:
            print(example)
            exit(2)
