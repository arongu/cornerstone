package cornerstone.workflow.webapp.configuration;

import cornerstone.workflow.lib.config.ConfigEncryptDecrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);
    // Environment variables
    public static final String SYSTEM_PROPERTY_CONF_FILE = "CONF_FILE";
    public static final String SYSTEM_PROPERTY_KEY_FILE  = "KEY_FILE";
    // Logging
    private static final String LOG_MESSAGE_PROPERTY_SET     = "[ System.getProperty ][ '{}' ] = '{}'";
    private static final String LOG_MESSAGE_PROPERTY_NOT_SET = "[ System.getProperty ][ '{}' ] is not set!";

    private Properties properties;
    private Properties db_users_properties;
    private Properties db_work_properties;
    private Properties app_properties;

    private String keyFile;
    private String confFile;

    public ConfigurationLoader() {
    }

    private void setKeyFileFromEnv() throws ConfigurationLoaderException {
        keyFile = System.getProperty(SYSTEM_PROPERTY_KEY_FILE);
        if ( null != keyFile) {
            logger.info(LOG_MESSAGE_PROPERTY_SET, SYSTEM_PROPERTY_KEY_FILE, keyFile);

        } else {
            logger.error(LOG_MESSAGE_PROPERTY_NOT_SET, SYSTEM_PROPERTY_KEY_FILE);
            throw new ConfigurationLoaderException(SYSTEM_PROPERTY_KEY_FILE + " is not set! (environment variable)");
        }
    }

    private void setConfFileFromEnv() throws ConfigurationLoaderException {
        confFile = System.getProperty(SYSTEM_PROPERTY_CONF_FILE);
        if ( null != confFile) {
            logger.info(LOG_MESSAGE_PROPERTY_SET, SYSTEM_PROPERTY_CONF_FILE, confFile);

        } else {
            logger.error(LOG_MESSAGE_PROPERTY_NOT_SET, SYSTEM_PROPERTY_CONF_FILE);
            throw new ConfigurationLoaderException(SYSTEM_PROPERTY_CONF_FILE + " is not set! (environment variable)");
        }
    }

    public void loadAndDecryptConfig() throws ConfigurationLoaderException, IOException {
        setKeyFileFromEnv();
        setConfFileFromEnv();

        final SecretKey key = ConfigEncryptDecrypt.loadAESKeyFromFile(keyFile);
        properties = ConfigEncryptDecrypt.decryptConfig(key, confFile);

        final ConfigurationCollector collector = new ConfigurationCollector(properties);
        db_users_properties = collector.getPropertiesForUsersDB();
        db_work_properties = collector.getPropertiesForWorkDB();
        app_properties = collector.getPropertiesForApp();
    }

    public String getKeyFile() {
        return keyFile;
    }

    public String getConfFile() {
        return confFile;
    }

    public Properties getAllProperties() {
        return properties;
    }

    public Properties getApp_properties() {
        return app_properties;
    }

    public Properties getWorkDbProperties() {
        return db_work_properties;
    }

    public Properties getUsersDbProperties() {
        return db_users_properties;
    }
}
