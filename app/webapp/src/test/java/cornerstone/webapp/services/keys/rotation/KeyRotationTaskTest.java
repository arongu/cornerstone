package cornerstone.webapp.services.keys.rotation;

import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStore;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStoreException;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStoreImpl;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStoreImpl;
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
    public void run_shouldGoThrowAllTheSteps_whenCalled() throws DatabaseKeyStoreException {
        final int rsaTTL       = 1000;
        final int jwtTTL       = 500;
        final String nodeName  = "mockito-node";
        final UUID uuid_A      = UUID.randomUUID();
        final UUID uuid_B      = UUID.randomUUID();
        final List<UUID> uuids = new LinkedList<>();
        uuids.add(uuid_A);
        uuids.add(uuid_B);
        // mocks
        final LocalKeyStore  localKeyStore  = Mockito.mock(LocalKeyStoreImpl.class);
        final DatabaseKeyStore publicKeyStore = Mockito.mock(DatabaseKeyStoreImpl.class);
        when(publicKeyStore.getLivePublicKeyUUIDs()).thenReturn(uuids);


        final KeyRotationTask keyRotationTask = new KeyRotationTask(localKeyStore, publicKeyStore, jwtTTL, rsaTTL, nodeName);
        keyRotationTask.run();


        verify(localKeyStore, times(1)).setSigningKeys(any(UUID.class), any(PrivateKey.class), any(PublicKey.class));
        verify(publicKeyStore, times(1)).addPublicKey(any(UUID.class), eq(nodeName), eq(rsaTTL + jwtTTL), anyString());
        verify(publicKeyStore, times(1)).getLivePublicKeyUUIDs();
        verify(localKeyStore, times(1)).keepOnlyPublicKeys(eq(uuids));
        verify(publicKeyStore, times(1)).deleteExpiredPublicKeys();
    }

    @Test
    public void run_shouldWriteAnErrorLog_whenAddKeyThrowsAnException() throws DatabaseKeyStoreException {
        final int rsaTTL      = 1000;
        final int jwtTTL      = 500;
        final String nodeName = "mockito-node";
        // mocks
        final LocalKeyStore  localKeyStore  = Mockito.mock(LocalKeyStoreImpl.class);
        final DatabaseKeyStore publicKeyStore = Mockito.mock(DatabaseKeyStoreImpl.class);
        when(publicKeyStore.addPublicKey(any(UUID.class), anyString(), anyInt(), anyString())).thenThrow(DatabaseKeyStoreException.class);


        assertDoesNotThrow(() -> {
            final KeyRotationTask keyRotationTask = new KeyRotationTask(localKeyStore, publicKeyStore, rsaTTL, jwtTTL, nodeName);
            keyRotationTask.run();
        });
    }

    @Test
    public void run_shouldWriteAnErrorLog_whenGetLiveKeyUUIDsThrowsAnException() throws DatabaseKeyStoreException {
        final int rsaTTL      = 1000;
        final int jwtTTL      = 500;
        final String nodeName = "mockito-node";
        // mocks
        final LocalKeyStore  localKeyStore  = Mockito.mock(LocalKeyStoreImpl.class);
        final DatabaseKeyStore publicKeyStore = Mockito.mock(DatabaseKeyStoreImpl.class);
        when(publicKeyStore.getLivePublicKeyUUIDs()).thenThrow(DatabaseKeyStoreException.class);


        assertDoesNotThrow(() -> {
            final KeyRotationTask keyRotationTask = new KeyRotationTask(localKeyStore, publicKeyStore, rsaTTL, jwtTTL, nodeName);
            keyRotationTask.run();
        });
    }
}
