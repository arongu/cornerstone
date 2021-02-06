package cornerstone.webapp.rest.endpoints.pubkeys;

import cornerstone.webapp.rest.error_responses.ErrorResponse;
import cornerstone.webapp.services.keys.rotation.KeyPairWithUUID;
import cornerstone.webapp.services.keys.stores.db.PublicKeyStore;
import cornerstone.webapp.services.keys.stores.db.PublicKeyStoreException;
import cornerstone.webapp.services.keys.stores.db.PublicKeyStoreImpl;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStoreImpl;
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

public class PublicKeyDTORestServiceTest {
    // getPublicKey - 200
    @Test
    public void getPublicKey__shouldReturnKeyWith_200_OK__whenKeyExists() {
        final String uuidStr                            = "00000000-0000-0000-0000-000000000000";
        final LocalKeyStore localKeyStore               = new LocalKeyStoreImpl();
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(localKeyStore, null);
        final PublicKey pubkey                          = new KeyPairWithUUID().keyPair.getPublic();
        localKeyStore.addPublicKey(UUID.fromString(uuidStr), pubkey);


        final Response response           = publicKeyRestService.getPublicKey(uuidStr);
        final String str_pubkey_local     = Base64.getEncoder().encodeToString(pubkey.getEncoded());
        final PublicKeyDTO publicKeyDTO   = (PublicKeyDTO) response.getEntity();
        final String str_pubkey_retrieved = publicKeyDTO.getPubkey();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(str_pubkey_local, str_pubkey_retrieved );
    }

    // getPublicKey - 404
    @Test
    public void getPublicKey__shouldReturn_404_NotFound__whenKeyDoesNotExistInBothKeyStores() throws PublicKeyStoreException {
        final LocalKeyStore localKeyStore               = Mockito.mock(LocalKeyStoreImpl.class);
        final PublicKeyStore publicKeyStore             = Mockito.mock(PublicKeyStoreImpl.class);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(localKeyStore, publicKeyStore);
        Mockito.when(publicKeyStore.getKey(Mockito.any(UUID.class))).thenThrow(NoSuchElementException.class);
        Mockito.when(localKeyStore.getPublicKey(Mockito.any(UUID.class))).thenThrow(NoSuchElementException.class);


        final Response response = publicKeyRestService.getPublicKey("00000000-0000-0000-0000-000000000111");
        final ErrorResponse er  = (ErrorResponse)response.getEntity();


        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("No such key.", er.getError());
        Mockito.verify(localKeyStore, Mockito.times(1)).getPublicKey(Mockito.any(UUID.class));
        Mockito.verify(publicKeyStore, Mockito.times(1)).getKey(Mockito.any(UUID.class));
    }


    // getPublicKey - 503  DONE
    @Test
    public void getPublicKey__shouldReturn_503__whenPublicKeyStoreExceptionIsThrown() throws PublicKeyStoreException {
        final LocalKeyStore localKeyStore               = Mockito.mock(LocalKeyStoreImpl.class);
        final PublicKeyStore publicKeyStore             = Mockito.mock(PublicKeyStoreImpl.class);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(localKeyStore, publicKeyStore);
        Mockito.when(localKeyStore.getPublicKey(Mockito.any(UUID.class))).thenThrow(NoSuchElementException.class);
        Mockito.when(publicKeyStore.getKey(Mockito.any(UUID.class))).thenThrow(PublicKeyStoreException.class);


        final Response response = publicKeyRestService.getPublicKey("00000000-0000-0000-0000-000000000111");
        final ErrorResponse er  = (ErrorResponse)response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("An error occurred during public key retrieval/local caching.", er.getError());
        Mockito.verify(localKeyStore, Mockito.times(1)).getPublicKey(Mockito.any(UUID.class));
        Mockito.verify(publicKeyStore, Mockito.times(1)).getKey(Mockito.any(UUID.class));
    }

