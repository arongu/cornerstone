package cornerstone.webapp.service.rsa.rotation;

import cornerstone.webapp.service.rsa.store.db.PublicKeyStore;
import cornerstone.webapp.service.rsa.store.db.PublicKeyStoreException;
import cornerstone.webapp.service.rsa.store.db.PublicKeyStoreImpl;
import cornerstone.webapp.service.rsa.store.local.LocalKeyStore;
import cornerstone.webapp.service.rsa.store.local.LocalKeyStoreImpl;
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
        final int rsaTTL = 1000;
        final int jwtTTL = 500;
        final String nodeName = "mockito-node";
        final UUID uuid_A = UUID.randomUUID();
        final UUID uuid_B = UUID.randomUUID();
        final List<UUID> uuids = new LinkedList<>();
        uuids.add(uuid_A);
        uuids.add(uuid_B);

        final LocalKeyStore  localKeyStore  = Mockito.mock(LocalKeyStoreImpl.class);
        final PublicKeyStore publicKeyStore = Mockito.mock(PublicKeyStoreImpl.class);
        when(publicKeyStore.getLiveKeyUUIDs()).thenReturn(uuids);



        final KeyRotationTask keyRotationTask = new KeyRotationTask(localKeyStore, publicKeyStore, jwtTTL, rsaTTL, nodeName);
        keyRotationTask.run();



        verify(localKeyStore, times(1)).setLiveKeys(any(UUID.class), any(PrivateKey.class), any(PublicKey.class));
        verify(publicKeyStore, times(1)).addKey(any(UUID.class), eq(nodeName), eq(rsaTTL + jwtTTL), anyString());
        verify(publicKeyStore, times(1)).getLiveKeyUUIDs();
        verify(localKeyStore, times(1)).sync(eq(uuids));
        verify(publicKeyStore, times(1)).deleteExpiredKeys();
    }

    @Test
    public void run_shouldWriteAnErrorLog_whenAddKeyThrowsAnException() throws PublicKeyStoreException {
        final int rsaTTL = 1000;
        final int jwtTTL = 500;
        final String nodeName = "mockito-node";


        final LocalKeyStore  localKeyStore  = Mockito.mock(LocalKeyStoreImpl.class);
        final PublicKeyStore publicKeyStore = Mockito.mock(PublicKeyStoreImpl.class);
        when(publicKeyStore.addKey(any(UUID.class), anyString(), anyInt(), anyString())).thenThrow(PublicKeyStoreException.class);


        assertDoesNotThrow(() -> {
            final KeyRotationTask keyRotationTask = new KeyRotationTask(localKeyStore, publicKeyStore, rsaTTL, jwtTTL, nodeName);
            keyRotationTask.run();
        });
    }

    @Test
    public void run_shouldWriteAnErrorLog_whenGetLiveKeyUUIDsThrowsAnException() throws PublicKeyStoreException {
        final int rsaTTL = 1000;
        final int jwtTTL = 500;
        final String nodeName = "mockito-node";


        final LocalKeyStore  localKeyStore  = Mockito.mock(LocalKeyStoreImpl.class);
        final PublicKeyStore publicKeyStore = Mockito.mock(PublicKeyStoreImpl.class);
        when(publicKeyStore.getLiveKeyUUIDs()).thenThrow(PublicKeyStoreException.class);


        assertDoesNotThrow(() -> {
            final KeyRotationTask keyRotationTask = new KeyRotationTask(localKeyStore, publicKeyStore, rsaTTL, jwtTTL, nodeName);
            keyRotationTask.run();
        });
    }
}
