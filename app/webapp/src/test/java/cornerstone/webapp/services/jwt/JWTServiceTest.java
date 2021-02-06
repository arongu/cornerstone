package cornerstone.webapp.services.jwt;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.services.keys.rotation.KeyPairWithUUID;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStoreImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JWTServiceTest {
    private static ConfigLoader configLoader;
    private static LocalKeyStore localKeyStore;

    @BeforeAll
    public static void init() {
        final String test_files_dir = "../../_test_config/";
        final String keyFile        = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile       = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            configLoader             = new ConfigLoader(keyFile, confFile);
            localKeyStore            = new LocalKeyStoreImpl();
            final KeyPairWithUUID kp = new KeyPairWithUUID();
            localKeyStore.setSigningKeys(kp.uuid, kp.keyPair.getPrivate(), kp.keyPair.getPublic());

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createJWT_shouldAddTheContentsOfTheMapAsClaims() throws Exception {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final Map<String,Object> m  = new HashMap<>();
        m.put("claimOne", "one");
        m.put("claimTwo", 2);
        m.put("claimThree", 3.2);


        final Base64.Decoder decoder = Base64.getDecoder();
        final String jws             = jwtService.createJws("almafa@gmail.com", m);
        final String[] parsed        = jws.split("\\.");
        final String strUuid         = localKeyStore.getSigningKeys().uuid.toString();
        final String strHeader       = new String(decoder.decode(parsed[0]));
        final String strPayload      = new String(decoder.decode(parsed[1]));


        assertEquals("{\"alg\":\"RS512\"}", strHeader);
        assertTrue(strPayload.contains("\"claimOne\":\"one\""));
        assertTrue(strPayload.contains("\"claimTwo\":2"));
        assertTrue(strPayload.contains("\"claimThree\":3.2"));
        assertTrue(strPayload.contains("\"keyId\":\"" + strUuid + "\""));
    }
}
