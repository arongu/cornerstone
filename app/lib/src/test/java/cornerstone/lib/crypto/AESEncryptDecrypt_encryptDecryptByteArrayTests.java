package cornerstone.lib.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AESEncryptDecrypt_encryptDecryptByteArrayTests {

    @Test
    @DisplayName("encryptByteArrayWithKey() -> decryptCipherArrayWithKe() - byte[] encryption/decryption test")
    public void encryptByteArrayWithKeyTest() throws AESEncryptDecrypt.AESToolException {
        final SecretKeySpec key = AESEncryptDecrypt.derive256BitAESKeyWithHmacSHA256("password", "abcdef0123456789");
        final String text = "this is my super-secret text with ~!@#$%^&*()_+ all sort of characters";

        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = AESEncryptDecrypt.encryptByteArrayWithKey(key, data);
        byte[] decrypted = AESEncryptDecrypt.decryptCipherArrayWithKey(key, encrypted);
        String str = new String(decrypted);

        System.out.println("original  (string) : " + text);
        System.out.println("decrypted (string) : " + str);
        System.out.println("original   (bytes) : " + Arrays.toString(data));
        System.out.println("decrypted  (bytes) : " + Arrays.toString(decrypted));
        System.out.println("encrypted  (bytes) : " + Arrays.toString(encrypted));

        assertArrayEquals(data, decrypted);
        assertEquals(text, str);
    }

    @Test
    @DisplayName("encryptByteArraysWithKey() -> decryptCipherArraysWithKey() - List<[]byte> encryption/decryption test")
    public void encryptByteArraysWithKeyTest() throws AESEncryptDecrypt.AESToolException {
        final SecretKey key = AESEncryptDecrypt.derive256BitAESKeyWithHmacSHA256("mypassword", "abcdef0123456789");

        final List<byte[]> originalList = Arrays.asList( "alma".getBytes(), "repa".getBytes(), "kontos".getBytes(), "12".getBytes());
        final List<byte[]> encryptedList = AESEncryptDecrypt.encryptByteArraysWithKey(key, originalList);
        final List<byte[]> decryptedList = AESEncryptDecrypt.decryptCipherArraysWithKey(key, encryptedList);

        for ( int i = 0; i < encryptedList.size(); i++){
            assertArrayEquals(originalList.get(i), decryptedList.get(i));
        }
    }

    @Test
    @DisplayName("encryptStringWithBase64KeyToBase64CipherText() -> decryptBase64CipherTextWithBase64KeyToString()")
    public void encryptDecryptStringWithBase64KeyTest() throws IOException, AESEncryptDecrypt.AESToolException {
        final URL keyFileUrl = getClass().getClassLoader().getResource("key.txt");

        final String base64key;
        try (BufferedReader reader = new BufferedReader(new FileReader(keyFileUrl.getPath()))) {
            base64key = reader.readLine();
        }

        String toEncrypt = "ENcryptThisText1230~~~#";
        String encrypted = AESEncryptDecrypt.encryptStringWithBase64KeyToBase64CipherText(base64key, toEncrypt);
        String decrypted = AESEncryptDecrypt.decryptBase64CipherTextWithBase64KeyToString(base64key, encrypted);

        System.out.println(base64key);
        System.out.println(toEncrypt);
        System.out.println(encrypted);
        System.out.println(decrypted);
        assertEquals(toEncrypt, decrypted);
    }
}
