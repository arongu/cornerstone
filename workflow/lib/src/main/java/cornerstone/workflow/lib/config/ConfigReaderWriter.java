package cornerstone.workflow.lib.config;

import cornerstone.workflow.lib.crypto.AESEncryptionDecryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigReaderWriter {
    private static final String aesPrefix = "AES_";
    private static final String encPrefix = "ENC_";
    private static final Pattern pattern = Pattern.compile("^([a-zA-Z0-9-_]+)(?:\\s*)=(?:\\s*)(.+)$");
    private static final Logger log = LoggerFactory.getLogger(ConfigReaderWriter.class);

    /**
     * Opens the key file, and returns the decoded AES key as a byte array.
     * @param filePath Key file path.
     * Reads the first line of the file.
     * Which must be the 256 bit AES key stored as base64 string.
     */
    public static byte[] loadAESKeyFromFile(final String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            return Base64.getDecoder().decode(line);
        } catch (IOException e){
            log.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Loads an encrypted config file and returns the decrypted fields in a Properties object.
     * @param key Decryption key.
     * @param path Encrypted config file path.
     */
    public static Properties decryptFile(final SecretKey key, final String path) throws IOException {
        try {
            final List<String> allLines = Files.readAllLines(Paths.get(path));
            final Properties properties = new Properties();

            int lineNumber = 1;
            for (String line : allLines) {
                Matcher m = pattern.matcher(line);
                if (m.find()){
                    String k = m.group(1);
                    String v = m.group(2);

                    if ( v.startsWith(aesPrefix)){
                        try {
                            String ba64 = v.substring(aesPrefix.length());
                            byte[] encrypted = Base64.getDecoder().decode(ba64);
                            byte[] decrypted = AESEncryptionDecryption.decryptCipherArrayWithKey(key, encrypted);
                            String value = new String(decrypted);
                            properties.put(k, value);
                            log.info("... '{}' = *****", k);

                        } catch (AESEncryptionDecryption.AESToolException e){
                            properties.put(k, null);
                            log.error("... Failed to decrypt: '{}' = '{}' (value set to null)", k, v);
                        }
                    } else {
                        properties.put(k, v);
                        log.info("... '{}' = '{}'", k, v);
                    }
                } else {
                    log.info("... Ignoring line @ {}", lineNumber);
                }
                lineNumber++;
            }

            return properties;

        } catch (IOException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Loads an encrypted config file and returns the decrypted fields in a Properties object.
     * @param key Decryption key.
     * @param path Encrypted config file path.
     */
    public static Properties encryptFile(final SecretKey key, final String path) throws IOException {
        return  null;
    }
}
