package cornerstone.workflow.app.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurationProviderTest {
    @Test
    public void getSystemProperties() {
        final String keyPath = "/home/aron/.corner/key.conf";
        final String confPath = "/home/aron/.corner/app.conf";

        final ConfigurationProvider cp = new ConfigurationProvider();

        // Should return what is set
        System.setProperty(ConfigurationProvider.SYSTEM_PROPERTY_KEY_FILE, keyPath);
        System.setProperty(ConfigurationProvider.SYSTEM_PROPERTY_CONF_FILE, confPath);

        cp.getSystemProperties();

        assertEquals(keyPath, cp.getKeyFile());
        assertEquals(confPath, cp.getConfFile());

        // Should fall back to default if not set
        System.clearProperty(ConfigurationProvider.SYSTEM_PROPERTY_KEY_FILE);
        System.clearProperty(ConfigurationProvider.SYSTEM_PROPERTY_CONF_FILE);

        cp.getSystemProperties();

        assertEquals(ConfigurationProvider.PATH_DEFAULT_KEY_FILE, cp.getKeyFile());
        assertEquals(ConfigurationProvider.PATH_DEFAULT_CONF_FILE, cp.getConfFile());
    }
}
