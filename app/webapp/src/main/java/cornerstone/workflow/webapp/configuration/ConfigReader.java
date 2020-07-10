package cornerstone.workflow.webapp.configuration;

import cornerstone.workflow.lib.config.ConfigEncryptDecrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigReader.class);
    // Environment variables
    public static final String SYSTEM_PROPERTY_CONF_FILE = "CONF_FILE";
    public static final String SYSTEM_PROPERTY_KEY_FILE = "KEY_FILE";
    // Logging
    private static final String LOG_MESSAGE_PROPERTY_SET     = "[ System.getProperty ][ '{}' ] = '{}'";
    private static final String LOG_MESSAGE_PROPERTY_NOT_SET = "[ System.getProperty ][ '{}' ] is not set!";

    private Properties properties;
    private Properties db_users_properties;
    private Properties db_work_properties;

    private String keyFile;
    private String confFile;

    public ConfigReader() {
    }

    private void setupKeyFile() {
        keyFile = System.getProperty(SYSTEM_PROPERTY_KEY_FILE);
        if ( null != keyFile) {
            logger.info(LOG_MESSAGE_PROPERTY_SET, SYSTEM_PROPERTY_KEY_FILE, keyFile);

        } else {
            logger.error(LOG_MESSAGE_PROPERTY_NOT_SET, SYSTEM_PROPERTY_KEY_FILE);
            throw new RuntimeException("Encryption key file is not set!");
        }
    }

    private void setupConfFile() {
        confFile = System.getProperty(SYSTEM_PROPERTY_CONF_FILE);
        if ( null != confFile) {
            logger.info(LOG_MESSAGE_PROPERTY_SET, SYSTEM_PROPERTY_CONF_FILE, confFile);

        } else {
            logger.error(LOG_MESSAGE_PROPERTY_NOT_SET, SYSTEM_PROPERTY_CONF_FILE);
            throw new RuntimeException("Conf file is not set!");
        }
    }

    public void loadConfig() throws IOException {
        setupKeyFile();
        setupConfFile();

        final SecretKey key = ConfigEncryptDecrypt.loadAESKeyFromFile(keyFile);
        properties = ConfigEncryptDecrypt.decryptConfig(key, confFile);

        final ConfigSorter cs = new ConfigSorter(properties);
        db_users_properties = cs.getDbUsersProperties();
        db_work_properties = cs.getDbWorkProperties();
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

    public Properties getWorkDbProperties() {
        return db_work_properties;
    }

    public Properties getUsersDbProperties() {
        return db_users_properties;
    }
}
