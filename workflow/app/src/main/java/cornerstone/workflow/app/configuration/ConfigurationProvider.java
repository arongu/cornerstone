package cornerstone.workflow.app.configuration;

import cornerstone.workflow.lib.config.ConfigEncryptDecrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationProvider {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationProvider.class);
    // Properties to look for
    public static final String SYSTEM_PROPERTY_KEY_FILE = "keyFile";
    public static final String SYSTEM_PROPERTY_CONF_FILE = "confFile";
    // Default paths
    public static final String PATH_DEFAULT_KEY_FILE = "/var/opt/cornerstone/key.conf";
    public static final String PATH_DEFAULT_CONF_FILE = "/var/opt/cornerstone/app.conf";
    // Logging
    private static final String LOG_MESSAGE_PROPERTY_SET = "[ System.getProperty ][ '{}' ] = '{}'";
    private static final String LOG_MESSAGE_PROPERTY_NOT_SET = "[ System.getProperty ][ '{}' ] is not set, using default value : '{}'";

    private Properties raw_properties;
    private Properties app_properties;
    private Properties db_account_properties;
    private Properties db_main_properties;

    private String keyFile, confFile;

    public ConfigurationProvider() {
    }

    public void getSystemProperties() {
        if ( null != System.getProperty(SYSTEM_PROPERTY_KEY_FILE)) {
            this.keyFile = System.getProperty(SYSTEM_PROPERTY_KEY_FILE);
            logger.info(LOG_MESSAGE_PROPERTY_SET, SYSTEM_PROPERTY_KEY_FILE, keyFile);
        } else {
            this.keyFile = PATH_DEFAULT_KEY_FILE;
            logger.info(LOG_MESSAGE_PROPERTY_NOT_SET, SYSTEM_PROPERTY_KEY_FILE, keyFile);
        }

        if ( null != System.getProperty(SYSTEM_PROPERTY_CONF_FILE)) {
            this.confFile = System.getProperty(SYSTEM_PROPERTY_CONF_FILE);
            logger.info(LOG_MESSAGE_PROPERTY_SET, SYSTEM_PROPERTY_CONF_FILE, confFile);
        } else {
            this.confFile = PATH_DEFAULT_CONF_FILE;
            logger.info(LOG_MESSAGE_PROPERTY_NOT_SET, SYSTEM_PROPERTY_CONF_FILE, confFile);
        }
    }

    public void loadConfig() throws IOException {
        getSystemProperties();
        final SecretKey key = ConfigEncryptDecrypt.loadAESKeyFromFile(keyFile);
        raw_properties = ConfigEncryptDecrypt.decryptConfig(key, confFile);

        final ConfigurationParser cp = new ConfigurationParser(raw_properties);
        app_properties = cp.get_app_properties();
        db_account_properties = cp.get_db_account_properties();
        db_main_properties = cp.get_db_main_properties();
    }

    public String getKeyFile() {
        return keyFile;
    }

    public String getConfFile() {
        return confFile;
    }

    public Properties getRaw_properties() {
        return raw_properties;
    }

    public Properties get_data_db_properties() {
        return db_main_properties;
    }

    public Properties get_account_db_properties() {
        return db_account_properties;
    }

    public Properties get_app_properties() {
        return app_properties;
    }
}
