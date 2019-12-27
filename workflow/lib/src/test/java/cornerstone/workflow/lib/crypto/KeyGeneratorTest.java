package cornerstone.workflow.lib.crypto;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KeyGeneratorTest {
    @Test
    public void generateKeyAsBase64_shouldNotThrowException_whenCalled() throws NoSuchAlgorithmException, InvalidKeySpecException {
        final String password = "password";
        final String salt = "salt";

        final String key = KeyGenerator.generateKeyAsBase64(password, salt, 250000, 256);
        System.out.println(key);
        assertNotNull(key);
    }
}
