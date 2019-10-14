package cornerstone.workflow.restapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class DBConfigurationParser {
    private static final Logger logger = LoggerFactory.getLogger(DBConfigurationParser.class);
    private static final String infoMessage = "[ {} ] <-- '{}' = '{}'";
    private static final String infoSecretMessage = "[ {} ] <-- '{}' = *****";
    private static final String errorMessage = "'{}' is not set";
    private static final String ignoreMessage = "'{}' is ignored";

    private Properties mainDB, userDB;

    public DBConfigurationParser(final Properties properties) {
       processProperties(properties);
    }

    public void processProperties(final Properties properties) {
        this.mainDB = new Properties();
        this.userDB = new Properties();

        for ( DBConfigurationField field : DBConfigurationField.values()) {
            String key = field.getKey();

            if ( key.startsWith(DBConfigurationField.mainPrefix) || key.startsWith(DBConfigurationField.userPrefix)) {
                String value = properties.getProperty(key);

                if ( value != null && ! value.isEmpty()) {
                    if ( key.startsWith(DBConfigurationField.mainPrefix)) {
                        this.mainDB.setProperty(key, value);
                        if ( ! key.contains("password")) {
                            logger.info(infoMessage, DBConfigurationField.mainPrefix, key, value);
                        } else {
                            logger.info(infoSecretMessage, DBConfigurationField.mainPrefix, key);
                        }
                    }
                    else if ( key.startsWith(DBConfigurationField.userPrefix)) {
                        this.userDB.setProperty(key, value);
                        if ( ! key.contains("password")) {
                            logger.info(infoMessage, DBConfigurationField.userPrefix, key, value);
                        } else {
                            logger.info(infoSecretMessage, DBConfigurationField.userPrefix, key);
                        }
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

    public Properties getMainDB() {
        return new Properties(mainDB);
    }

    public Properties getUserDB() {
        return new Properties(userDB);
    }
}
