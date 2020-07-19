package cornerstone.lib.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

public final class AESEncryptDecrypt {
    private static final Logger logger = LoggerFactory.getLogger(AESEncryptDecrypt.class);

    public static class AESToolException extends Exception {
        AESToolException(final String message) {
            super(message);
        }
    }

    public static SecretKeySpec derive256BitAESKeyWithHmacSHA256(final String password, final String salt) throws AESToolException {

        if ( null == password ||
             null == salt ||
             0 == password.getBytes().length ||
             16 != salt.getBytes().length) {

            throw new AESToolException("Password and salt cannot be null or empty, salt must be 16 bytes long!");

        } else {
            try {
                final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                final KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 250000, 256);
                final SecretKey key = factory.generateSecret(spec);

                return new SecretKeySpec(key.getEncoded(), "AES");

            } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
                logger.error(e.getMessage());
                throw new AESToolException(e.getMessage());
            }
        }
    }

    public static byte[] encryptByteArrayWithKey(final SecretKey key, final byte[] ba) throws AESToolException {
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            // Get the iv from the cipher
            final byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
            final byte[] enc = cipher.doFinal(ba);

            // Add iv, encrypt and store data
            byte[] cipherArray = new byte[iv.length + enc.length];
            System.arraycopy(iv, 0, cipherArray, 0, iv.length);
            System.arraycopy(enc, 0, cipherArray, iv.length, enc.length);

            return cipherArray;

        } catch (final InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidParameterSpecException | BadPaddingException | IllegalBlockSizeException e) {
            logger.error(e.getMessage());
            throw new AESToolException(e.getMessage());
        }
    }

    public static byte[] decryptCipherArrayWithKey(final SecretKey key, final byte[] cipherArray) throws AESToolException {
        try {
            final byte[] iv = new byte[16];
            final byte[] encrypted = new byte[cipherArray.length - 16];

            System.arraycopy(cipherArray, 0, iv, 0,16);
            System.arraycopy(cipherArray, 16, encrypted,0, encrypted.length);

            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

            return cipher.doFinal(encrypted);

        } catch (final InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            logger.error(e.getMessage());
            throw new AESToolException(e.getMessage());
        }
    }

    public static List<byte[]> encryptByteArraysWithKey(final SecretKey key, final List<byte[]> byteArrays) throws AESToolException {
        final Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        } catch (final NoSuchPaddingException | NoSuchAlgorithmException e){
            logger.error(e.getMessage());
            throw new AESToolException(e.getMessage());
        }

        final List<byte[]> cipherArrayList = new LinkedList<>();
        int index = 0;
        for ( final byte[] ba : byteArrays) {
            try {
                // Get the iv from the cipher
                cipher.init(Cipher.ENCRYPT_MODE, key);
                final byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
                final byte[] enc = cipher.doFinal(ba);

                // Add iv, encrypt and store data
                final byte[] cipherArray = new byte[iv.length + enc.length];
                System.arraycopy(iv,0, cipherArray,0, iv.length);
                System.arraycopy(enc,0, cipherArray, iv.length, enc.length);
                cipherArrayList.add(cipherArray);

            } catch (final BadPaddingException | IllegalBlockSizeException | InvalidParameterSpecException | InvalidKeyException e) {
                logger.error("Failed to encrypt data at index {} - {}", index, e.getMessage());
                cipherArrayList.add(null);
            }

            index++;
        }

        return cipherArrayList;
    }

    public static List<byte[]> decryptCipherArraysWithKey(final SecretKey key, final List<byte[]> cipherArrays) throws AESToolException {
        final Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        } catch (final NoSuchPaddingException | NoSuchAlgorithmException e){
            logger.error(e.getMessage());
            throw new AESToolException(e.getMessage());
        }

        final List<byte[]> decryptedList = new LinkedList<>();
        int index = 0;
        for ( final byte[] cipherArray : cipherArrays) {
            try {
                final byte[] iv = new byte[16];
                final byte[] encryptedData = new byte[cipherArray.length - 16];

                System.arraycopy(cipherArray,0, iv,0,16);
                System.arraycopy(cipherArray,16, encryptedData,0, encryptedData.length);

                cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
                final byte[] decryptedData = cipher.doFinal(encryptedData);

                decryptedList.add(decryptedData);

            } catch (final BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | InvalidKeyException e) {
                logger.error("Failed to decrypt data at index {} - {}", index, e.getMessage());
                decryptedList.add(null);
            }

            index++;
        }

        return decryptedList;
    }

    public static List<String> encryptByteArraysWithKeyToBase64CipherTexts(final SecretKey key, final List<byte[]> byteArrays) throws AESToolException {
        final List<byte[]> cipherArrayList = encryptByteArraysWithKey(key, byteArrays);
        final List<String> base64cipherTexts = new LinkedList<>();

        final Base64.Encoder base64encoder = Base64.getEncoder();
        for ( final byte[] cipherArray : cipherArrayList) {
            final String ba64 = base64encoder.encodeToString(cipherArray);
            base64cipherTexts.add(ba64);
        }

        return base64cipherTexts;
    }

    public static List<byte[]> decryptBase64CipherTextsWithKeyToByteArrays(final SecretKey key, final List<String> base64CipherTexts) throws AESToolException {
        final List<byte[]> cipherArrayList = new LinkedList<>();

        final Base64.Decoder base64decoder = Base64.getDecoder();
        for ( final String cipherText : base64CipherTexts ) {
            final byte[] cipherArray = base64decoder.decode(cipherText);
            cipherArrayList.add(cipherArray);
        }

        return decryptCipherArraysWithKey(key, cipherArrayList);
    }

    public static String encryptStringWithKeyToBase64CipherText(final SecretKey key, final String text) throws AESToolException {
        final byte[] encryptedBa = encryptByteArrayWithKey(key, text.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBa);
    }

    public static String decryptBase64CipherTextWithKeyToString(final SecretKey key, final String cipherText) throws AESToolException {
        final byte[] cipherArray = Base64.getDecoder().decode(cipherText);
        final byte[] decrypted = decryptCipherArrayWithKey(key, cipherArray);

        return new String(decrypted);
    }

    public static String encryptStringWithBase64KeyToBase64CipherText(final String keyAsBase64, final String text) throws AESToolException {
        final byte[] keyBa = Base64.getDecoder().decode(keyAsBase64);
        final SecretKeySpec key = new SecretKeySpec(keyBa, "AES");

        return encryptStringWithKeyToBase64CipherText(key, text);
    }

    public static String decryptBase64CipherTextWithBase64KeyToString(final String keyAsBase64, final String base64CipherText) throws AESToolException {
        final byte[] keyBa = Base64.getDecoder().decode(keyAsBase64);
        final SecretKeySpec key = new SecretKeySpec(keyBa, "AES");

        return decryptBase64CipherTextWithKeyToString(key, base64CipherText);
    }

    public static List<String> encryptStringsWithBase64KeyToBase64CipherTexts(final String keyAsBase64, final List<String> strings) throws AESToolException {
        final byte[] keyBa = Base64.getDecoder().decode(keyAsBase64);
        final SecretKeySpec key = new SecretKeySpec(keyBa, "AES");

        final List<byte[]> byteArrays = new LinkedList<>();
        for ( final String s : strings) {
            byteArrays.add(s.getBytes());
        }

        return encryptByteArraysWithKeyToBase64CipherTexts(key, byteArrays);
    }

    public static List<String> decryptBase64CipherTextsWithBase64KeyToStrings(final String keyAsBase64, final List<String> base64CipherTexts) throws AESToolException {
        final byte [] keyBa = Base64.getDecoder().decode(keyAsBase64);
        final SecretKeySpec key = new SecretKeySpec(keyBa, "AES");

        final List<byte[]> decryptedByteArrays = decryptBase64CipherTextsWithKeyToByteArrays(key, base64CipherTexts);
        final List<String> decryptedStrings = new LinkedList<>();

        for ( final byte[] ba : decryptedByteArrays) {
            decryptedStrings.add(new String(ba));
        }

        return decryptedStrings;
    }
}
