package cornerstone.workflow.lib.crypto;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AESToolTest {
    @Test
    @DisplayName("derive256BitKey() exception tests")
    public void derive256bitKey_ThrowTest() {
        assertThrows(AESTool.AESToolException.class, () -> {
            AESTool.derive256BitKey(null, null);
        });
        assertThrows(AESTool.AESToolException.class, () -> {
            AESTool.derive256BitKey("password", null);
        });
        assertThrows(AESTool.AESToolException.class, () -> {
            AESTool.derive256BitKey(null, "short");
        });
        assertThrows(AESTool.AESToolException.class, () -> {
            AESTool.derive256BitKey("password", "abcdef0123456789-");
        });
        assertThrows(AESTool.AESToolException.class, () -> {
            AESTool.derive256BitKey("", "abcdef0123456789");
        });
    }

    @Test
    @DisplayName("derive256BitKey() Java, GO key derivation test")
    public void derive256bitKey_test_against_go() throws AESTool.AESToolException, DecoderException {
        byte[] expected = Hex.decodeHex("1D9E9E7E2BD8B7840124A79F4D486ECD81BD53E2511DA83BEE3F3642A5C7A0AD".toCharArray());
        SecretKeySpec key = AESTool.derive256BitKey("password", "abcdef0123456789");

        byte[] ba = key.getEncoded();
        for (int i = 0; i < ba.length; i++) {
            System.out.printf("%02X ", ba[i]);
        }
        System.out.println();

        for (int i = 0; i < expected.length; i++) {
            System.out.printf("%02X ", expected[i]);
        }
        System.out.println();

        assertArrayEquals(expected, ba);
    }

    @Test
    @DisplayName("encryptByteArray() -> decryptByteArray test")
    public void encryptDecryptTest() throws AESTool.AESToolException {
        final SecretKeySpec key = AESTool.derive256BitKey("password", "abcdef0123456789");
        final String text = "this is my super-secret text with ~!@#$%^&*()_+ all sort of characters";

        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = AESTool.encryptByteArray(key, data);
        byte[] decrypted = AESTool.decryptByteArray(key, encrypted);
        String str = new String(decrypted);

        System.out.println("text    (original) : " + text);
        System.out.println("text       (bytes) : " + Arrays.toString(data));
        System.out.println("encrypted  (bytes) : " + Arrays.toString(encrypted));
        System.out.println("decrypted  (bytes) : " + Arrays.toString(decrypted));
        System.out.println("decrypted (string) : " + str);

        assertArrayEquals(data, decrypted);
        assertEquals(text, str);
    }

    @Test
    @DisplayName("alma x5 encrypt() -> decrypt() test, IV should change")
    public void testIvChangeWhenBulk() throws AESTool.AESToolException {
        SecretKeySpec key = AESTool.derive256BitKey("password", "abcdef0123456789");
        byte[] ba = key.getEncoded();
        String base64key = Base64.getEncoder().encodeToString(ba);

        String text = "alma";
        List<String> lst = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            lst.add(text);
        }

        List<String> encryptedBase64List = AESTool.encrypt(base64key, lst);
        for (String b64 : encryptedBase64List) {
            System.out.println(b64);
        }

        List<String> decryptedStrings = AESTool.decrypt(base64key, encryptedBase64List);
        for (String s : decryptedStrings) {
            System.out.println(s);
            assertEquals(text, s);
        }
    }
}

