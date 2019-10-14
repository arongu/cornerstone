package cornerstone.workflow.lib.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AESEncryptDecrypt_encryptDecryptBase64Tests {
    @Test
    @DisplayName("encryptStringsWithBase64KeyToBase64CipherTexts() -> decryptBase64CipherTextsWithBase64KeyToStrings()")
    public void testIVChange() throws AESEncryptDecrypt.AESToolException {
        final SecretKeySpec key = AESEncryptDecrypt.derive256BitKey("password", "abcdef0123456789");
        final byte[] ba = key.getEncoded();
        final String base64key = Base64.getEncoder().encodeToString(ba);

        String text = "alma";
        final List<String> lst = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            lst.add(text);
        }

        final List<String> encryptedBase64List = AESEncryptDecrypt.encryptStringsWithBase64KeyToBase64CipherTexts(base64key, lst);
        for (String b64 : encryptedBase64List) {
            System.out.println(b64);
        }

        final List<String> decryptedStrings = AESEncryptDecrypt.decryptBase64CipherTextsWithBase64KeyToStrings(base64key, encryptedBase64List);
        for (String s : decryptedStrings) {
            System.out.println(s);
            assertEquals(text, s);
        }
    }
}

