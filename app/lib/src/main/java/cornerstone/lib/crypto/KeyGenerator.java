package cornerstone.lib.crypto;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Base64.Encoder;

public class KeyGenerator {
    public static Key generateKey(final String password,
                           final String salt,
                           final int iterationCount,
                           final int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {

        final SecretKeyFactory factory  = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        final KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), iterationCount, keyLength);

        return factory.generateSecret(keySpec);
    }

    public static byte[] generateKeyAsBytes(final String password,
                                     final String salt,
                                     final int iterationCount,
                                     final int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {

        return  generateKey(password, salt, iterationCount, keyLength).getEncoded();
    }

    public static String generateKeyAsBase64(final String password,
                                     final String salt,
                                     final int iterationCount,
                                     final int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {

        final Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(generateKeyAsBytes(password, salt, iterationCount, keyLength));
    }
}
