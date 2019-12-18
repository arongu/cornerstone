package cornerstone.workflow.app.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ConfigurationParser {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationParser.class);
    private static final String errorMessage = "'{}' is not set";
    private static final String ignoreMessage = "'{}' is ignored";

    private Properties db_main_properties, db_account_properties, app_properties;

    public ConfigurationParser(final Properties properties) {
       processProperties(properties);
    }

    private void addWithLog(final Properties properties, final String key, final String value, final String messagePrefix) {
        properties.setProperty(key, value);
        if ( ! key.contains("password")) {
            logger.info("[ {} ] <-- '{}' = '{}'", messagePrefix, key, value);
        } else {
            logger.info("[ {} ] <-- '{}' = *****", messagePrefix, key);
        }
    }

    public void processProperties(final Properties properties) {
        this.app_properties = new Properties();
        this.db_main_properties = new Properties();
        this.db_account_properties = new Properties();

        for ( ConfigurationField field : ConfigurationField.values()) {
            final String key = field.getKey();

            if ( key.startsWith(ConfigurationField.mainPrefix) || key.startsWith(ConfigurationField.userPrefix) || key.startsWith(ConfigurationField.appPrefix)) {
                final String value = properties.getProperty(key);

                if ( null != value && ! value.isEmpty()) {
                    if ( key.startsWith(ConfigurationField.mainPrefix)) {
                        addWithLog(this.db_main_properties, key, value, ConfigurationField.mainPrefix);
                    }
                    else if ( key.startsWith(ConfigurationField.userPrefix)) {
                        addWithLog(this.db_account_properties, key, value, ConfigurationField.userPrefix);
                    }
                    else if ( key.startsWith(ConfigurationField.appPrefix)) {
                        addWithLog(this.app_properties, key, value, ConfigurationField.appPrefix);
                    }
                }
                else {
                    logger.error(errorMessage, key);
                }
            }
            else {
                logger.info(ignoreMessage, key);
            }
        }
    }

    public Properties get_app_properties() {
        return new Properties(app_properties);
    }

    public Properties get_db_main_properties() {
        return new Properties(db_main_properties);
    }

    public Properties get_db_account_properties() {
        return new Properties(db_account_properties);
    }
}
