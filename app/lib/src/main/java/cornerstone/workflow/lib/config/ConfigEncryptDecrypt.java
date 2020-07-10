package cornerstone.workflow.lib.config;

import cornerstone.workflow.lib.crypto.AESEncryptDecrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigEncryptDecrypt {
    private static final String aesPrefix = "AES_";
    private static final String encPrefix = "ENC_";

    private static final String messageEncrypted = "[ ENCRYPT ]        @ {}  '{}' = *****";
    private static final String messageEncryptionFailed = "[ ENCRYPT FAILED ] @ {}  '{}' = '****' (value set to n/a)";
    private static final String messageDecrypted = "[ DECRYPT, ADD ]   @ {}  '{}' = *****";
    private static final String messageDecryptionFailed = "[ DECRYPT FAILED ] @ {}  '{}' = '{}'";
    private static final String messageAdd = "[ ADD ]            @ {}  '{}' = '{}'";
    private static final String messageIgnore = "[ IGNORE ]         @ {}  '{}'";

    private static final Pattern configLinePattern = Pattern.compile("^([a-zA-Z0-9-_]+)(\\s*)=(\\s*)(.+)$");
    private static final Logger logger = LoggerFactory.getLogger(ConfigEncryptDecrypt.class);

    /**
     * Opens the key file, reads the first line and returns the decoded AES key as a byte array.
     * @param path Key file path.
     * Reads the first line of the file.
     * Which must be the 256 bit AES key stored as base64 string.
     */
    public static SecretKey loadAESKeyFromFile(final String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            final String firstLine = reader.readLine();
            final byte[] ba = Base64.getDecoder().decode(firstLine);

            return new SecretKeySpec(ba, "AES");
        }
        catch (IOException e){
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Loads an encrypted config file and returns the decrypted fields in a Properties object.
     * @param key Decryption key.
     * @param path Encrypted config file path.
     */
    public static Properties decryptConfig(final SecretKey key, final String path) throws IOException {
        try {
            final List<String> allLines = Files.readAllLines(Paths.get(path));
            final Properties properties = new Properties();

            int lineNumber = 1;
            for (String line : allLines) {
                final Matcher m = configLinePattern.matcher(line);

                if ( m.find()){
                    final String k = m.group(1);
                    final String v = m.group(4);

                    if ( v.startsWith(aesPrefix)) {
                        try {
                            final String cipherText = v.substring(aesPrefix.length());
                            final String decrypted = AESEncryptDecrypt.decryptBase64CipherTextWithKeyToString(key, cipherText);
                            properties.put(k, decrypted);
                            logger.info(messageDecrypted, String.format("%03d" , lineNumber), k);

                        } catch (AESEncryptDecrypt.AESToolException e) {
                            properties.put(k, "n/a");
                            logger.error(messageDecryptionFailed, String.format("%03d" , lineNumber), k, v);
                        }

                    } else {
                        properties.put(k, v);
                        logger.info(messageAdd, String.format("%03d" , lineNumber), k, v);
                    }

                } else {
                    logger.info(messageIgnore, String.format("%03d" , lineNumber), line);
                }

                lineNumber++;
            }

            return properties;

        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Loads a config file and returns the encrypted lines in a List<String>
     * @param key Encryption key.
     * @param path Config file path.
     */
    public static List<String> encryptConfig(final SecretKey key, final String path) throws IOException {
        try {
            final List<String> lines = Files.readAllLines(Paths.get(path));
            final List<String> encryptedLines = new LinkedList<>();

            int lineNumber = 1;
            for ( String line : lines) {
                final Matcher m = configLinePattern.matcher(line);
                String encryptedLine = line;

                if ( m.find()) {
                    final String k = m.group(1);
                    final String v = m.group(4);

                    if ( v.startsWith(encPrefix)) {
                        try {
                            final String toEncrypt = v.substring(encPrefix.length());
                            final String encryptedValue = aesPrefix + AESEncryptDecrypt.encryptStringWithKeyToBase64CipherText(key, toEncrypt);
                            encryptedLine = m.group(1) + m.group(2) + "=" + m.group(3) + encryptedValue;
                            logger.info(messageEncrypted, String.format("%03d" , lineNumber), k);

                        } catch (AESEncryptDecrypt.AESToolException e) {
                            encryptedLine += "n/a";
                            logger.error(messageEncryptionFailed, String.format("%03d" , lineNumber), k);
                        }
                    }
                }

                encryptedLines.add(encryptedLine);
                lineNumber++;
            }

            return encryptedLines;

        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }
}
