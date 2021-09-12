package cornerstone.webapp.services.keys.stores.manager;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.services.keys.rotation.KeyPairWithUUID;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStore;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStoreException;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStoreImpl;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStoreImpl;
import cornerstone.webapp.services.keys.stores.local.SigningKeys;
import cornerstone.webapp.services.keys.stores.local.SigningKeysException;
import org.junit.jupiter.api.AfterAll;
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

        } catch (final IOException e) {
            e.printStackTrace();
        }
        System.out.println("... @BeforeAll END ...");
    }

    @AfterAll
    public static void cleanDB() throws DatabaseKeyStoreException {
        System.out.println("... @AfterAll (cleanDB) ...");
        final List<UUID> exp = databaseKeyStore.getExpiredPublicKeyUUIDs();
        final List<UUID> lv  = databaseKeyStore.getLivePublicKeyUUIDs();

        for (UUID e : exp) { databaseKeyStore.deletePublicKey(e); }
        for (UUID l : lv)  { databaseKeyStore.deletePublicKey(l); }
    }

    // --- addPublicKey
    /*
        [OK]        local keystore
        [OK]        database keystore
        [not touch] toAdd
        [not touch] toDelete
        PublicKey
     */
    @Test
    public void addPublicKey_shouldAddPublicKeyToLocalKeyStoreAndThenDatabaseKeyStore_whenEverythingIsOK_PublicKey() throws KeyManagerException, DatabaseKeyStoreException {
        final KeyManager keyManager   = new KeyManagerImpl(configLoader, localKeyStore, databaseKeyStore,null,null);
        final KeyPairWithUUID keyPair = new KeyPairWithUUID();
        final UUID uuid               = keyPair.uuid;
        final PublicKey pubKey        = keyPair.keyPair.getPublic();
        final String b64pubKey        = Base64.getEncoder().encodeToString(pubKey.getEncoded());


        keyManager.addPublicKey(uuid, pubKey);


        assertEquals(pubKey, localKeyStore.getPublicKey(uuid));
        assertEquals(b64pubKey, databaseKeyStore.getPublicKey(uuid).getBase64Key());
    }

    /*
        [OK]        local keystore
        [OK]        database keystore
        [not touch] toAdd
        [not touch] toDelete
        base64
     */
    @Test
    public void addPublicKey_shouldAddPublicKeyToLocalKeyStoreAndThenDatabaseKeyStore_whenEverythingIsOK_Base64() throws KeyManagerException, DatabaseKeyStoreException {
        final KeyManager keyManager   = new KeyManagerImpl(configLoader, localKeyStore, databaseKeyStore,null,null);
        final KeyPairWithUUID keyPair = new KeyPairWithUUID();
        final UUID uuid               = keyPair.uuid;
        final PublicKey pubKey        = keyPair.keyPair.getPublic();
        final String b64pubKey        = Base64.getEncoder().encodeToString(pubKey.getEncoded());


        keyManager.addPublicKey(uuid, b64pubKey);


        assertEquals(pubKey, localKeyStore.getPublicKey(uuid));
        assertEquals(b64pubKey, databaseKeyStore.getPublicKey(uuid).getBase64Key());
    }

    /*
        [throws exception] local keystore
        [should not call ] database keystore
        [not touch]        toAdd
        [not touch]        toDelete
        PublicKey null
     */
    @Test
    public void addPublicKey_shouldThrowKeyManagerException_whenPublicKeyIsNull() throws InvalidKeySpecException, NoSuchAlgorithmException, DatabaseKeyStoreException {
        final LocalKeyStore mockLocalKeyStore       = Mockito.mock(LocalKeyStore.class);
        Mockito.doThrow(new InvalidKeySpecException()).when(mockLocalKeyStore).addPublicKey(Mockito.any(), (String) Mockito.any());
        final DatabaseKeyStore mockDatabaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        Mockito.doReturn(1).when(mockDatabaseKeyStore).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));


        final KeyManager keyManager = new KeyManagerImpl(configLoader, mockLocalKeyStore, mockDatabaseKeyStore,null,null);


        assertThrows(KeyManagerException.class, () -> keyManager.addPublicKey(UUID.randomUUID(), (PublicKey) null));
        Mockito.verify(mockDatabaseKeyStore, Mockito.times(0)).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));
    }

    /*
        [throws exception] local keystore
        [should not call ] database keystore
        [not touch]        toAdd
        [not touch]        toDelete
        PublicKey invalid
    */
    @Test
    public void addPublicKey_shouldThrowKeyManagerException_whenPublicKeyIsInvalid_Base64() throws InvalidKeySpecException, NoSuchAlgorithmException, DatabaseKeyStoreException {
        final LocalKeyStore mockLocalKeyStore       = Mockito.mock(LocalKeyStore.class);
        Mockito.doThrow(new InvalidKeySpecException()).when(mockLocalKeyStore).addPublicKey(Mockito.any(), (String) Mockito.any());
        final DatabaseKeyStore mockDatabaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        Mockito.doReturn(1).when(mockDatabaseKeyStore).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));


        final KeyManager keyManager = new KeyManagerImpl(configLoader, mockLocalKeyStore, mockDatabaseKeyStore, null, null);


        assertThrows(KeyManagerException.class, () -> keyManager.addPublicKey(UUID.randomUUID(), "thisIsNotaKey"));
        Mockito.verify(mockDatabaseKeyStore, Mockito.times(0)).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));
    }

    /*
        [OK]               local keystore
        [throws exception] database keystore
        [should use cache] toAdd
        [not touch]        toDelete
        PublicKey valid
    */
    @Test
    public void addPublicKey_shouldCachePublicKeyForAddition_whenDatabaseThrowsException_PublicKey() throws DatabaseKeyStoreException, KeyManagerException, InvalidKeySpecException, NoSuchAlgorithmException {
        final Map<UUID,String> toAdd                = new HashMap<>();
        final LocalKeyStore mockLocalKeyStore       = Mockito.mock(LocalKeyStore.class);
        final DatabaseKeyStore mockDatabaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        Mockito.doThrow(new DatabaseKeyStoreException("DB error.")).when(mockDatabaseKeyStore).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.any(Integer.class), Mockito.any(String.class));
        final KeyPairWithUUID keyPair               = new KeyPairWithUUID();
        final String b64pubKey                      = Base64.getEncoder().encodeToString(keyPair.keyPair.getPublic().getEncoded());
        final KeyManager keyManager = new KeyManagerImpl(configLoader, mockLocalKeyStore, mockDatabaseKeyStore, toAdd, null);


        keyManager.addPublicKey(keyPair.uuid, keyPair.keyPair.getPublic());


        assertTrue(toAdd.containsValue(b64pubKey));
        Mockito.verify(mockLocalKeyStore,    Mockito.times(1)).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class));
        Mockito.verify(mockDatabaseKeyStore, Mockito.times(1)).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));
    }

    /*
        [OK]               local keystore
        [throws exception] database keystore
        [should use cache] toAdd
        [not touch]        toDelete
        base64key valid
    */
    @Test
    public void addPublicKey_shouldCachePublicKeyForAddition_whenDatabaseThrowsException_Base64() throws DatabaseKeyStoreException, KeyManagerException, InvalidKeySpecException, NoSuchAlgorithmException {
        final Map<UUID,String> toAdd                = new HashMap<>();
        final LocalKeyStore mockLocalKeyStore       = Mockito.mock(LocalKeyStore.class);
        final DatabaseKeyStore mockDatabaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        Mockito.doThrow(new DatabaseKeyStoreException("DB error.")).when(mockDatabaseKeyStore).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.any(Integer.class), Mockito.any(String.class));
        final KeyPairWithUUID keyPair               = new KeyPairWithUUID();
        final String b64pubKey                      = Base64.getEncoder().encodeToString(keyPair.keyPair.getPublic().getEncoded());


        final KeyManager keyManager = new KeyManagerImpl(configLoader, mockLocalKeyStore, mockDatabaseKeyStore, toAdd, null);
        keyManager.addPublicKey(keyPair.uuid, b64pubKey);


        assertTrue(toAdd.containsValue(b64pubKey));
        Mockito.verify(mockLocalKeyStore,    Mockito.times(1)).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class));
        Mockito.verify(mockDatabaseKeyStore, Mockito.times(1)).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));
    }
    // --- end of addPublicKey


    // -- deleteKey
    /*
        [OK]            local keystore
        [OK]            database keystore
        [should use 1x] toAdd
        [should use 1x] toDelete
    */
    @Test
    public void deleteKey_shouldDeletePublicKeyFromLocalKeyStoreAndDatabaseKeyStoreAndAlsoFromBothCaches_whenEverythingIsOK() throws KeyManagerException, DatabaseKeyStoreException {
        final Map<UUID,String> toAdd  = Mockito.mock(HashMap.class);
        final Set<UUID> toDelete      = Mockito.mock(HashSet.class);
        final LocalKeyStore lks       = Mockito.mock(LocalKeyStore.class);
        final DatabaseKeyStore dbs    = Mockito.mock(DatabaseKeyStore.class);
        final KeyManager keyManager   = new KeyManagerImpl(configLoader, lks, dbs, toAdd, toDelete);
        final UUID uuid               = UUID.randomUUID();


        keyManager.deletePublicKey(uuid);


        Mockito.verify(lks,      Mockito.times(1)).deletePublicKey(Mockito.eq(uuid));
        Mockito.verify(dbs,      Mockito.times(1)).deletePublicKey(Mockito.eq(uuid));
        Mockito.verify(toAdd,    Mockito.times(1)).remove(Mockito.eq(uuid));
        Mockito.verify(toDelete, Mockito.times(1)).remove(Mockito.eq(uuid));
    }

    /*
        [OK]               local keystore
        [throws exception] database keystore
        [should use it 1x] toAdd    - remove uuid
        [should use it 1x] toDelete - cache
    */
    @Test
    public void deleteKey_shouldRemoveUuidFromToAddAndAddUuidTotoDelete_whenDatabaseKeyStoreThrowsExceptionMocked() throws KeyManagerException, DatabaseKeyStoreException {
        final UUID uuid                             = UUID.randomUUID();
        final Map<UUID,String> mockAdd              = Mockito.mock(HashMap.class);
        final Set<UUID> mockDelete                  = Mockito.mock(HashSet.class);
        final LocalKeyStore mockLocalKeyStore       = Mockito.mock(LocalKeyStoreImpl.class);
        final DatabaseKeyStore mockDatabaseKeyStore = Mockito.mock(DatabaseKeyStoreImpl.class);
        Mockito.doThrow(new DatabaseKeyStoreException("DB error.")).when(mockDatabaseKeyStore).deletePublicKey(Mockito.any(UUID.class));
        final KeyManager keyManager = new KeyManagerImpl(configLoader, mockLocalKeyStore, mockDatabaseKeyStore, mockAdd, mockDelete);


        keyManager.deletePublicKey(uuid);


        Mockito.verify(mockLocalKeyStore, Mockito.times(1)).deletePublicKey(Mockito.eq(uuid));
        Mockito.verify(mockAdd,           Mockito.times(1)).remove(Mockito.eq(uuid));
        Mockito.verify(mockDelete,        Mockito.times(1)).add(Mockito.eq(uuid));
    }
    // -- end of deleteKey


    // getPublicKey
    /*
        [OK] local keystore
        [OK] database keystore
    */
    @Test
    public void getPublicKey_shouldTryToGetKeyFromLocalKeyStoreThenGoToDatabaseFetchItThenCacheIt_whenLocalKeyStoreDoesNotHaveTheKeyMock() throws NoSuchElementException, DatabaseKeyStoreException, KeyManagerException {
        final KeyPairWithUUID keyPairWithUUID = new KeyPairWithUUID();
        final UUID uuid                       = keyPairWithUUID.uuid;
        final PublicKey publicKey             = keyPairWithUUID.keyPair.getPublic();
        final String b64pubKey                = Base64.getEncoder().encodeToString(keyPairWithUUID.keyPair.getPublic().getEncoded());
        final LocalKeyStore localStore        = new LocalKeyStoreImpl();
        final KeyManager keyManager           = new KeyManagerImpl(configLoader, localStore, databaseKeyStore,null,null);
        databaseKeyStore.addPublicKey(uuid, this.getClass().getSimpleName(),300, b64pubKey);

        assertThrows(NoSuchElementException.class, () -> localStore.getPublicKey(uuid));
        assertEquals(publicKey, keyManager.getPublicKey(uuid));
        assertEquals(publicKey, localStore.getPublicKey(uuid));
    }

    /*
        [OK] local keystore
        [OK] database keystore
    */
    @Test
    public void getPublicKey_shouldTryToGetKeyFromLocalKeyStoreThenGoToDatabaseFetchItThenCacheIt_whenLocalKeyStoreDoesNotHaveTheKey() throws NoSuchElementException, DatabaseKeyStoreException, KeyManagerException {
        final KeyPairWithUUID keyPairWithUUID = new KeyPairWithUUID();
        final UUID uuid                       = keyPairWithUUID.uuid;
        final PublicKey publicKey             = keyPairWithUUID.keyPair.getPublic();
        final String b64pubKey                = Base64.getEncoder().encodeToString(keyPairWithUUID.keyPair.getPublic().getEncoded());
        final KeyManager keyManager           = new KeyManagerImpl(configLoader, localKeyStore, databaseKeyStore,null,null);
        databaseKeyStore.addPublicKey(uuid, this.getClass().getSimpleName(),300, b64pubKey);


        assertThrows(NoSuchElementException.class, () -> localKeyStore.getPublicKey(uuid)); // validate that is not in the local store
        assertEquals(publicKey, keyManager.getPublicKey(uuid));                             // keyManager should fetch it, and cache it in local store
        assertEquals(publicKey, localKeyStore.getPublicKey(uuid));                          // validate that key is in the local store
    }

    /*
        [OK]              local keystore
        [returns garbage] database keystore
    */
    @Test
    public void getPublicKey_shouldThrowKeyManagerException_whenDatabaseReturnsGarbageAndTriesToCacheTheBase64KeyInTheLocalStore() throws DatabaseKeyStoreException {
        final UUID uuid = UUID.randomUUID();
        final String garbage = Base64.getEncoder().encodeToString("garbage".getBytes());
        final KeyManager keyManager = new KeyManagerImpl(configLoader, localKeyStore, databaseKeyStore, null, null);
        databaseKeyStore.addPublicKey(uuid, this.getClass().getSimpleName(), 300, garbage);

        assertThrows(KeyManagerException.class, () -> keyManager.getPublicKey(uuid));
    }

    /*
        [OK]               local keystore
        [throws exception] database keystore
    */
    @Test
    public void getPublicKey_shouldThrowKeyManagerException_whenDatabaseThrowsException() throws DatabaseKeyStoreException {
        final UUID uuid = UUID.randomUUID();
        final DatabaseKeyStore mockDatabaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        Mockito.doThrow(new DatabaseKeyStoreException("DB error.")).when(mockDatabaseKeyStore).getPublicKey(Mockito.any(UUID.class));
        final KeyManager keyManager = new KeyManagerImpl(configLoader, localKeyStore, mockDatabaseKeyStore, null, null);

        assertThrows(KeyManagerException.class, () -> keyManager.getPublicKey(uuid));
    }
    // -- end of getPublicKey


    // getSigningKeys
    /*
        [OK] local keystore
        [OK] signing keys
    */
    @Test
    public void getSigningKeys_shouldCallLocalKeyStoreAndReturnSigningKeys_whenEverythingIsOK() throws SigningKeysException, KeyManagerException {
        final LocalKeyStore lks = new LocalKeyStoreImpl();
        final KeyPairWithUUID kp = new KeyPairWithUUID();
        lks.setSigningKeys(kp.uuid, kp.keyPair.getPrivate(), kp.keyPair.getPublic());
        final KeyManager keyManager = new KeyManagerImpl(null, lks,null,null,null);


        final SigningKeys signingKeys = keyManager.getSigningKeys();


        assertEquals(kp.keyPair.getPrivate(), signingKeys.privateKey);
        assertEquals(kp.keyPair.getPublic(), signingKeys.publicKey);
    }

    /*
        [OK]      local keystore
        [not set] signing keys
    */
    @Test
    public void getSigningKeys_shouldThrowSigningKeysException_whenSigningKeysAreNotSet() {
        final LocalKeyStore lks = new LocalKeyStoreImpl();
        final KeyManager keyManager = new KeyManagerImpl(null, lks,null,null,null);


        assertThrows(SigningKeysException.class, keyManager::getSigningKeys);
    }
    // -- end of getSigningKeys


    // removeExpiredKeys
    /*
        [OK] DatabaseKeyStore
    */
    @Test
    public void removeExpiredKeys_shouldCallDatabaseKeyStoreDeleteExpiredPublicKeys_whenRemoveExpiredKeysCalled() throws DatabaseKeyStoreException {
        final DatabaseKeyStore mockDatabaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        final KeyManager keyManager = new KeyManagerImpl(null, null, mockDatabaseKeyStore,null,null);


        keyManager.removeExpiredKeys();


        Mockito.verify(mockDatabaseKeyStore, Mockito.times(1)).deleteExpiredPublicKeys();
    }

    /*
        [throws exception] DatabaseKeyStore
    */
    @Test
    public void removeExpiredKeys_shouldThrowDatabaseKeyStoreException_whenRemoveExpiredKeysThrowsException() throws DatabaseKeyStoreException {
        final DatabaseKeyStore mockDatabaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        Mockito.doThrow(new DatabaseKeyStoreException("DB error.")).when(mockDatabaseKeyStore).deleteExpiredPublicKeys();
        final KeyManager keyManager = new KeyManagerImpl(null, null, mockDatabaseKeyStore,null,null);


        assertThrows(DatabaseKeyStoreException.class, keyManager::removeExpiredKeys);


        Mockito.verify(mockDatabaseKeyStore, Mockito.times(1)).deleteExpiredPublicKeys();
    }
    // -- end of removeExpiredKeys


    // setSigningKeys
    /*
        [OK] local keystore
        [OK] signing keys
    */
    @Test
    public void setSigningKeys_shouldCallLocalKeyStore_whenSetSigningKeysIsCalled() throws SigningKeysException, KeyManagerException {
        final KeyPairWithUUID kp = new KeyPairWithUUID();
        final LocalKeyStore lks  = new LocalKeyStoreImpl();
        final KeyManager keyManager = new KeyManagerImpl(null, lks,null,null,null);


        assertThrows(SigningKeysException.class, localKeyStore::getSigningKeys);
        lks.setSigningKeys(kp.uuid, kp.keyPair.getPrivate(), kp.keyPair.getPublic());
        final SigningKeys signingKeys = keyManager.getSigningKeys();
        assertEquals(kp.keyPair.getPublic(), signingKeys.publicKey);
        assertEquals(kp.keyPair.getPrivate(), signingKeys.privateKey);
    }
    // -- end of setSigningKeys


    // syncLiveKeys
    /*
        [OK] local keystore
        [OK] database keystore
        [OK] signing keys
    */
    @Test
    public void syncLiveKeys_shouldSyncLocalKeyStoreWithDatabase_whenEverythingIsOK() throws DatabaseKeyStoreException {
        final Base64.Encoder encoder                = Base64.getEncoder();
        final String nodeName                       = this.getClass().getSimpleName();
        final HashMap<UUID, PublicKey> expired_keys = new HashMap<>();
        final HashMap<UUID, PublicKey> live_keys    = new HashMap<>();
        final LocalKeyStore testLocalKeyStore       = new LocalKeyStoreImpl();

        // expired keys
        System.out.println("... expired keys ...");
        for ( int i = 0; i < 3; i++) {
            final KeyPairWithUUID k = new KeyPairWithUUID();
            expired_keys.put(k.uuid, k.keyPair.getPublic());
            testLocalKeyStore.addPublicKey(k.uuid, k.keyPair.getPublic());
            databaseKeyStore.addPublicKey(k.uuid, nodeName, -10000, encoder.encodeToString(k.keyPair.getPublic().getEncoded()));
        }
        // live keys
        System.out.println("... live keys ...");
        for ( int i = 0; i < 2; i++) {
            final KeyPairWithUUID k = new KeyPairWithUUID();
            live_keys.put(k.uuid, k.keyPair.getPublic());
            testLocalKeyStore.addPublicKey(k.uuid, k.keyPair.getPublic());
            databaseKeyStore.addPublicKey(k.uuid, nodeName, 30000, encoder.encodeToString(k.keyPair.getPublic().getEncoded()));
        }


        // key manager
        final KeyManager keyManager = new KeyManagerImpl(null, testLocalKeyStore, databaseKeyStore, null, null);
        keyManager.syncLiveKeys();


        // verify all the expired keys are gone from the local store
        System.out.println("... verify expired keys are removed ...");
        for ( final Map.Entry<UUID, PublicKey> e : expired_keys.entrySet()) {
            assertThrows(NoSuchElementException.class, () -> testLocalKeyStore.getPublicKey(e.getKey()));
        }

        // verify all the live keys are present in the local store
        System.out.println("... verify live keys are present ...");
        for ( final Map.Entry<UUID, PublicKey> e : live_keys.entrySet()) {
            assertEquals(e.getValue(), testLocalKeyStore.getPublicKey(e.getKey()));
        }
    }

    /*
        [OK]               local keystore
        [OK]               signing keys
        [throws exception] database keystore
    */
    @Test
    public void syncLiveKeys_shouldThrowDatabasKeyStoreException_whenDatabaseThrowsException() throws DatabaseKeyStoreException {
        final HashMap<UUID, PublicKey> expired_keys = new HashMap<>();
        final HashMap<UUID, PublicKey> live_keys    = new HashMap<>();
        final LocalKeyStore testLocalKeyStore       = new LocalKeyStoreImpl();
        final DatabaseKeyStore mockDatabaseKeyStore  = Mockito.mock(DatabaseKeyStore.class);
        Mockito.doThrow(new DatabaseKeyStoreException("DB error.")).when(mockDatabaseKeyStore).getLivePublicKeyUUIDs();

        // expired keys
        System.out.println("... expired keys ...");
        for ( int i = 0; i < 3; i++) {
            final KeyPairWithUUID k = new KeyPairWithUUID();
            expired_keys.put(k.uuid, k.keyPair.getPublic());
            testLocalKeyStore.addPublicKey(k.uuid, k.keyPair.getPublic());
        }
        // live keys
        System.out.println("... live keys ...");
        for ( int i = 0; i < 2; i++) {
            final KeyPairWithUUID k = new KeyPairWithUUID();
            live_keys.put(k.uuid, k.keyPair.getPublic());
            testLocalKeyStore.addPublicKey(k.uuid, k.keyPair.getPublic());
        }


        // key manager
        final KeyManager keyManager = new KeyManagerImpl(null, testLocalKeyStore, mockDatabaseKeyStore, null, null);
        assertThrows(DatabaseKeyStoreException.class, keyManager::syncLiveKeys);


        // verify all the expired keys are still present from the local store
        System.out.println("... verify expired keys are still present ...");
        for ( final Map.Entry<UUID, PublicKey> e : expired_keys.entrySet()) {
            assertEquals(e.getValue(), testLocalKeyStore.getPublicKey(e.getKey()));
        }

        // verify all the live keys are present in the local store
        System.out.println("... verify live keys are also present ...");
        for ( final Map.Entry<UUID, PublicKey> e : live_keys.entrySet()) {
            assertEquals(e.getValue(), testLocalKeyStore.getPublicKey(e.getKey()));
        }
    }
    // -- end of syncLiveKeys
}
