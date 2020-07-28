package cornerstone.webapp.configuration;

import cornerstone.lib.config.ConfigEncryptDecrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);

    private Properties db_users_properties;
    private Properties db_work_properties;
    private Properties app_properties;
    private String keyFile;
    private String confFile;

    public ConfigurationLoader(final String keyFile, final String confFile) throws IOException {
        this.keyFile = keyFile;
        this.confFile = confFile;
        loadAndDecryptConfig();
    }

    public void loadAndDecryptConfig() throws IOException {
        final SecretKey secretKey   = ConfigEncryptDecrypt.loadAESKeyFromFile(keyFile);
        final Properties properties = ConfigEncryptDecrypt.decryptConfig(secretKey, confFile);

        try {
            final ConfigurationSorter sorter = new ConfigurationSorter(properties);
            sorter.sortProperties();

            db_users_properties = sorter.getPropertiesForUsersDB();
            db_work_properties  = sorter.getPropertiesForWorkDB();
            app_properties      = sorter.getPropertiesForApp();

        } catch (final ConfigurationSorterException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public void setConfFile(String confFile) {
        this.confFile = confFile;
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
