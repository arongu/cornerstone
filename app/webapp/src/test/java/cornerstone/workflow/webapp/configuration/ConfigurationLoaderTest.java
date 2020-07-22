package cornerstone.workflow.webapp.configuration;

import cornerstone.webapp.configuration.ConfigurationLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigurationLoaderTest {
    private static final String TEST_CONFIG_DIR = "../../_test_config/";

    private static final String keyFile  = Paths.get(TEST_CONFIG_DIR + "key.conf").toAbsolutePath().normalize().toString();
    private static final String confFile = Paths.get(TEST_CONFIG_DIR + "app.conf").toAbsolutePath().normalize().toString();

    @Test
    public void loadAndDecryptConfig_shouldThrowIOException_whenFilesDoNotExist() throws IOException {
        final ConfigurationLoader cr = new ConfigurationLoader("xxx", "xxx");

        assertThrows(IOException.class, cr::loadAndDecryptConfig);
    }

    @Test
    public void loadAndDecryptConfig_shouldLoadConfig_whenAllSet() throws IOException {
        final ConfigurationLoader cr = new ConfigurationLoader(keyFile, confFile);

        cr.loadAndDecryptConfig();

        assertEquals(keyFile, cr.getKeyFile());
        assertEquals(confFile, cr.getConfFile());
    }
}
