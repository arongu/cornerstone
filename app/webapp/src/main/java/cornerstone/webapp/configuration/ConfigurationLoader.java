package cornerstone.webapp.configuration;

import cornerstone.lib.config.ConfigEncryptDecrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationLoader {
    public static final String SYSTEM_PROPERTY_KEY_FILE  = "KEY_FILE";
    public static final String SYSTEM_PROPERTY_CONF_FILE = "CONF_FILE";
    public static final String DEFAULT_CONF_FILE = "/opt/cornerstone/app.conf";
    public static final String DEFAULT_KEY_FILE  = "/opt/cornerstone/key.conf";

    private static final String LOG_MESSAGE_PROPERTY_SET                        = "[ System.getProperty ][ '{}' ] = '{}'";
    private static final String LOG_MESSAGE_PROPERTY_NOT_SET                    = "[ System.getProperty ][ '{}' ] is not set!";
    private static final String LOG_MESSAGE_PROPERTY_FALL_BACK_TO_DEFAULT_VALUE = "Fall back to default value for [ '{}' ] = '{}'";

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);
    private Properties db_users_properties;
    private Properties db_work_properties;
    private Properties app_properties;

    private String keyFile;
    private String confFile;

    public ConfigurationLoader() throws IOException {
        setKeyFileFromEnv();
        setConfFileFromEnv();
        loadAndDecryptConfig();
    }

    private void setKeyFileFromEnv() {
        keyFile = System.getProperty(SYSTEM_PROPERTY_KEY_FILE);
        if (null != keyFile) {
            logger.info(LOG_MESSAGE_PROPERTY_SET, SYSTEM_PROPERTY_KEY_FILE, keyFile);
        } else {
            keyFile = DEFAULT_KEY_FILE;
            logger.error(LOG_MESSAGE_PROPERTY_NOT_SET, SYSTEM_PROPERTY_KEY_FILE);
            logger.info(LOG_MESSAGE_PROPERTY_FALL_BACK_TO_DEFAULT_VALUE, SYSTEM_PROPERTY_KEY_FILE, keyFile);
        }
    }

    private void setConfFileFromEnv() {
        confFile = System.getProperty(SYSTEM_PROPERTY_CONF_FILE);
        if (null != confFile) {
            logger.info(LOG_MESSAGE_PROPERTY_SET, SYSTEM_PROPERTY_CONF_FILE, confFile);
        } else {
            confFile = DEFAULT_CONF_FILE;
            logger.error(LOG_MESSAGE_PROPERTY_NOT_SET, SYSTEM_PROPERTY_CONF_FILE);
            logger.info(LOG_MESSAGE_PROPERTY_FALL_BACK_TO_DEFAULT_VALUE, SYSTEM_PROPERTY_CONF_FILE, confFile);
        }
    }

    public void loadAndDecryptConfig() throws IOException {
        final SecretKey key = ConfigEncryptDecrypt.loadAESKeyFromFile(keyFile);
        final Properties properties = ConfigEncryptDecrypt.decryptConfig(key, confFile);

        try {
            final ConfigurationSorter collector = new ConfigurationSorter(properties);
            db_users_properties = collector.getPropertiesForUsersDB();
            db_work_properties = collector.getPropertiesForWorkDB();
            app_properties = collector.getPropertiesForApp();

        } catch (final ConfigurationSorterException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getKeyFile() {
        return keyFile;
    }

    public String getConfFile() {
        return confFile;
    }

    public Properties getAppProperties() {
        return app_properties;
    }

    public Properties getWorkDbProperties() {
        return db_work_properties;
    }

    public Properties getUsersDbProperties() {
        return db_users_properties;
    }
}