    // getExpiredKeyUUIDs - 200 content
    @Test
    public void getExpiredKeyUUIDs__shouldReturn_200_OK_withListOfUUIDs__whenThereAreExpiredKeys() throws PublicKeyStoreException {
        final PublicKeyStore publicKeyStore             = Mockito.mock(PublicKeyStore.class);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(null, publicKeyStore);
        final List<UUID> uuidList                       = new LinkedList<>();
        final UUID expiredUuid                          = UUID.fromString("00000000-0000-0000-0000-000000005555");
        final UUID expiredUuid2                         = UUID.fromString("00000000-0000-0000-0000-000000006666");
        uuidList.add(expiredUuid);
        uuidList.add(expiredUuid2);
        Mockito.when(publicKeyStore.getExpiredKeyUUIDs()).thenReturn(uuidList);


        final Response response       = publicKeyRestService.getExpiredKeyUUIDs();
        final List<UUID> receivedList = (List<UUID>) response.getEntity();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(2, receivedList.size());
        assertTrue(receivedList.contains(expiredUuid));
        assertTrue(receivedList.contains(expiredUuid2));
        assertEquals(uuidList, receivedList);
    }

    // getExpiredKeyUUIDs - 200 no-content
    @Test
    public void getExpiredKeyUUIDs__shouldReturn_200_OK_withEmptyList__whenThereAreNoExpiredKeys() throws PublicKeyStoreException {
        final PublicKeyStore publicKeyStore             = Mockito.mock(PublicKeyStore.class);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(null, publicKeyStore);
        Mockito.when(publicKeyStore.getExpiredKeyUUIDs()).thenReturn(new LinkedList<>());


        final Response response       = publicKeyRestService.getExpiredKeyUUIDs();
        final List<UUID> receivedList = (List<UUID>) response.getEntity();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(0, receivedList.size());
    }

    // getExpiredKeyUUIDs - 503 error
    @Test
    public void getExpiredKeyUUIDs__shouldReturn_500__whenPublicKeyStoreExceptionIsThrown() throws PublicKeyStoreException {
        final PublicKeyStore publicKeyStore             = Mockito.mock(PublicKeyStore.class);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(null, publicKeyStore);
        Mockito.when(publicKeyStore.getExpiredKeyUUIDs()).thenThrow(PublicKeyStoreException.class);


        final Response response = publicKeyRestService.getExpiredKeyUUIDs();
        final ErrorResponse er  = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("An error occurred during expired key retrieval.", er.getError());
    }

    // getLiveKeyUUIDs - 200 content
    @Test
    public void getLiveKeyUUIDs__shouldReturn_200_OK_withListOfUUIDs__whenThereAreLiveKeys() throws PublicKeyStoreException {
        final PublicKeyStore publicKeyStore = Mockito.mock(PublicKeyStore.class);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(null, publicKeyStore);
        final List<UUID> list = new LinkedList<>();
        list.add(UUID.fromString("00000000-0000-0000-0000-000000005555"));
        Mockito.when(publicKeyStore.getLiveKeyUUIDs()).thenReturn(list);


        final Response response       = publicKeyRestService.getLiveKeyUUIDs();
        final List<UUID> receivedList = (List<UUID>) response.getEntity();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(1, receivedList.size());
        assertEquals(list, receivedList);
    }

    // getLiveKeyUUIDs - 200 no-content
    @Test
    public void getLiveKeyUUIDs__shouldReturn_200_OK_withEmptyList__whenThereAreNoLiveKeys() throws PublicKeyStoreException {
        final PublicKeyStore publicKeyStore = Mockito.mock(PublicKeyStore.class);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(null, publicKeyStore);
        Mockito.when(publicKeyStore.getLiveKeyUUIDs()).thenReturn(new LinkedList<>());


        final Response response       = publicKeyRestService.getLiveKeyUUIDs();
        final List<UUID> receivedList = (List<UUID>) response.getEntity();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(0, receivedList.size());
    }

    // getLiveKeyUUIDs - 500 error
    @Test
    public void getLiveKeyUUIDs__shouldReturn_503__whenPublicKeyStoreExceptionIsThrown() throws PublicKeyStoreException {
        final PublicKeyStore publicKeyStore = Mockito.mock(PublicKeyStore.class);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(null, publicKeyStore);
        Mockito.when(publicKeyStore.getLiveKeyUUIDs()).thenThrow(PublicKeyStoreException.class);


        final Response response = publicKeyRestService.getLiveKeyUUIDs();
        final ErrorResponse er  = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("An error occurred during live key retrieval.", er.getError());
    }
}
