package cornerstone.workflow.app.configuration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurationProviderTest {

    private static final String dev_files_dir = "../../_dev_files/test_config/";
    private static final String confPath  = Paths.get(dev_files_dir + "app.conf").toAbsolutePath().normalize().toString();
    private static final String keyPath = Paths.get(dev_files_dir + "key.conf").toAbsolutePath().normalize().toString();

    @AfterAll
    public static void unset() {
        System.clearProperty(ConfigurationProvider.SYSTEM_PROPERTY_KEY_FILE);
        System.clearProperty(ConfigurationProvider.SYSTEM_PROPERTY_CONF_FILE);
    }


    @Test
    public void getSystemProperties_shouldFallBackToDefault_whenNotSet() throws IOException {
        final ConfigurationProvider cp = new ConfigurationProvider();
        unset();

        cp.loadConfig();

        assertEquals(ConfigurationProvider.PATH_DEFAULT_KEY_FILE, cp.getKeyFile());
        assertEquals(ConfigurationProvider.PATH_DEFAULT_CONF_FILE, cp.getConfFile());
    }


    @Test
    public void getSystemProperties_shouldUseSystemProperties_whenSet() throws IOException {
        final ConfigurationProvider cp = new ConfigurationProvider();
        System.setProperty(ConfigurationProvider.SYSTEM_PROPERTY_KEY_FILE, keyPath);
        System.setProperty(ConfigurationProvider.SYSTEM_PROPERTY_CONF_FILE, confPath);

        cp.loadConfig();

        assertEquals(keyPath, cp.getKeyFile());
        assertEquals(confPath, cp.getConfFile());
    }
}
