package cornerstone.workflow.app.configuration;

import cornerstone.workflow.app.configuration.enums.ConfigFieldsApp;
import cornerstone.workflow.app.configuration.enums.ConfigFieldsDbAccount;
import cornerstone.workflow.app.configuration.enums.ConfigFieldsDbData;
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

    /**
     * Adds key value to Properties object, with logging.
     */
    private void addAndLog(final Properties properties,
                           final String key,
                           final String value,
                           final String logPrefix) {

        if ( key != null && !key.isEmpty() &&
             value != null && !value.isEmpty() ) {

            properties.setProperty(key, value);

            if ( key.contains("password") ||
                 key.contains("key") ) {

                logger.info("[ {} ] <-- '{}' = *****", logPrefix, key);

            } else {
                logger.info("[ {} ] <-- '{}' = '{}'", logPrefix, key, value);
            }

        } else {
            logger.error(errorMessage, key);
        }
    }

    public void processProperties(final Properties properties) {

        // get db_data_fields
        {
            db_data_properties = new Properties();

            for ( final ConfigFieldsDbData field : ConfigFieldsDbData.values() ) {
                if ( null != properties.get(field.key) ) {
                    addAndLog(
                            db_data_properties,
                            field.key,
                            properties.getProperty(field.key),
                            ConfigFieldsDbData.prefix_db_main
                    );

                } else {
                    logger.info(ignoreMessage, field.key);
                }
            }
        }

        // get db_account_fields
        {
            db_account_properties = new Properties();

            for ( final ConfigFieldsDbAccount field : ConfigFieldsDbAccount.values() ) {
                if ( null != properties.get(field.key) ) {
                    addAndLog(
                            db_account_properties,
                            field.key,
                            properties.getProperty(field.key),
                            ConfigFieldsDbAccount.prefix_db_account
                    );

                } else {
                    logger.info(ignoreMessage, field.key);
                }
            }
        }

        // get app_account_fields
        {
            app_properties = new Properties();

            for ( final ConfigFieldsApp field : ConfigFieldsApp.values() ) {
                if ( null != properties.get(field.key) ) {
                    addAndLog(
                            app_properties,
                            field.key,
                            properties.getProperty(field.key),
                            ConfigFieldsApp.prefix_app
                    );

                } else {
                    logger.info(ignoreMessage, field.key);
                }
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
