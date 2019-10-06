package cornerstone.workflow.config;

import cornerstone.workflow.restapi.config.ConfigLoader;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO fix TCs
public class ConfigLoaderTest {
    private static final String hexString = "a3224844f478d92cf2c81cf262fddfa379c74fff91a17651df24c601cab6be4b";

    @Test
    @DisplayName("Key file load test")
    void loadKeyFileTest() throws IOException {
        byte[] ba = ConfigLoader.loadKeyFile("/home/aron/coreapi/key.txt");
        String hex = Hex.decodeHex(ba.);

        assertEquals(hexString, hex);
    }

    @Test
    @DisplayName("Decrypt config test")
    void loadEncryptedConfigTest() throws IOException {
        final byte[] keyBa = Hex.decode(hexString);
        SecretKey key = new SecretKeySpec(keyBa, "AES");
        Properties properties = ConfigLoader.loadEncryptedConfig(key, "/home/aron/coreapi/conf.txt");
    }
}
