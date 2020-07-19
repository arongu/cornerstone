package cornerstone.workflow.webapp.configuration;

import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.configuration.ConfigurationLoaderException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigurationLoaderTest {
    private static final String TEST_CONFIG_DIR = "../../_test_config/";

    private static final String confPath = Paths.get(TEST_CONFIG_DIR + "app.conf").toAbsolutePath().normalize().toString();
    private static final String keyPath = Paths.get(TEST_CONFIG_DIR + "key.conf").toAbsolutePath().normalize().toString();

    @AfterAll
    public static void unsetProperties() {
        System.clearProperty(ConfigurationLoader.SYSTEM_PROPERTY_KEY_FILE);
        System.clearProperty(ConfigurationLoader.SYSTEM_PROPERTY_CONF_FILE);
    }

    @Test
    public void loadAndDecryptConfig_shouldThrowConfigurationLoaderException_whenEnvironmentIsNotSet() throws IOException, ConfigurationLoaderException {
        unsetProperties();
        final ConfigurationLoader cr = new ConfigurationLoader();

        assertThrows(ConfigurationLoaderException.class, cr::loadAndDecryptConfig);
    }

    @Test
    public void loadAndDecryptConfig_shouldLoadConfig_whenSystemPropertiesAreSet() throws IOException, ConfigurationLoaderException {
        final ConfigurationLoader cr = new ConfigurationLoader();
        System.setProperty(ConfigurationLoader.SYSTEM_PROPERTY_KEY_FILE, keyPath);
        System.setProperty(ConfigurationLoader.SYSTEM_PROPERTY_CONF_FILE, confPath);

        cr.loadAndDecryptConfig();

        assertEquals(keyPath, cr.getKeyFile());
        assertEquals(confPath, cr.getConfFile());
    }
}
