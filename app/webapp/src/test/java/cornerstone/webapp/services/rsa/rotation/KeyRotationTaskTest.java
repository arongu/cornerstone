package cornerstone.webapp.services.rsa.rotation;

import cornerstone.webapp.services.rsa.store.db.PublicKeyStore;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreException;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreImpl;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStore;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStoreImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

public class KeyRotationTaskTest {
    @Test
    public void run_shouldGoThrowAllTheSteps_whenCalled() throws PublicKeyStoreException {
        final int given_rsa_ttl = 1000;
        final int given_jwt_ttl = 500;
        final String given_node_name = "mockito-node";
        final UUID given_uuid_a = UUID.randomUUID();
        final UUID given_uuid_b = UUID.randomUUID();
        final List<UUID> given_uuid_list = new LinkedList<>();
        given_uuid_list.add(given_uuid_a);
        given_uuid_list.add(given_uuid_b);

        final LocalKeyStore  local_key_store  = Mockito.mock(LocalKeyStoreImpl.class);
        final PublicKeyStore public_key_store = Mockito.mock(PublicKeyStoreImpl.class);
        when(public_key_store.getLiveKeyUUIDs()).thenReturn(given_uuid_list);



        final KeyRotationTask keyRotationTask = new KeyRotationTask(local_key_store, public_key_store, given_jwt_ttl, given_rsa_ttl, given_node_name);
        keyRotationTask.run();



        verify(local_key_store, times(1)).setPublicAndPrivateKeys(any(UUID.class), any(PrivateKey.class), any(PublicKey.class));
        verify(public_key_store, times(1)).addKey(any(UUID.class), eq(given_node_name), eq(given_rsa_ttl + given_jwt_ttl), anyString());
        verify(public_key_store, times(1)).getLiveKeyUUIDs();
        verify(local_key_store, times(1)).sync(eq(given_uuid_list));
        verify(public_key_store, times(1)).deleteExpiredKeys();
    }

    @Test
    public void run_shouldWriteAnErrorLog_whenAddKeyThrowsAnException() throws PublicKeyStoreException {
        final int given_rsa_ttl = 1000;
        final int given_jwt_ttl = 500;
        final String given_node_name = "mockito-node";


        final LocalKeyStore  local_key_store  = Mockito.mock(LocalKeyStoreImpl.class);
        final PublicKeyStore public_key_store = Mockito.mock(PublicKeyStoreImpl.class);
        when(public_key_store.addKey(any(UUID.class), anyString(), anyInt(), anyString())).thenThrow(PublicKeyStoreException.class);


        assertDoesNotThrow(() -> {
            final KeyRotationTask keyRotationTask = new KeyRotationTask(local_key_store, public_key_store, given_rsa_ttl, given_jwt_ttl, given_node_name);
            keyRotationTask.run();
        });
    }

    @Test
    public void run_shouldWriteAnErrorLog_whenGetLiveKeyUUIDsThrowsAnException() throws PublicKeyStoreException {
        final int given_rsa_ttl = 1000;
        final int given_jwt_ttl = 500;
        final String given_node_name = "mockito-node";


        final LocalKeyStore  local_key_store  = Mockito.mock(LocalKeyStoreImpl.class);
        final PublicKeyStore public_key_store = Mockito.mock(PublicKeyStoreImpl.class);
        when(public_key_store.getLiveKeyUUIDs()).thenThrow(PublicKeyStoreException.class);


        assertDoesNotThrow(() -> {
            final KeyRotationTask keyRotationTask = new KeyRotationTask(local_key_store, public_key_store, given_rsa_ttl, given_jwt_ttl, given_node_name);
            keyRotationTask.run();
        });
    }
}
