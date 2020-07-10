package cornerstone.workflow.webapp.configuration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigReaderTest {
    private static final String TEST_CONFIG_DIR = "../../_test_config/";

    private static final String confPath = Paths.get(TEST_CONFIG_DIR + "app.conf").toAbsolutePath().normalize().toString();
    private static final String keyPath  = Paths.get(TEST_CONFIG_DIR + "key.conf").toAbsolutePath().normalize().toString();

    @AfterAll
    public static void unsetProperties() {
        System.clearProperty(ConfigReader.SYSTEM_PROPERTY_KEY_FILE);
        System.clearProperty(ConfigReader.SYSTEM_PROPERTY_CONF_FILE);
    }


    @Test
    public void getSystemProperties_shouldFallBackToDefaultAndThrowIOException_whenNotSetAndDoesNotHavePermissionToOpenIt() {
        final ConfigReader cr = new ConfigReader();
        unsetProperties();

        final IOException e = assertThrows(IOException.class, cr::loadConfig);

        assertTrue(e.getMessage().contains("/var/opt/cornerstone/key.conf (Permission denied)"));
    }

//    @Test
//    public void getSystemProperties_shouldFallBackToDefault_whenNotSetAndHasTheRightPermissions() throws IOException {
//        final ConfigurationProvider cp = new ConfigurationProvider();
//        unset();
//
//        cp.loadConfig();
//
//        assertEquals(ConfigurationProvider.PATH_DEFAULT_KEY_FILE, cp.getKeyFile());
//        assertEquals(ConfigurationProvider.PATH_DEFAULT_CONF_FILE, cp.getConfFile());
//    }

    @Test
    public void getSystemProperties_shouldUseSystemProperties_whenSet() throws IOException {
        final ConfigReader cr = new ConfigReader();
        System.setProperty(ConfigReader.SYSTEM_PROPERTY_KEY_FILE, keyPath);
        System.setProperty(ConfigReader.SYSTEM_PROPERTY_CONF_FILE, confPath);

        cr.loadConfig();

        assertEquals(keyPath, cr.getKeyFile());
        assertEquals(confPath, cr.getConfFile());
    }
}
