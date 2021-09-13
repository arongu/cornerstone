package cornerstone.webapp.rest.api.pubkeys;

import cornerstone.webapp.rest.error_responses.ErrorResponse;
import cornerstone.webapp.services.keys.rotation.KeyPairWithUUID;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStore;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStoreException;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStoreImpl;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStoreImpl;
import cornerstone.webapp.services.keys.stores.manager.KeyManager;
import cornerstone.webapp.services.keys.stores.manager.KeyManagerImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;
import java.security.PublicKey;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
    getPublicKey - 200  DONE
    getPublicKey - 404  DONE
    getPublicKey - 503  DONE

    getExpiredKeyUUIDs - 200 content    DONE
    getExpiredKeyUUIDs - 200 no-content DONE
    getExpiredKeyUUIDs - 503 error      DONE

    getLiveKeyUUIDs - 200 content     DOME
    getLiveKeyUUIDs - 200 no-content  DONE
    getLiveKeyUUIDs - 500 error       DONE
 */

public class PublicKeyRestServiceTest {
    // getPublicKey - 200
    @Test
    public void getPublicKey__shouldReturnKeyWith_200_OK__whenKeyExists() {
        final String               uuid                 = "00000000-0000-0000-0000-000000000000";
        final LocalKeyStore        localKeyStore        = new LocalKeyStoreImpl();
        final KeyManager           keyManager           = new KeyManagerImpl(null, localKeyStore, null);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(keyManager);
        final PublicKey publicKey                       = new KeyPairWithUUID().keyPair.getPublic();
        localKeyStore.addPublicKey(UUID.fromString(uuid), publicKey);


        final Response     response             = publicKeyRestService.getPublicKey(uuid);
        final String       str_pubkey_local     = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        final PublicKeyDTO publicKeyDTO         = (PublicKeyDTO) response.getEntity();
        final String       str_pubkey_retrieved = publicKeyDTO.getPubkey();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(str_pubkey_local, str_pubkey_retrieved );
    }

    // getPublicKey - 404
    @Test
    public void getPublicKey__shouldReturn_404_NotFound__whenKeyDoesNotExistInBothKeyStores() throws DatabaseKeyStoreException {
        final LocalKeyStore        localKeyStore              = Mockito.mock(LocalKeyStoreImpl.class);
        final DatabaseKeyStore     databaseKeyStore           = Mockito.mock(DatabaseKeyStoreImpl.class);
        final KeyManager           keyManager                 = new KeyManagerImpl(null, localKeyStore, databaseKeyStore);
        final PublicKeyRestService publicKeyRestService       = new PublicKeyRestService(keyManager);
        // mocks
        Mockito.when(databaseKeyStore.getPublicKey(Mockito.any(UUID.class)))
               .thenThrow(NoSuchElementException.class);
        // mocks
        Mockito.when(localKeyStore.getPublicKey(Mockito.any(UUID.class)))
               .thenThrow(NoSuchElementException.class);


        final Response response = publicKeyRestService.getPublicKey("00000000-0000-0000-0000-000000000111");
        final ErrorResponse er  = (ErrorResponse)response.getEntity();


        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("No such key.", er.getError());
        Mockito.verify(localKeyStore,    Mockito.times(1)).getPublicKey(Mockito.any(UUID.class));
        Mockito.verify(databaseKeyStore, Mockito.times(1)).getPublicKey(Mockito.any(UUID.class));
    }


    // getPublicKey - 503  DONE
    @Test
    public void getPublicKey__shouldReturn_503__whenPublicKeyStoreExceptionIsThrown() throws DatabaseKeyStoreException {
        final LocalKeyStore        localKeyStore        = Mockito.mock(LocalKeyStoreImpl.class);
        final DatabaseKeyStore     databaseKeyStore     = Mockito.mock(DatabaseKeyStoreImpl.class);
        final KeyManager           keyManager           = new KeyManagerImpl(null, localKeyStore, databaseKeyStore);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(keyManager);
        // mocks
        Mockito.when(localKeyStore.getPublicKey(Mockito.any(UUID.class)))
               .thenThrow(NoSuchElementException.class);
        Mockito.when(databaseKeyStore.getPublicKey(Mockito.any(UUID.class)))
               .thenThrow(DatabaseKeyStoreException.class);


        final Response response = publicKeyRestService.getPublicKey("00000000-0000-0000-0000-000000000111");
        final ErrorResponse er  = (ErrorResponse)response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("An error occurred during public key retrieval!", er.getError());
        Mockito.verify(localKeyStore,    Mockito.times(1)).getPublicKey(Mockito.any(UUID.class));
        Mockito.verify(databaseKeyStore, Mockito.times(1)).getPublicKey(Mockito.any(UUID.class));
    }

