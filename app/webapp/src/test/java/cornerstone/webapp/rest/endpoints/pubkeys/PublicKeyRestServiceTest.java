package cornerstone.webapp.rest.endpoints.pubkeys;

import cornerstone.webapp.rest.error_responses.SingleErrorResponse;
import cornerstone.webapp.services.rsa.rotation.KeyPairWithUUID;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStore;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreException;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreImpl;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStore;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStoreImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;
import java.security.PublicKey;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PublicKeyRestServiceTest {
    @Test
    public void getPublicKey_shouldReturnAKey_whenItExists() {
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
    public void getPublicKey_shouldReturnSingleErrorResponseWithNotFound_whenKeyDoesNotExistInBothKeyStore() throws PublicKeyStoreException {
        final LocalKeyStore localKeyStore               = Mockito.mock(LocalKeyStoreImpl.class);
        final PublicKeyStore publicKeyStore             = Mockito.mock(PublicKeyStoreImpl.class);
        Mockito.when(publicKeyStore.getKey(Mockito.any(UUID.class))).thenThrow(NoSuchElementException.class);
        Mockito.when(localKeyStore.getPublicKey(Mockito.any(UUID.class))).thenThrow(NoSuchElementException.class);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(localKeyStore, publicKeyStore);


        final Response response = publicKeyRestService.getPublicKey("00000000-0000-0000-0000-000000000111");
        final SingleErrorResponse singleErrorResponse = (SingleErrorResponse)response.getEntity();


        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("No such key.", singleErrorResponse.getError());
        Mockito.verify(localKeyStore, Mockito.times(1)).getPublicKey(Mockito.any(UUID.class));
        Mockito.verify(publicKeyStore, Mockito.times(1)).getKey(Mockito.any(UUID.class));
    }

    @Test
    public void getExpiredKeys__shouldReturnExpiredKeysWith_200_OK__whenThereAreExpiredKeys() throws PublicKeyStoreException {
        final PublicKeyStore publicKeyStore             = Mockito.mock(PublicKeyStore.class);
        final List<UUID> uuidList                       = new LinkedList<>();
        final UUID expiredUuid                          = UUID.fromString("00000000-0000-0000-0000-000000005555");
        final UUID expiredUuid2                         = UUID.fromString("00000000-0000-0000-0000-000000006666");
        uuidList.add(expiredUuid);
        uuidList.add(expiredUuid2);
        Mockito.when(publicKeyStore.getExpiredKeyUUIDs()).thenReturn(uuidList);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(null, publicKeyStore);


        final Response response                         = publicKeyRestService.getExpiredKeyUUIDs();
        final List<UUID> receivedList                   = (List<UUID>) response.getEntity();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(2, receivedList.size());
        assertTrue(receivedList.contains(expiredUuid));
        assertTrue(receivedList.contains(expiredUuid2));
        assertEquals(uuidList, receivedList);
    }

    @Test
    public void getExpiredKeys__shouldReturn_204_NoContent__whenThereAreNoKeys() throws PublicKeyStoreException {
        final PublicKeyStore publicKeyStore             = Mockito.mock(PublicKeyStore.class);
        Mockito.when(publicKeyStore.getExpiredKeyUUIDs()).thenReturn(new LinkedList<>());
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(null, publicKeyStore);


        final Response response                         = publicKeyRestService.getExpiredKeyUUIDs();
        final List<UUID> receivedList                   = (List<UUID>) response.getEntity();


        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void getLivePublicKeys__shouldReturn_200_OK__whenThereLiveKeys() throws PublicKeyStoreException {
        final PublicKeyStore publicKeyStore = Mockito.mock(PublicKeyStore.class);
        final List<UUID> list = new LinkedList<>();
        list.add(UUID.fromString("00000000-0000-0000-0000-000000005555"));
        Mockito.when(publicKeyStore.getLiveKeyUUIDs()).thenReturn(list);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(null, publicKeyStore);


        final Response response       = publicKeyRestService.getLiveKeyUUIDs();
        final List<UUID> receivedList = (List<UUID>) response.getEntity();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(1, receivedList.size());
        assertEquals(list, receivedList);
    }

    @Test
    public void getLivePublicKeys__shouldReturn_204_NO_CONTENT__whenThereAreNoLiveKeys() throws PublicKeyStoreException {
        final PublicKeyStore publicKeyStore = Mockito.mock(PublicKeyStore.class);
        final List<UUID> list = new LinkedList<>();
        Mockito.when(publicKeyStore.getLiveKeyUUIDs()).thenReturn(list);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(null, publicKeyStore);


        final Response response       = publicKeyRestService.getLiveKeyUUIDs();


        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void getLivePublicKeys__shouldReturn_503__whenExceptionIsThrown() throws PublicKeyStoreException {
        final PublicKeyStore publicKeyStore = Mockito.mock(PublicKeyStore.class);
        Mockito.when(publicKeyStore.getLiveKeyUUIDs()).thenThrow(PublicKeyStoreException.class);
        final PublicKeyRestService publicKeyRestService = new PublicKeyRestService(null, publicKeyStore);


        final Response response                       = publicKeyRestService.getLiveKeyUUIDs();
        final SingleErrorResponse singleErrorResponse = (SingleErrorResponse) response.getEntity();


        // TODO this should be single error response everywhere
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        //assertEquals("asd", singleErrorResponse.getError());
    }
}
