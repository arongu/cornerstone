package cornerstone.workflow.lib.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Base64;

public class AESGenerateKeyTest {
    @Test
    @DisplayName("Generate AES key as base64string")
    public void generateAESKey() throws AESEncryptDecrypt.AESToolException {
        final String password = "password";
        final String salt = "0123456789abcdef";

        final Key key = AESEncryptDecrypt.derive256BitKey(password, salt);
        final String stringKey = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println(stringKey);
    }
}
