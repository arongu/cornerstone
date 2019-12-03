package cornerstone.workflow.lib.config;

import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigEncryptDecryptTest {
    @Test
    @DisplayName("Open and load the key from file.")
    void loadKeyFileTest() throws IOException {
        final String originalKeyAsHex = "a3224844f478d92cf2c81cf262fddfa379c74fff91a17651df24c601cab6be4b";
        final URL keyFileUrl = getClass().getClassLoader().getResource("key.txt");

        final byte[] bytes = ConfigEncryptDecrypt.loadAESKeyFromFile(keyFileUrl.getPath()).getEncoded();
        final String loadedKeyAsHex = Hex.encodeHexString(bytes);

        assertEquals(originalKeyAsHex, loadedKeyAsHex);
    }
}