    // getExpiredKeyUUIDs - 200 content
    @Test
    public void getExpiredKeyUUIDs__shouldReturn_200_OK_withListOfUUIDs__whenThereAreExpiredKeys() throws DatabaseKeyStoreException {
        final DatabaseKeyStore     databaseKeyStore     = Mockito.mock(DatabaseKeyStore.class);
        final KeyManager           keyManager           = new KeyManagerImpl(null, null, databaseKeyStore);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(keyManager);
        final List<UUID>           expired_uuids        = new LinkedList<>();
        final UUID                 exp_a                = UUID.fromString("00000000-0000-0000-0000-000000005555");
        final UUID                 exp_b                = UUID.fromString("00000000-0000-0000-0000-000000006666");
        expired_uuids.add(exp_a);
        expired_uuids.add(exp_b);
        // mocks
        Mockito.when(databaseKeyStore.getExpiredPublicKeyUUIDs())
               .thenReturn(expired_uuids);


        final Response response       = publicKeyRestService.getExpiredKeyUUIDs();
        final List<UUID> receivedList = (List<UUID>) response.getEntity();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(2, receivedList.size());
        assertTrue(receivedList.contains(exp_a));
        assertTrue(receivedList.contains(exp_b));
        assertEquals(expired_uuids, receivedList);
    }

    // getExpiredKeyUUIDs - 200 no-content
    @Test
    public void getExpiredKeyUUIDs__shouldReturn_200_OK_withEmptyList__whenThereAreNoExpiredKeys() throws DatabaseKeyStoreException {
        final DatabaseKeyStore     databaseKeyStore     = Mockito.mock(DatabaseKeyStore.class);
        final KeyManager           keyManager           = new KeyManagerImpl(null, null, databaseKeyStore);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(keyManager);
        // mocks
        Mockito.when(databaseKeyStore.getExpiredPublicKeyUUIDs())
               .thenReturn(new LinkedList<>());


        final Response response       = publicKeyRestService.getExpiredKeyUUIDs();
        final List<UUID> receivedList = (List<UUID>) response.getEntity();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(0, receivedList.size());
    }

    // getExpiredKeyUUIDs - 503 error
    @Test
    public void getExpiredKeyUUIDs__shouldReturn_500__whenPublicKeyStoreExceptionIsThrown() throws DatabaseKeyStoreException {
        final DatabaseKeyStore     databaseKeyStore     = Mockito.mock(DatabaseKeyStore.class);
        final KeyManager           keyManager           = new KeyManagerImpl(null, null, databaseKeyStore);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(keyManager);
        // mocks
        Mockito.when(databaseKeyStore.getExpiredPublicKeyUUIDs())
               .thenThrow(DatabaseKeyStoreException.class);


        final Response response = publicKeyRestService.getExpiredKeyUUIDs();
        final ErrorResponse er  = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("An error occurred during expired uuid retrieval.", er.getError());
    }

    // getLiveKeyUUIDs - 200 content
    @Test
    public void getLiveKeyUUIDs__shouldReturn_200_OK_withListOfUUIDs__whenThereAreLiveKeys() throws DatabaseKeyStoreException {
        final DatabaseKeyStore     databaseKeyStore     = Mockito.mock(DatabaseKeyStore.class);
        final KeyManager           keyManager           = new KeyManagerImpl(null, null, databaseKeyStore);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(keyManager);
        final List<UUID> list                           = new LinkedList<>();
        // mocks
        list.add(UUID.fromString("00000000-0000-0000-0000-000000005555"));
        Mockito.when(databaseKeyStore.getLivePublicKeyUUIDs())
               .thenReturn(list);


        final Response response       = publicKeyRestService.getLiveKeyUUIDs();
        final List<UUID> receivedList = (List<UUID>) response.getEntity();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(1, receivedList.size());
        assertEquals(list, receivedList);
    }

    // getLiveKeyUUIDs - 200 no-content
    @Test
    public void getLiveKeyUUIDs__shouldReturn_200_OK_withEmptyList__whenThereAreNoLiveKeys() throws DatabaseKeyStoreException {
        final DatabaseKeyStore     databaseKeyStore     = Mockito.mock(DatabaseKeyStore.class);
        final KeyManager           keyManager           = new KeyManagerImpl(null, null, databaseKeyStore);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(keyManager);
        // mocks
        Mockito.when(databaseKeyStore.getLivePublicKeyUUIDs())
               .thenReturn(new LinkedList<>());


        final Response response       = publicKeyRestService.getLiveKeyUUIDs();
        final List<UUID> receivedList = (List<UUID>) response.getEntity();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(0, receivedList.size());
    }

    // getLiveKeyUUIDs - 500 error
    @Test
    public void getLiveKeyUUIDs__shouldReturn_503__whenPublicKeyStoreExceptionIsThrown() throws DatabaseKeyStoreException {
        final DatabaseKeyStore     databaseKeyStore     = Mockito.mock(DatabaseKeyStore.class);
        final KeyManager           keyManager           = new KeyManagerImpl(null, null, databaseKeyStore);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(keyManager);
        // mocks
        Mockito.when(databaseKeyStore.getLivePublicKeyUUIDs())
               .thenThrow(DatabaseKeyStoreException.class);


        final Response response = publicKeyRestService.getLiveKeyUUIDs();
        final ErrorResponse er  = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("An error occurred during live uuid retrieval.", er.getError());
    }
}
