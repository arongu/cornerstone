package cornerstone.webapp.configuration;

import cornerstone.lib.config.ConfigEncryptDecrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    private Properties db_users_properties;
    private Properties db_work_properties;
    private Properties app_properties;
    private String keyFile;
    private String confFile;

    /**
     * Loads the configuration when the application is started.
     * @param keyFile path of the decryption key file
     * @param confFile path of the encrypted configuration file
     */
    public ConfigLoader(final String keyFile, final String confFile) throws IOException {
        this.keyFile = keyFile;
        this.confFile = confFile;
        loadAndDecryptConfig();
    }

    /**
     * Opens both the keyFile and confFile and decrypts it, then loads the decrypted data into separate Properties.
     * db_user_properties -- properties of "user" db, which only handles user related data
     * db_work_properties -- properties of "work" db, which only handles application related data
     * app_properties     -- properties of cornerstone itself (e.g.: rotation interval, node name etc.)
     * @throws IOException when the underlying ConfigEncryptDecrypt files to load the keyfile or fails to decrypt the config file.
     */
    public void loadAndDecryptConfig() throws IOException {
        final SecretKey secretKey   = ConfigEncryptDecrypt.loadAESKeyFromFile(keyFile);
        final Properties properties = ConfigEncryptDecrypt.decryptConfig(secretKey, confFile);

        try {
            final ConfigSorter sorter = new ConfigSorter(properties);
            sorter.sortProperties();

            db_users_properties = sorter.getPropertiesForUsersDB();
            db_work_properties  = sorter.getPropertiesForWorkDB();
            app_properties      = sorter.getPropertiesForApp();

        } catch (final ConfigSorterException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * @param keyFile Path of the key file to be used for decrypting the configuration file.
     */
    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    /**
     * @param confFile Path of the configuration file.
     */
    public void setConfFile(String confFile) {
        this.confFile = confFile;
    }

    /**
     * @return Path of the key file used for decrypting the configuration file.
     */
    public String getKeyFile() {
        return keyFile;
    }

    /**
     * @return Path of the configuration file used for starting the app.
     */
    public String getConfFile() {
        return confFile;
    }

    /**
     * @return Application related properties parsed from the configuration file.
     */
    public Properties getAppProperties() {
        return app_properties;
    }

    /**
     * @return "work" database related properties parsed from the configuration file.
     */
    public Properties getWorkDbProperties() {
        return db_work_properties;
    }

    /**
     * @return "user" database related properties parsed from the configuration file.
     */
    public Properties getUsersDbProperties() {
        return db_users_properties;
    }
}
