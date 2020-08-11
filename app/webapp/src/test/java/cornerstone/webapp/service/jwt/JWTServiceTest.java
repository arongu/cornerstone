package cornerstone.webapp.service.jwt;

import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.service.rsa.rotation.KeyPairWithUUID;
import cornerstone.webapp.service.rsa.store.local.LocalKeyStore;
import cornerstone.webapp.service.rsa.store.local.LocalKeyStoreImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class JWTServiceTest {
    private static ConfigurationLoader configurationLoader;
    private static LocalKeyStore localKeyStore;

    @BeforeAll
    public static void init() {
        final String test_files_dir = "../../_test_config/";
        final String keyFile  = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            configurationLoader = new ConfigurationLoader(keyFile, confFile);

            localKeyStore = new LocalKeyStoreImpl();
            final KeyPairWithUUID kp = new KeyPairWithUUID();
            localKeyStore.setPublicAndPrivateKeys(kp.uuid, kp.keyPair.getPrivate(), kp.keyPair.getPublic());

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void dummy() {
        final JWTService JWTService = new JWTServiceImpl(configurationLoader, localKeyStore);

        final Map<String,Object> m = new HashMap<>();
        m.put("claimOne", "one");
        m.put("climTwo", 2);
        m.put("claimThree", 3.2);

        final String jwt_string = JWTService.issueJWT("almafa@gmail.com", m);
        final String[] parsed = jwt_string.split("\\.");
        final Base64.Decoder decoder = Base64.getDecoder();


        final String header    = new String(decoder.decode(parsed[0]));
        final String payload   = new String(decoder.decode(parsed[1]));
        final String signature = new String(Base64.getUrlDecoder().decode(parsed[2]));

        System.out.println(header);
        System.out.println(payload);
        System.out.println(signature);
    }
}
