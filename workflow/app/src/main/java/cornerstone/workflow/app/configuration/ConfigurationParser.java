package cornerstone.workflow.app.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ConfigurationParser {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationParser.class);
    private static final String errorMessage = "'{}' is not set";
    private static final String ignoreMessage = "'{}' is ignored";

    private Properties db_account_properties;
    private Properties db_data_properties;
    private Properties app_properties;

    public ConfigurationParser(final Properties properties) {
       processProperties(properties);
    }

    private void addWithLog(final Properties properties, final String key, final String value, final String messagePrefix) {
        if ( null != value && ! value.isEmpty() ) {
            properties.setProperty(key, value);

            if ( ! (key.contains("password") || key.contains("key"))) {
                logger.info("[ {} ] <-- '{}' = '{}'", messagePrefix, key, value);
            } else {
                logger.info("[ {} ] <-- '{}' = *****", messagePrefix, key);
            }

        } else {
            logger.error(errorMessage, key);
        }
    }

    public void processProperties(final Properties properties) {
        app_properties = new Properties();
        db_account_properties = new Properties();
        db_data_properties = new Properties();

        for (ConfigurationField field : ConfigurationField.values()) {
            final String key = field.getKey();

            if ( ConfigurationField.DB_ACCOUNT_DRIVER == field ||
                    ConfigurationField.DB_ACCOUNT_URL == field ||
                    ConfigurationField.DB_ACCOUNT_USER == field ||
                    ConfigurationField.DB_ACCOUNT_PASSWORD == field ||
                    ConfigurationField.DB_ACCOUNT_MIN_IDLE == field ||
                    ConfigurationField.DB_ACCOUNT_MAX_IDLE == field ||
                    ConfigurationField.DB_ACCOUNT_MAX_OPEN == field) {

                final String value = properties.getProperty(key);
                addWithLog(db_account_properties, key, value, ConfigurationField.account_db_prefix);

            } else if (ConfigurationField.DB_DATA_DRIVER == field ||
                    ConfigurationField.DB_DATA_URL == field ||
                    ConfigurationField.DB_DATA_USER == field ||
                    ConfigurationField.DB_DATA_PASSWORD == field ||
                    ConfigurationField.DB_DATA_MIN_IDLE == field ||
                    ConfigurationField.DB_DATA_MAX_IDLE == field ||
                    ConfigurationField.DB_DATA_MAX_OPEN == field){

                final String value = properties.getProperty(key);
                addWithLog(db_data_properties, key, value, ConfigurationField.db_main_prefix);

            } else if (ConfigurationField.APP_ADMIN_USER == field || ConfigurationField.APP_JWS_KEY == field) {

                final String value = properties.getProperty(key);
                addWithLog(app_properties, key, value, "app");

            } else {
                logger.info(ignoreMessage, key);
            }
        }
    }

    public Properties get_app_properties() {
        return app_properties;
    }

    public Properties get_db_data_properties() {
        return db_data_properties;
    }

    public Properties get_db_account_properties() {
        return db_account_properties;
    }
}
