package cornerstone.webapp.rest.endpoints.pubkeys;

import cornerstone.webapp.config.ConfigLoader;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.rest.error_responses.SingleErrorResponse;
import cornerstone.webapp.services.rsa.rotation.KeyPairWithUUID;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStore;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreImpl;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStore;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStoreImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PublicKeyRestServiceTest {
    private static ConfigLoader configLoader;
    private static WorkDB workDB;

    @BeforeAll
    public static void setSystemProperties() {
        final String test_files_dir = "../../_test_config/";
        final String keyFile        = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile       = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            configLoader = new ConfigLoader(keyFile, confFile);
            workDB       = new WorkDB(configLoader);

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getPublicKey_shouldReturnKey_whenKeyExists() {
        final String uuidStr                            = "00000000-0000-0000-0000-000000000000";
        final LocalKeyStore localKeyStore               = new LocalKeyStoreImpl();
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(localKeyStore, null);
        final PublicKey pubkey                          = new KeyPairWithUUID().keyPair.getPublic();
        localKeyStore.addPublicKey(UUID.fromString(uuidStr), pubkey);


        final Response response           = publicKeyRestService.getPublicKey(uuidStr);
        final String str_pubkey_local     = Base64.getEncoder().encodeToString(pubkey.getEncoded());
        final String str_pubkey_retrieved = response.getEntity().toString();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(str_pubkey_local, str_pubkey_retrieved );
    }

    @Test
    public void getPublicKey_shouldReturnSingleErrorResponseWithNotFound_whenKeyDoesNotExist() {
        final LocalKeyStore localKeyStore               = new LocalKeyStoreImpl();
        final PublicKeyStore publicKeyStore             = new PublicKeyStoreImpl(workDB);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(localKeyStore, publicKeyStore);


        final Response response = publicKeyRestService.getPublicKey("00000000-0000-0000-0000-000000000111");
        final SingleErrorResponse singleErrorResponse = (SingleErrorResponse)response.getEntity();


        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("No such key.", singleErrorResponse.getError());
    }
}
