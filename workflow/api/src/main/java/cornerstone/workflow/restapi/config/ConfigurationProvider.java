package cornerstone.workflow.restapi.config;

import cornerstone.workflow.lib.config.ConfigEncryptDecrypt;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationProvider {
    private Properties properties, mainDBproperties, adminDBproperties;

    public ConfigurationProvider() throws IOException {
        loadConfig();
        final DBConfigurationParser dbConfigurationParser = new DBConfigurationParser(properties);
        mainDBproperties = dbConfigurationParser.getMainDB();
        adminDBproperties = dbConfigurationParser.getUserDB();
    }

    public void loadConfig() throws IOException {
        final SecretKey key = ConfigEncryptDecrypt.loadAESKeyFromFile("/var/opt/cornerstone/key.conf");
        this.properties = ConfigEncryptDecrypt.decryptConfig(key, "/var/opt/cornerstone/app.conf");
    }

    public Properties getProperties() {
        return properties;
    }

    public Properties getMainDBproperties() {
        return mainDBproperties;
    }

    public Properties getAdminDBproperties() {
        return adminDBproperties;
    }
}
