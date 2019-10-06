package cornerstone.workflow.restapi.config;

import cornerstone.workflow.lib.crypto.AESTool;
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

public final class ConfigLoader {
    private static final String aesPrefix = "AES_";
    private static final Pattern pattern = Pattern.compile("^([a-zA-Z0-9-_]+)(?:\\s*)=(?:\\s*)(.+)$");
    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);

    public static byte[] loadKeyFile(final String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            return Base64.getDecoder().decode(line);
        } catch (IOException e){
            log.error(e.getMessage());
            throw e;
        }
    }

    public static Properties loadEncryptedConfig(final SecretKey key, final String path) throws IOException {
        try {
            final List<String> allLines = Files.readAllLines(Paths.get(path));
            final Properties properties = new Properties();

            int lineNo = 1;
            for (String line : allLines) {
                Matcher m = pattern.matcher(line);
                if (m.find()){
                    String k = m.group(1);
                    String v = m.group(2);

                    if ( v.startsWith(aesPrefix)){
                        try {
                            String ba64 = v.substring(aesPrefix.length());
                            byte[] encrypted = Base64.getDecoder().decode(ba64);
                            byte[] decrypted = AESTool.decryptByteArray(key, encrypted);
                            String value = new String(decrypted);
                            properties.put(k, value);
                            log.info("... '{}' = *****", k);

                        } catch (AESTool.AESToolException e){
                            properties.put(k, null);
                            log.error("... Failed to decrypt: '{}' = '{}' (value set to null)", k, v);
                        }
                    } else {
                        properties.put(k, v);
                        log.info("... '{}' = '{}'", k, v);
                    }
                } else {
                    log.info("... Ignoring line @ {}", lineNo);
                }
                lineNo++;
            }

            return properties;

        } catch (IOException e) {
            log.error(e.getMessage());
            throw e;
        }
    }
}
