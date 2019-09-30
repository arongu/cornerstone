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

public final class AESTool {
    private static final Logger log = LoggerFactory.getLogger(AESTool.class);

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
            throw new AESToolException(e.getMessage());
        }
    }

    public static byte[] encryptByteArray(final SecretKey key, final byte[] ba) throws AESToolException {
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            // Get the iv from the cipher
            final byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
            final byte[] enc = cipher.doFinal(ba);

            // Add iv, encrypt and store data
            byte[] cb = new byte[iv.length + enc.length];
            System.arraycopy(iv, 0, cb, 0, iv.length);
            System.arraycopy(enc, 0, cb, iv.length, enc.length);
            return cb;

        } catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidParameterSpecException | BadPaddingException | IllegalBlockSizeException e){
            throw new AESToolException(e.getMessage());
        }
    }

    public static List<byte[]> encryptByteArrays(final SecretKey key, final List<byte[]> byteArrays) throws AESToolException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e){
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
                log.error("Failed to encrypt data at index {}", index);
                cbList.add(null);
            }
            index++;
        }

        return cbList;
    }

    public static byte[] decryptByteArray(final SecretKey key, final byte[] ba) throws AESToolException {
        try {
            final byte[] iv = new byte[16];
            System.arraycopy(ba, 0, iv, 0,16);

            final byte[] encrypted = new byte[ba.length - 16];
            System.arraycopy(ba, 16, encrypted,0, encrypted.length);

            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

            return cipher.doFinal(encrypted);

        } catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e){
            throw new AESToolException(e.getMessage());
        }
    }

    public static List<byte[]> decryptByteArrays(final SecretKey key, final List<byte[]> cypherArrays) throws AESToolException {
        final Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e){
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
                log.error("Failed to decrypt data at index {}", index);
                decryptedList.add(null);
            }
            index++;
        }

        return decryptedList;
    }

    public static List<String> encryptByteArraysToBase64Strings(final SecretKey key, final List<byte[]> byteArrays) throws AESToolException {
        final List<byte[]> cbList = encryptByteArrays(key, byteArrays);
        final List<String> base64cipherTexts = new LinkedList<>();

        final Base64.Encoder base64encoder = Base64.getEncoder();
        for ( byte[] ba : cbList){
            String ba64 = base64encoder.encodeToString(ba);
            base64cipherTexts.add(ba64);
        }

        return base64cipherTexts;
    }

    public static List<byte[]> decryptBase64StringsToByteArrays(final SecretKey key, final List<String> base64CipherTexts) throws AESToolException {
        final List<byte[]> cbList = new LinkedList<>();

        final Base64.Decoder base64decoder = Base64.getDecoder();
        for ( String ba64 : base64CipherTexts){
            byte[] ba = base64decoder.decode(ba64);
            cbList.add(ba);
        }

        return decryptByteArrays(key, cbList);
    }

    public static String encrypt(final String keyAsBase64, final String text) throws AESToolException {
        byte[] key = Base64.getDecoder().decode(keyAsBase64);
        byte[] encryptedBa = encryptByteArray(new SecretKeySpec(key, "AES"), text.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBa);
    }

    public static List<String> encrypt(final String keyAsBase64, final List<String> strings) throws AESToolException {
        byte[] keyBa = Base64.getDecoder().decode(keyAsBase64);
        SecretKeySpec key = new SecretKeySpec(keyBa, "AES");

        List<byte[]> byteArrays = new LinkedList<>();
        for ( String s : strings){
            byteArrays.add(s.getBytes());
        }

        return encryptByteArraysToBase64Strings(key, byteArrays);
    }

    public static String decrypt(final String keyAsBase64, final String base64CipherText) throws AESToolException {
        final Base64.Decoder decoder = Base64.getDecoder();

        byte[] key = decoder.decode(keyAsBase64);
        byte[] encryptedBa = decoder.decode(base64CipherText);

        byte[] decryptedBa = decryptByteArray(new SecretKeySpec(key, "AES"), encryptedBa);
        return new String(decryptedBa);
    }

    public static List<String> decrypt(final String keyAsBase64, final List<String> base64CipherTexts) throws AESToolException {
        final SecretKeySpec key = new SecretKeySpec(
                Base64.getDecoder().decode(keyAsBase64),
                "AES"
        );

        final List<byte[]> decryptedBas = decryptBase64StringsToByteArrays(key, base64CipherTexts);
        List<String> decrypted = new LinkedList<>();

        for ( byte[] ba : decryptedBas) {
            decrypted.add(new String(ba));
        }

        return decrypted;
    }
}
