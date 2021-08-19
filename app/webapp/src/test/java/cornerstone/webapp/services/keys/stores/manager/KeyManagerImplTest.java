package cornerstone.webapp.services.keys.stores.manager;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.services.keys.rotation.KeyPairWithUUID;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStore;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStoreException;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStoreImpl;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStoreImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class KeyManagerImplTest {
    private static ConfigLoader configLoader;
    private static LocalKeyStore localKeyStore;
    private static DatabaseKeyStore databaseKeyStore;

    @BeforeAll
    public static void init() {
        final String test_config_dir = "../../_test_config/";
        final String key_file        = Paths.get(test_config_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String conf_file       = Paths.get(test_config_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            configLoader                          = new ConfigLoader(key_file, conf_file);
            localKeyStore                         = new LocalKeyStoreImpl();
            databaseKeyStore                      = new DatabaseKeyStoreImpl(new WorkDB(configLoader));
            final KeyPairWithUUID keyPairWithUUID = new KeyPairWithUUID();
            localKeyStore.setSigningKeys(keyPairWithUUID.uuid, keyPairWithUUID.keyPair.getPrivate(), keyPairWithUUID.keyPair.getPublic());

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /*
        [OK] local keystore
        [OK] database keystore
        PublicKey
     */
    @Test
    public void addPublicKey_shouldAddPublicKeyToLocalKeyStoreAndThenDatabaseKeyStore_whenEverythingIsOK_PublicKey() throws KeyManagerException, DatabaseKeyStoreException {
        final KeyManager keyManager = new KeyManagerImpl(configLoader, localKeyStore, databaseKeyStore, null, null);
        final KeyPairWithUUID keyPairWithUUID = new KeyPairWithUUID();
        final PublicKey publicKey = keyPairWithUUID.keyPair.getPublic();
        final UUID uuid = keyPairWithUUID.uuid;
        final String b64PublicKey = Base64.getEncoder().encodeToString(keyPairWithUUID.keyPair.getPublic().getEncoded());


        keyManager.addPublicKey(uuid, publicKey);


        assertEquals(publicKey, localKeyStore.getPublicKey(uuid));
        assertEquals(b64PublicKey, databaseKeyStore.getPublicKey(uuid).getBase64Key());
    }

    /*
        [OK] local keystore
        [OK] database keystore
        base64
     */
    @Test
    public void addPublicKey_shouldAddPublicKeyToLocalKeyStoreAndThenDatabaseKeyStore_whenEverythingIsOK_Base64() throws KeyManagerException, DatabaseKeyStoreException {
        final KeyManager keyManager = new KeyManagerImpl(configLoader, localKeyStore, databaseKeyStore, null, null);
        final KeyPairWithUUID keyPairWithUUID = new KeyPairWithUUID();
        final PublicKey publicKey = keyPairWithUUID.keyPair.getPublic();
        final UUID uuid = keyPairWithUUID.uuid;
        final String b64PublicKey = Base64.getEncoder().encodeToString(keyPairWithUUID.keyPair.getPublic().getEncoded());


        keyManager.addPublicKey(uuid, b64PublicKey);


        assertEquals(publicKey, localKeyStore.getPublicKey(uuid));
        assertEquals(b64PublicKey, databaseKeyStore.getPublicKey(uuid).getBase64Key());
    }

    /*
        [throws] local keystore
        [should not call] database keystore
        PublicKey null
     */
    @Test
    public void addPublicKey_shouldThrowKeyManagerException_whenPublicKeyIsNull() throws InvalidKeySpecException, NoSuchAlgorithmException, DatabaseKeyStoreException {
        final LocalKeyStore mockLocalKeyStore       = Mockito.mock(LocalKeyStore.class);
        Mockito.doThrow(new InvalidKeySpecException()).when(mockLocalKeyStore).addPublicKey(Mockito.any(), (String) Mockito.any());
        final DatabaseKeyStore mockDatabaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        Mockito.doReturn(1).when(mockDatabaseKeyStore).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));


        final KeyManager keyManager = new KeyManagerImpl(configLoader, mockLocalKeyStore, mockDatabaseKeyStore, null, null);


        assertThrows(KeyManagerException.class, () -> keyManager.addPublicKey(UUID.randomUUID(), (PublicKey) null));
        Mockito.verify(mockDatabaseKeyStore, Mockito.times(0)).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));
    }

    /*
        [throws] local keystore
        [should not call] database keystore
        PublicKey invalid
    */
    @Test
    public void addPublicKey_shouldThrowKeyManagerException_whenPublicKeyIsInvalid() throws InvalidKeySpecException, NoSuchAlgorithmException, DatabaseKeyStoreException {
        final LocalKeyStore mockLocalKeyStore       = Mockito.mock(LocalKeyStore.class);
        Mockito.doThrow(new InvalidKeySpecException()).when(mockLocalKeyStore).addPublicKey(Mockito.any(), (String) Mockito.any());
        final DatabaseKeyStore mockDatabaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        Mockito.doReturn(1).when(mockDatabaseKeyStore).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));


        final KeyManager keyManager = new KeyManagerImpl(configLoader, mockLocalKeyStore, mockDatabaseKeyStore, null, null);

        assertThrows(KeyManagerException.class, () -> keyManager.addPublicKey(UUID.randomUUID(), "thisIsNotaKey"));
        Mockito.verify(mockDatabaseKeyStore, Mockito.times(0)).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));
    }

    /*
        [throws] local keystore
        [should call 1x ] database keystore
        [should use cache] cache
        PublicKey valid
    */
    @Test
    public void addPublicKey_shouldCachePublicKeyForAddition_whenDatabaseThrowsException() throws DatabaseKeyStoreException, KeyManagerException {
        final Map<UUID,String> toAdd                = new HashMap<>();
        final LocalKeyStore mockLocalKeyStore       = Mockito.mock(LocalKeyStore.class);
        final DatabaseKeyStore mockDatabaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        Mockito.doThrow(new DatabaseKeyStoreException("DB error.")).when(mockDatabaseKeyStore).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.any(Integer.class), Mockito.any(String.class));
        final KeyPairWithUUID keyPairWithUUID       = new KeyPairWithUUID();
        final String base64key                      = Base64.getEncoder().encodeToString(keyPairWithUUID.keyPair.getPublic().getEncoded());


        final KeyManager keyManager = new KeyManagerImpl(configLoader, mockLocalKeyStore, mockDatabaseKeyStore, toAdd, null);
        keyManager.addPublicKey(keyPairWithUUID.uuid, keyPairWithUUID.keyPair.getPublic());


        assertTrue(toAdd.containsValue(base64key));
        Mockito.verify(mockDatabaseKeyStore, Mockito.times(1)).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));
    }
}
