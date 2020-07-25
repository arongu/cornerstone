package cornerstone.webapp.configuration;

import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.configuration.enums.DB_USERS_ENUM;
import cornerstone.webapp.configuration.enums.DB_WORK_ENUM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class ConfigurationSorter {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationSorter.class);
    private static final String errorMessage  = "'{}' is not set!";

    private Properties rawProperties;
    private Properties properties_db_users;
    private Properties properties_db_work;
    private Properties properties_app;

    public ConfigurationSorter(final Properties properties) {
        rawProperties = properties;
    }

    /**
     * Logs and sets key-value pairs as a property.
     */
    private static void addKeyValueAndLogIt(final Properties props, final String key, final String value, final String logPrefix) {
        if ( key != null && ! key.isEmpty() && value != null && ! value.isEmpty()) {
            props.setProperty(key, value);

            if ( key.contains("password") || key.contains("key")) {
                logger.info(String.format("++ %-15s %-30s = *****", logPrefix, key));
            } else {
                logger.info(String.format("++ %-15s %-30s = '%s'", logPrefix, key, value));
            }

        } else {
            logger.error(errorMessage, key);
        }
    }

    public void sortProperties() throws ConfigurationSorterException {
        final Set<String> missingProperties = new HashSet<>();
        // WORK DB
        properties_db_work = new Properties();
        for ( final DB_WORK_ENUM work_enum : DB_WORK_ENUM.values()) {
            if ( null != rawProperties.get(work_enum.key)) {
                addKeyValueAndLogIt(properties_db_work, work_enum.key, rawProperties.getProperty(work_enum.key), DB_WORK_ENUM.PREFIX_DB_WORK);

            } else {
                logger.error(errorMessage, work_enum.key);
                missingProperties.add(work_enum.key);
            }
        }

        // USERS DB
        properties_db_users = new Properties();
        for ( final DB_USERS_ENUM db_users_enum : DB_USERS_ENUM.values()) {
            if ( null != rawProperties.get(db_users_enum.key)) {
                addKeyValueAndLogIt(properties_db_users, db_users_enum.key, rawProperties.getProperty(db_users_enum.key), DB_USERS_ENUM.PREFIX_DB_USERS);

            } else {
                logger.error(errorMessage, db_users_enum.key);
                missingProperties.add(db_users_enum.key);
            }
        }

        // APP
        properties_app = new Properties();
        for ( final APP_ENUM app_enum : APP_ENUM.values()) {
            if ( null != rawProperties.get(app_enum.key)) {
                addKeyValueAndLogIt(properties_app, app_enum.key, rawProperties.getProperty(app_enum.key), APP_ENUM.PREFIX_APP);

            } else {
                logger.error(errorMessage, app_enum.key);
                missingProperties.add(app_enum.key);
            }
        }

        if (missingProperties.size() != 0) {
            final String msg = "The following configuration fields are not set: " + missingProperties.toString();
            logger.error(msg);
            throw new ConfigurationSorterException(msg);
        }
    }

    public Properties getPropertiesForApp() {
        return properties_app;
    }

    public Properties getPropertiesForWorkDB() {
        return properties_db_work;
    }

    public Properties getPropertiesForUsersDB() {
        return properties_db_users;
    }

    public void setProperties(final Properties properties) {
        rawProperties = properties;
    }
}
