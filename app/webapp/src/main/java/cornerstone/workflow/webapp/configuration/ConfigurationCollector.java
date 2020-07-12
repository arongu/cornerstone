package cornerstone.workflow.webapp.configuration;

import cornerstone.workflow.webapp.configuration.enums.APP_ENUM;
import cornerstone.workflow.webapp.configuration.enums.DB_USERS_ENUM;
import cornerstone.workflow.webapp.configuration.enums.DB_WORK_ENUM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ConfigurationCollector {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationCollector.class);
    private static final String errorMessage  = "'{}' is not set";
    private static final String ignoreMessage = "'{}' is ignored";

    private Properties properties_db_users;
    private Properties properties_db_work;
    private Properties properties_app;

    public ConfigurationCollector(final Properties properties) {
       collectProperties(properties);
    }

    /**
     * Logs and sets key-value pairs as a property.
     */
    private void addKeyValueAndLogIt(final Properties props, final String key, final String value, final String logPrefix) {
        if ( key != null && ! key.isEmpty() && value != null && ! value.isEmpty()) {
            props.setProperty(key, value);

            if ( key.contains("password") || key.contains("key")) {
                logger.info("[ {} ] <-- '{}' = *****", logPrefix, key);
            } else {
                logger.info("[ {} ] <-- '{}' = '{}'", logPrefix, key, value);
            }

        } else {
            logger.error(errorMessage, key);
        }
    }

    public void collectProperties(final Properties properties) {
        // WORK DB
        properties_db_work = new Properties();
        for ( final DB_WORK_ENUM work_enum : DB_WORK_ENUM.values()) {
            if ( null != properties.get(work_enum.key)) {
                addKeyValueAndLogIt(properties_db_work, work_enum.key, properties.getProperty(work_enum.key), DB_WORK_ENUM.PREFIX_DB_WORK);

            } else {
                logger.info(ignoreMessage, work_enum.key);
            }
        }

        // USERS DB
        properties_db_users = new Properties();
        for ( final DB_USERS_ENUM users_enum : DB_USERS_ENUM.values()) {
            if ( null != properties.get(users_enum.key)) {
                addKeyValueAndLogIt(properties_db_users, users_enum.key, properties.getProperty(users_enum.key), DB_USERS_ENUM.PREFIX_DB_USERS);

            } else {
                logger.info(ignoreMessage, users_enum.key);
            }
        }

        // APP
        properties_app = new Properties();
        for ( final APP_ENUM app_enum : APP_ENUM.values()) {
            if ( null != properties.get(app_enum.key)) {
                addKeyValueAndLogIt(properties_app, app_enum.key, properties.getProperty(app_enum.key), APP_ENUM.PREFIX_APP);

            } else {
                logger.info(ignoreMessage, app_enum.key);
            }
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
}
