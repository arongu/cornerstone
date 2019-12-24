package cornerstone.workflow.lib.crypto;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AESEncryptDecrypt_derive256BitKeyTests {
    @Test
    @DisplayName("derive256bitKey - password and salt is null test")
    public void derive256bitKey_shouldThrowException_whenPasswordAndSaltIsNull_then() {
        assertThrows(AESEncryptDecrypt.AESToolException.class, () -> {
            AESEncryptDecrypt.derive256BitKey(null, null);
        });
    }

    @Test
    @DisplayName("derive256bitKey - salt is null test")
    public void derive256bitKey_shouldThrowException_whenSaltIsNull() {
        assertThrows(AESEncryptDecrypt.AESToolException.class, () -> {
            AESEncryptDecrypt.derive256BitKey("password", null);
        });
    }

    @Test
    @DisplayName("derive256bitKey - password is null, salt is too short test")
    public void derive256bitKey_shouldThrowException_whenPasswordIsNullSaltIsShort() {
        assertThrows(AESEncryptDecrypt.AESToolException.class, () -> {
            AESEncryptDecrypt.derive256BitKey(null, "short");
        });
    }

    @Test
    @DisplayName("derive256bitKey - password, salt is too long")
    public void derive256bitKey_shouldThrowException_whenSaltIsTooLong() {
        assertThrows(AESEncryptDecrypt.AESToolException.class, () -> {
            AESEncryptDecrypt.derive256BitKey("password", "abcdef0123456789-");
        });
    }

    @Test
    @DisplayName("derive256bitKey - password is empty string")
    public void derive256bitKey_shouldThrowException_whenPasswordIsEmptyString() {
        assertThrows(AESEncryptDecrypt.AESToolException.class, () -> {
            AESEncryptDecrypt.derive256BitKey("", "abcdef0123456789");
        });
    }

    @Test
    @DisplayName("derive256BitKey - Java,GO key derivation comparision test")
    public void derive256bitKey_testAgainstGo() throws AESEncryptDecrypt.AESToolException, DecoderException {
        byte[] expected = Hex.decodeHex("1D9E9E7E2BD8B7840124A79F4D486ECD81BD53E2511DA83BEE3F3642A5C7A0AD".toCharArray());
        SecretKeySpec key = AESEncryptDecrypt.derive256BitKey("password", "abcdef0123456789");

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
}
