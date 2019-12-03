package cornerstone.workflow.app.configuration;

import cornerstone.workflow.lib.config.ConfigEncryptDecrypt;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationProvider {
    private Properties properties, main_db_properties, account_db_properties;

    public ConfigurationProvider() throws IOException {
        loadConfig();
        final DBConfigurationParser dbConfigurationParser = new DBConfigurationParser(properties);
        main_db_properties = dbConfigurationParser.getMainDB();
        account_db_properties = dbConfigurationParser.getAccountDB();
    }

    public void loadConfig() throws IOException {
        final SecretKey key = ConfigEncryptDecrypt.loadAESKeyFromFile("/var/opt/cornerstone/key.conf");
        this.properties = ConfigEncryptDecrypt.decryptConfig(key, "/var/opt/cornerstone/app.conf");
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
