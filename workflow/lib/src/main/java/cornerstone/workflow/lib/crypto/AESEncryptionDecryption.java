package cornerstone.workflow.lib.crypto;

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

public final class AESEncryptionDecryption {
    private static final Logger log = LoggerFactory.getLogger(AESEncryptionDecryption.class);

    public static class AESToolException extends Exception {
        AESToolException(final String message) {
            super(message);
        }
    }

    public static SecretKeySpec derive256BitKey(final String password, final String salt) throws AESToolException {

        if (password == null || salt == null || password.getBytes().length == 0 || salt.getBytes().length != 16) {
            throw new AESToolException("Password and salt cannot be null or empty, salt must be 16 bytes long!");
        }

        try {
            final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            final KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 250000, 256);
            SecretKey key = keyFactory.generateSecret(keySpec);
            return new SecretKeySpec(key.getEncoded(), "AES");

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error(e.getMessage());
            throw new AESToolException(e.getMessage());
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
            byte[] cipherBytes = new byte[iv.length + enc.length];
            System.arraycopy(iv, 0, cipherBytes, 0, iv.length);
            System.arraycopy(enc, 0, cipherBytes, iv.length, enc.length);

            return cipherBytes;

        } catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidParameterSpecException | BadPaddingException | IllegalBlockSizeException e){
            log.error(e.getMessage());
            throw new AESToolException(e.getMessage());
        }
    }

    public static byte[] decryptCipherArrayWithKey(final SecretKey key, final byte[] ba) throws AESToolException {
        try {
            final byte[] iv = new byte[16];
            System.arraycopy(ba, 0, iv, 0,16);

            final byte[] encrypted = new byte[ba.length - 16];
            System.arraycopy(ba, 16, encrypted,0, encrypted.length);

            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

            return cipher.doFinal(encrypted);

        } catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e){
            log.error(e.getMessage());
            throw new AESToolException(e.getMessage());
        }
    }

    public static List<byte[]> encryptByteArraysWithKey(final SecretKey key, final List<byte[]> byteArrays) throws AESToolException {
        final Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e){
            log.error(e.getMessage());
            throw new AESToolException(e.getMessage());
        }

        final List<byte[]> cbList = new LinkedList<>();
        int index = 0;
        for ( byte[] ba : byteArrays){
            try {
                // Get the iv from the cipher
                cipher.init(Cipher.ENCRYPT_MODE, key);
                byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
                byte[] enc = cipher.doFinal(ba);

                // Add iv, encrypt and store data
                byte[] cb = new byte[iv.length + enc.length];
                System.arraycopy(iv, 0, cb, 0, iv.length);
                System.arraycopy(enc, 0, cb, iv.length, enc.length);
                cbList.add(cb);

            } catch (BadPaddingException | IllegalBlockSizeException | InvalidParameterSpecException | InvalidKeyException e) {
                log.error("Failed to encrypt data at index {} - {}", index, e.getMessage());
                cbList.add(null);
            }

            index++;
        }

        return cbList;
    }

    public static List<byte[]> decryptCipherArraysWithKey(final SecretKey key, final List<byte[]> cypherArrays) throws AESToolException {
        final Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e){
            log.error(e.getMessage());
            throw new AESToolException(e.getMessage());
        }

        final List<byte[]> decryptedList = new LinkedList<>();
        int index = 0;
        for ( byte[] cba : cypherArrays) {
            try {
                byte[] iv = new byte[16];
                System.arraycopy(cba, 0, iv, 0,16);

                byte[] encrypted = new byte[cba.length - 16];
                System.arraycopy(cba, 16, encrypted, 0, encrypted.length);

                cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
                byte[] ba = cipher.doFinal(encrypted);

                decryptedList.add(ba);

            } catch (BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | InvalidKeyException e) {
                log.error("Failed to decrypt data at index {} - {}", index, e.getMessage());
                decryptedList.add(null);
            }
            index++;
        }

        return decryptedList;
    }

    public static List<String> encryptByteArraysWithKeyToBase64CipherTexts(final SecretKey key, final List<byte[]> byteArrays) throws AESToolException {
        final List<byte[]> cbList = encryptByteArraysWithKey(key, byteArrays);
        final List<String> base64cipherTexts = new LinkedList<>();

        final Base64.Encoder base64encoder = Base64.getEncoder();
        for ( byte[] ba : cbList){
            String ba64 = base64encoder.encodeToString(ba);
            base64cipherTexts.add(ba64);
        }

        return base64cipherTexts;
    }

    public static List<byte[]> decryptBase64CipherTextsWithKeyToByteArrays(final SecretKey key, final List<String> base64CipherTexts) throws AESToolException {
        final List<byte[]> cbList = new LinkedList<>();

        final Base64.Decoder base64decoder = Base64.getDecoder();
        for ( String ba64 : base64CipherTexts){
            byte[] ba = base64decoder.decode(ba64);
            cbList.add(ba);
        }

        return decryptCipherArraysWithKey(key, cbList);
    }

    public static String encryptStringWithKeyToBase64CipherText(final SecretKey key, final String text) throws AESToolException {
        final byte[] encryptedBa = encryptByteArrayWithKey(key, text.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBa);
    }

    public static String decryptBase64CipherTextWithKeyToString(final SecretKey key, final String cipherText) throws AESToolException {
        final byte[] ba = Base64.getDecoder().decode(cipherText);
        final byte[] decrypted = decryptCipherArrayWithKey(key, ba);

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
        for ( String s : strings){
            byteArrays.add(s.getBytes());
        }

        return encryptByteArraysWithKeyToBase64CipherTexts(key, byteArrays);
    }

    public static List<String> decryptBase64CipherTextsWithBase64KeyToStrings(final String keyAsBase64, final List<String> base64CipherTexts) throws AESToolException {
        final byte [] keyBa = Base64.getDecoder().decode(keyAsBase64);
        final SecretKeySpec key = new SecretKeySpec(keyBa, "AES");

        final List<byte[]> decryptedBas = decryptBase64CipherTextsWithKeyToByteArrays(key, base64CipherTexts);
        final List<String> decrypted = new LinkedList<>();

        for ( byte[] ba : decryptedBas) {
            decrypted.add(new String(ba));
        }

        return decrypted;
    }
}
