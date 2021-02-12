package cornerstone.webapp.configuration;

import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.configuration.enums.DB_USERS_ENUM;
import cornerstone.webapp.configuration.enums.DB_WORK_ENUM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Sorts the loaded configuration into separate 'Properties':
 * application
 * work db
 * user db
 */
public class ConfigSorter {
    private static final Logger logger = LoggerFactory.getLogger(ConfigSorter.class);
    private static final String errorMessage  = "'{}' is not set!";

    private Properties rawProperties;
    private Properties properties_db_users;
    private Properties properties_db_work;
    private Properties properties_app;

    public ConfigSorter(final Properties properties) {
        rawProperties = properties;
    }

    /**
     * Adds key-value pairs to a Properties instance and the meantime it logs it.
     * Values of keys which contains 'password' in their name will not be shown in the logs!
     * e.g.: work_db_password = *****
     * @param key This key will be added to the props.
     * @param value Value of the key
     * @param props The Properties object which the key/value will be added.
     * @param logPrefix Prefix used to make the logs easier to read e.g.: "db_work" or "db_user" or "app".
     * @param sensitiveValue To prevent password/keys to be leaked into logs the value will be replaced with asterisks if set to true.
     */
    private static void addKeyValueAndLogIt(final Properties props, final String key, final String value, final String logPrefix, final boolean sensitiveValue) {
        if ( key != null && ! key.isEmpty() && value != null && ! value.isEmpty()) {
            props.setProperty(key, value);

            if ( sensitiveValue ) {
                logger.info(String.format("++ %-15s %-30s = *****", logPrefix, key));
            } else {
                logger.info(String.format("++ %-15s %-30s = '%s'", logPrefix, key, value));
            }

        } else {
            logger.error(errorMessage, key);
        }
    }

    /**
     * Sorts the "raw" Properties object into 3 separate Properties by initializing them.
     * Sorting happens by iterating through the ENUM values of each property sets.
     * APP_ENUM         - holds the required application configuration filed names
     * DB_USERS_ENUM    - holds the required USER DB configuration field names
     * DB_WORK_ENUM     - holds the required WORK DB configuration field names
     * @throws ConfigSorterException if any of the required field is missing.
     */
    public void sortProperties() throws ConfigSorterException {
        final Set<String> missingProperties = new HashSet<>();
        // WORK DB
        properties_db_work = new Properties();
        for ( final DB_WORK_ENUM work_enum : DB_WORK_ENUM.values()) {
            if ( null != rawProperties.get(work_enum.key)) {
                addKeyValueAndLogIt(
                        properties_db_work, work_enum.key,
                        rawProperties.getProperty(work_enum.key), DB_WORK_ENUM.PREFIX_DB_WORK,
                        work_enum.sensitiveValue
                );

            } else {
                logger.error(errorMessage, work_enum.key);
                missingProperties.add(work_enum.key);
            }
        }

        // USERS DB
        properties_db_users = new Properties();
        for ( final DB_USERS_ENUM db_users_enum : DB_USERS_ENUM.values()) {
            if ( null != rawProperties.get(db_users_enum.key)) {
                addKeyValueAndLogIt(
                        properties_db_users, db_users_enum.key,
                        rawProperties.getProperty(db_users_enum.key), DB_USERS_ENUM.PREFIX_DB_USERS,
                        db_users_enum.sensitiveValue
                );

            } else {
                logger.error(errorMessage, db_users_enum.key);
                missingProperties.add(db_users_enum.key);
            }
        }

        // APP
        properties_app = new Properties();
        for ( final APP_ENUM app_enum : APP_ENUM.values()) {
            if ( null != rawProperties.get(app_enum.key)) {
                addKeyValueAndLogIt(
                        properties_app, app_enum.key,
                        rawProperties.getProperty(app_enum.key), APP_ENUM.PREFIX_APP,
                        app_enum.sensitiveValue
                );

            } else {
                logger.error(errorMessage, app_enum.key);
                missingProperties.add(app_enum.key);
            }
        }

        if (missingProperties.size() != 0) {
            final String msg = "The following configuration fields are not set: " + missingProperties.toString();
            logger.error(msg);
            throw new ConfigSorterException(msg);
        }
    }

    /**
     * @return Properties of application. (Fields in APP_ENUM)
     */
    public Properties getPropertiesForApp() {
        return properties_app;
    }

    /**
     * @return Properties of work DB. (Fields in DB_WORK_ENUM)
     */
    public Properties getPropertiesForWorkDB() {
        return properties_db_work;
    }

    /**
     * @return Properties of users DB. (Fields in DB_USERS_ENUM)
     */
    public Properties getPropertiesForUsersDB() {
        return properties_db_users;
    }

    /**
     * Sets the common/raw Properties object.
     * @param  properties The new common/raw Properties to work with it.
     */
    public void setProperties(final Properties properties) {
        rawProperties = properties;
    }
}
