package cornerstone.workflow.webapp.services.ssl_key_service;

import cornerstone.workflow.webapp.configuration.ConfigurationLoader;
import cornerstone.workflow.webapp.configuration.ConfigurationLoaderException;
import cornerstone.workflow.webapp.configuration.enums.APP_ENUM;
import cornerstone.workflow.webapp.datasource.DataSourceWorkDB;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;

public class SSLKeyServiceTest {
    private static SSLKeyService sslKeyService;
    private static ConfigurationLoader configurationLoader;

    @BeforeAll
    public static void setSystemProperties() {
        final String test_files_dir = "../../_test_config/";
        final String confPath = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();
        final String keyPath  = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();

        System.setProperty(ConfigurationLoader.SYSTEM_PROPERTY_CONF_FILE, confPath);
        System.setProperty(ConfigurationLoader.SYSTEM_PROPERTY_KEY_FILE, keyPath);

        try {
            configurationLoader = new ConfigurationLoader();
            configurationLoader.loadAndDecryptConfig();

            final DataSourceWorkDB ds = new DataSourceWorkDB(configurationLoader);
            sslKeyService = new SSLKeyService(ds);

        } catch ( final IOException | ConfigurationLoaderException e ) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() throws SSLKeyServiceException, NoSuchMethodException {
        final Base64.Encoder encoder = Base64.getEncoder();
        final String nodeName = configurationLoader.getApp_properties().getProperty(APP_ENUM.NODE_NAME.key);

        long start;
        double end;
        for ( int i = 0, max = 10; i < max; i++) {
            start = System.currentTimeMillis();
            final KeyPairWithUUID keyPairWithUUID = KeyPairWithUUIDGenerator.generateKeyPairWithUUID();
            final String base64pubkey = encoder.encodeToString(keyPairWithUUID.keyPair.getPublic().getEncoded());
            int result = sslKeyService.savePublicKeyToDB(keyPairWithUUID.uuid, nodeName, base64pubkey, 172800);

            end = (double)(System.currentTimeMillis() - start) / 1000;
            System.out.println(
                    String.format("[ OK ] %03d/%03d -- elapsed (%.03fs) -- uuid: '%s', base64pubkey: '%s'", i+1, max, end, keyPairWithUUID.uuid.toString(), base64pubkey)
            );
        }
    }
}
