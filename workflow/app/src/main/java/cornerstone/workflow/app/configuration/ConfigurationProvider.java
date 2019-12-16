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
    static final String SYSTEM_PROPERTY_KEY_FILE = "keyFile";
    static final String SYSTEM_PROPERTY_CONF_FILE = "confFile";
    // Default paths
    static final String PATH_DEFAULT_KEY_FILE = "/var/opt/cornerstone/key.conf";
    static final String PATH_DEFAULT_CONF_FILE = "/var/opt/cornerstone/app.conf";
    // Logging
    private static final String LOG_MESSAGE_PROPERTY_SET = "[ System.getProperty ][ '{}' ] = '{}'";
    private static final String LOG_MESSAGE_PROPERTY_NOT_SET = "[ System.getProperty ][ '{}' ] is not set, using default value : '{}'";

    private Properties properties, main_db_properties, account_db_properties;
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
        this.properties = ConfigEncryptDecrypt.decryptConfig(key, confFile);

        final DBConfigurationParser dbConfigurationParser = new DBConfigurationParser(properties);
        this.main_db_properties = dbConfigurationParser.getMainDB();
        this.account_db_properties = dbConfigurationParser.getAccountDB();
    }

    public String getKeyFile() {
        return keyFile;
    }

    public String getConfFile() {
        return confFile;
    }

    public Properties getProperties() {
        return properties;
    }

    public Properties get_data_db_properties() {
        return main_db_properties;
    }

    public Properties get_account_db_properties() {
        return account_db_properties;
    }
}
