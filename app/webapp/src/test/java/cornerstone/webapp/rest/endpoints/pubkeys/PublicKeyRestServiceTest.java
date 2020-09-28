package cornerstone.webapp.rest.endpoints.pubkeys;

import cornerstone.webapp.config.ConfigLoader;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.services.rsa.rotation.KeyPairWithUUID;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStore;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreImpl;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStore;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStoreImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PublicKeyRestServiceTest {
    private static LocalKeyStore localKeyStore;
    private static PublicKeyStore publicKeyStore;

    // prepared uuids
    private static final UUID uuid_only_local  = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final UUID uuid_only_public = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID uuid_both        = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID uuid_none        = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID uuid_expired     = UUID.fromString("00000000-0000-0000-0000-000000000003");
    // public keys
    private static final PublicKey pubkey_local   = new KeyPairWithUUID().keyPair.getPublic();
    private static final PublicKey pubkey_public  = new KeyPairWithUUID().keyPair.getPublic();
    private static final PublicKey pubkey_expired = new KeyPairWithUUID().keyPair.getPublic();


    @BeforeAll
    public static void setSystemProperties() {
        final String test_files_dir = "../../_test_config/";
        final String keyFile        = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile       = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            final ConfigLoader configLoader = new ConfigLoader(keyFile, confFile);
            configLoader.loadAndDecryptConfig();

            localKeyStore   = new LocalKeyStoreImpl();
            publicKeyStore  = new PublicKeyStoreImpl(new WorkDB(configLoader));

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getPublicKey_shouldReturnKey_whenKeyExists() {
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(localKeyStore, publicKeyStore);
        localKeyStore.addPublicKey(uuid_only_local, pubkey_local);


        final Response response           = publicKeyRestService.getPublicKey("00000000-0000-0000-0000-000000000000");
        final Base64.Encoder encoder      = Base64.getEncoder();
        final String str_pubkey_local     = encoder.encodeToString(pubkey_local.getEncoded());
        final String str_pubkey_retrieved = response.getEntity().toString();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(str_pubkey_local, str_pubkey_retrieved );
    }
}
