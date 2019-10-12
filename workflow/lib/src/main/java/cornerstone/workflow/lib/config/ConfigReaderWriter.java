package cornerstone.workflow.lib.config;

import cornerstone.workflow.lib.crypto.AESEncryptionDecryption;
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

public final class ConfigReaderWriter {
    private static final String aesPrefix = "AES_";
    private static final String encPrefix = "ENC_";
    private static final Pattern configLinePattern = Pattern.compile("^([a-zA-Z0-9-_]+)(\\s*)=(\\s*)(.+)$");
    private static final Logger log = LoggerFactory.getLogger(ConfigReaderWriter.class);

    /**
     * Opens the key file, reads the first line and returns the decoded AES key as a byte array.
     * @param filePath Key file path.
     * Reads the first line of the file.
     * Which must be the 256 bit AES key stored as base64 string.
     */
    public static SecretKey loadAESKeyFromFile(final String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            final String firstLine = reader.readLine();
            final byte[] ba = Base64.getDecoder().decode(firstLine);

            return new SecretKeySpec(ba, "AES");
        }
        catch (IOException e){
            log.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Loads an encrypted config file and returns the decrypted fields in a Properties object.
     * @param key Decryption key.
     * @param path Encrypted config file path.
     */
    public static Properties loadEncryptedConfig(final SecretKey key, final String path) throws IOException {
        try {
            final List<String> allLines = Files.readAllLines(Paths.get(path));
            final Properties properties = new Properties();

            int lineNumber = 1;
            for (String line : allLines) {
                Matcher m = configLinePattern.matcher(line);

                if (m.find()){
                    String k = m.group(1);
                    String v = m.group(4);

                    if ( v.startsWith(aesPrefix)){
                        try {
                            String cipherText = v.substring(aesPrefix.length());
                            String decrypted = AESEncryptionDecryption.decryptBase64CipherTextWithKeyToString(key, cipherText);
                            properties.put(k, decrypted);
                            log.info("... decrypt @ {} '{}' = *****", lineNumber,k);

                        } catch (AESEncryptionDecryption.AESToolException e){
                            properties.put(k, "n/a");
                            log.error("... decrypt FAILED @ {} '{}' = '{}' (value set to null)", lineNumber, k, v);
                        }
                    } else {
                        properties.put(k, v);
                        log.info("... add @ {} '{}' = '{}'", lineNumber, k, v);
                    }
                } else {
                    log.info("... ignore @ {}", lineNumber);
                }
                lineNumber++;
            }
            return properties;
        }
        catch (IOException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Loads an encrypted config file and returns the decrypted fields in a Properties object.
     * @param key Decryption key.
     * @param path Encrypted config file path.
     */
    public static List<String> loadAndEncryptLines(final SecretKey key, final String path) throws IOException {
        try {
            final List<String> allLines = Files.readAllLines(Paths.get(path));
            final List<String> encryptedLines = new LinkedList<>();

            for (String line : allLines) {
                Matcher m = configLinePattern.matcher(line);
                String encryptedLine = line;

                if (m.find()){
                    String k = m.group(1);
                    String v = m.group(4);

                    if ( v.startsWith(encPrefix)){
                        try {
                            String encryptedValue = aesPrefix + AESEncryptionDecryption.encryptStringWithKeyToBase64CipherText(key, v);
                            encryptedLine = m.group(1) + m.group(2) + "=" + m.group(3) + encryptedValue;
                            log.info("... '{}' = '{}'", k, encryptedValue);
                        } catch (AESEncryptionDecryption.AESToolException e){
                            encryptedLine += "n/a";
                            log.error("... Failed to encrypt: '{}' = '****' (value set to n/a)", k);
                        }
                    }
                }

                encryptedLines.add(encryptedLine);
            }

            return encryptedLines;

        } catch (IOException e) {
            log.error(e.getMessage());
            throw e;
        }
    }
}
