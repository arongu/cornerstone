package cornerstone.webapp.configuration;

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
    public void constructor_shouldThrowIOException_whenFilesDoNotExist() {
        assertThrows(IOException.class, () -> new ConfigurationLoader("xxx", "xxx"));
    }

    @Test
    public void constructorAndLoadAndDecryptConfig_shouldLoadConfig_whenAllSet() throws IOException {
        final ConfigurationLoader cr = new ConfigurationLoader(keyFile, confFile);

        cr.loadAndDecryptConfig();

        assertEquals(keyFile, cr.getKeyFile());
        assertEquals(confFile, cr.getConfFile());
    }
}
