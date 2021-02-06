package cornerstone.lib.crypto;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Base64.Encoder;

/**
 * Class to generate secret keys for encryption using password, salt and iteration count.
 * Similar to what is inside AESEncryptDecrypt class.
 */
public class KeyGenerator {
    /**
     * Generates a new secret key.
     * @param password Password.
     * @param salt Salt for password.
     * @param iterationCount Number of iterations to be used.
     * @param keyLength Length of the key.
     * @return Encryption key.
     * @throws NoSuchAlgorithmException Thrown when 'PBKDF2WithHmacSHA256' does not exist. Should never happen.
     * @throws InvalidKeySpecException Thrown when the created secret key is invalid. Check byte lengths.
     */
    public static Key generateKey(final String password,
                                  final String salt,
                                  final int iterationCount,
                                  final int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {

        final SecretKeyFactory factory  = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        final KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), iterationCount, keyLength);

        return factory.generateSecret(keySpec);
    }

    /**
     * Generates a new secret key as a byte array.
     * @param password Password.
     * @param salt Salt for password.
     * @param iterationCount Number of iterations to be used.
     * @param keyLength Length of the key.
     * @return Encryption key as byte array.
     * @throws NoSuchAlgorithmException Thrown when 'PBKDF2WithHmacSHA256' does not exist. Should never happen.
     * @throws InvalidKeySpecException Thrown when the created secret key is invalid. Check byte lengths.
     */
    public static byte[] generateKeyAsBytes(final String password,
                                            final String salt,
                                            final int iterationCount,
                                            final int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {

        return  generateKey(password, salt, iterationCount, keyLength).getEncoded();
    }

    /**
     * Generates a new secret key as a bas64 string.
     * @param password Password.
     * @param salt Salt for password.
     * @param iterationCount Number of iterations to be used.
     * @param keyLength Length of the key.
     * @return Encryption key as base64 string.
     * @throws NoSuchAlgorithmException Thrown when 'PBKDF2WithHmacSHA256' does not exist. Should never happen.
     * @throws InvalidKeySpecException Thrown when the created secret key is invalid. Check byte lengths.
     */
    public static String generateKeyAsBase64(final String password,
                                             final String salt,
                                             final int iterationCount,
                                             final int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {

        final Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(generateKeyAsBytes(password, salt, iterationCount, keyLength));
    }
}
