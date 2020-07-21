package cornerstone.workflow.webapp.services.rsa_key_services;

import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.services.rsakey.store.remote.DBPublicKeyStore;
import cornerstone.webapp.services.rsakey.store.remote.DBPublicKeyStoreException;
import cornerstone.webapp.services.rsakey.rotation.KeyPairWithUUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;

public class SSLLocalKeyStorageServiceTest {
    private static DBPublicKeyStore sslKeyService;
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

            final WorkDB ds = new WorkDB(configurationLoader);
            sslKeyService = new DBPublicKeyStore(ds);

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() throws DBPublicKeyStoreException {
        final Base64.Encoder encoder = Base64.getEncoder();
        final String nodeName = configurationLoader.getAppProperties().getProperty(APP_ENUM.APP_NODE_NAME.key);

        long start;
        double end;
        for ( int i = 0, max = 10; i < max; i++) {
            start = System.currentTimeMillis();
            final KeyPairWithUUID keyPairWithUUID = new KeyPairWithUUID();
            final String base64pubkey = encoder.encodeToString(keyPairWithUUID.keyPair.getPublic().getEncoded());
            int result = sslKeyService.addPublicKey(keyPairWithUUID.uuid, nodeName, 172800, base64pubkey);

            end = (double)(System.currentTimeMillis() - start) / 1000;
            System.out.println(
                    String.format("[ OK ] %03d/%03d -- elapsed (%.03fs) -- uuid: '%s', base64pubkey: '%s'", i+1, max, end, keyPairWithUUID.uuid.toString(), base64pubkey)
            );
        }
    }
}
