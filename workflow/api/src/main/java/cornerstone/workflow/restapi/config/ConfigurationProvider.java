package cornerstone.workflow.restapi.config;

import cornerstone.workflow.lib.config.ConfigReaderWriter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationProvider {
    private static final String keyFile = "/home/aron/cc/key.txt";
    private static final String confFile = "/home/aron/cc/jcore/conf.txt";

    private Properties properties, mainDBproperties, adminDBproperties;

    public ConfigurationProvider() throws IOException {
        loadConfig();
        final DBConfigurationParser dbConfigurationParser = new DBConfigurationParser(properties);
        mainDBproperties = dbConfigurationParser.getMainDB();
        adminDBproperties = dbConfigurationParser.getAdminDB();
    }

    public void loadConfig() throws IOException {
        final SecretKey key = ConfigReaderWriter.loadAESKeyFromFile(keyFile);
        this.properties = ConfigReaderWriter.decryptConfig(key, confFile);
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
