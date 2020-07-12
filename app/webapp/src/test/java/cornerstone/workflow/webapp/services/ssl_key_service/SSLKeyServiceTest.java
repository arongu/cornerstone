package cornerstone.workflow.webapp.services.ssl_key_service;

import cornerstone.workflow.webapp.configuration.ConfigurationLoader;
import cornerstone.workflow.webapp.configuration.ConfigurationLoaderException;
import cornerstone.workflow.webapp.datasource.DataSourceWorkDB;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;

public class SSLKeyServiceTest {
    private static SSLKeyService sslKeyService;

    @BeforeAll
    public static void setSystemProperties() {
        final String test_files_dir = "../../_test_config/";
        final String confPath = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();
        final String keyPath  = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();

        System.setProperty(ConfigurationLoader.SYSTEM_PROPERTY_CONF_FILE, confPath);
        System.setProperty(ConfigurationLoader.SYSTEM_PROPERTY_KEY_FILE, keyPath);

        try {
            final ConfigurationLoader cr = new ConfigurationLoader();
            cr.loadAndDecryptConfig();

            final DataSourceWorkDB ds = new DataSourceWorkDB(cr);
            sslKeyService = new SSLKeyService(ds);

        } catch ( final IOException | ConfigurationLoaderException e ) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() throws SSLKeyServiceException, NoSuchMethodException {
        final Base64.Encoder encoder = Base64.getEncoder();
        for ( int i = 0 ; i < 1000; i++) {
            final KeyPairWithUUID kd = KeyPairWithUUIDGenerator.generateKeyPairWithUUID();
            final String base64pubkey = encoder.encodeToString(kd.keyPair.getPublic().getEncoded());

            long start = System.currentTimeMillis();
            int result = sslKeyService.savePublicKeyToDB(base64pubkey, "SSLKeyServiceTest", kd.uuid);
            long end = System.currentTimeMillis() - start;
            System.out.println(String.format("OK -- %s --(%d) uuid: '%s', base64pubkey: '%s'", end,result, kd.uuid.toString(), base64pubkey));
        }
    }
}
