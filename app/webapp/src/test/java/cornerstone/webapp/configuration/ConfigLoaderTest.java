package cornerstone.webapp.configuration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigLoaderTest {
    @Test
    public void constructor_shouldThrowIOException_whenFilesDoNotExist() {
        assertThrows(IOException.class, () -> new ConfigLoader("xxx", "xxx"));
    }

    @Test
    public void constructorAndLoadAndDecryptConfig_shouldLoadConfig_whenAllSet() throws Exception {
        final String CONFIG_DIR = System.getenv("CONFIG_DIR");

        // throw exception if 'CONFIG_DIR' environment variable is not set
        if ( CONFIG_DIR != null && ! CONFIG_DIR.isEmpty() ) {
            final String keyFile  = Paths.get(CONFIG_DIR + "key.conf").toAbsolutePath().normalize().toString();
            final String confFile = Paths.get(CONFIG_DIR + "app.conf").toAbsolutePath().normalize().toString();

            final ConfigLoader cr = new ConfigLoader(keyFile, confFile);

            cr.loadAndDecryptConfig();

            assertEquals(keyFile, cr.getKeyFile());
            assertEquals(confFile, cr.getConfFile());

        } else {
            throw new RuntimeException("Environment variable 'CONFIG_DIR' is not set for the test! to set it: export CONFIG_DIR=/home/aron/projects/cornerstone/_test_config/");
        }
    }
}
