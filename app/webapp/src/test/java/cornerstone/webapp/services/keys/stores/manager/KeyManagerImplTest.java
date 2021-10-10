package cornerstone.webapp.services.keys.stores.manager;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.services.keys.common.PublicKeyData;
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
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class KeyManagerImplTest {
    private static ConfigLoader     CONFIG_LOADER;
    private static LocalKeyStore    LOCAL_KEYSTORE;
    private static DatabaseKeyStore DATABASE_KEYSTORE;

    @BeforeAll
    public static void init() {
        final String test_config_dir = System.getenv("CONFIG_DIR");
        final String key_file        = Paths.get(test_config_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String conf_file       = Paths.get(test_config_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            CONFIG_LOADER     = new ConfigLoader(key_file, conf_file);
            LOCAL_KEYSTORE    = new LocalKeyStoreImpl();
            DATABASE_KEYSTORE = new DatabaseKeyStoreImpl(new WorkDB(CONFIG_LOADER));

        } catch (final IOException e) {
            e.printStackTrace();
        }
        System.out.println("... @BeforeAll END ...");
    }

    @AfterAll
    public static void cleanDB() throws DatabaseKeyStoreException {
        System.out.println("... @AfterAll (cleanDB) ...");
        final List<UUID> expiredUUIDs = DATABASE_KEYSTORE.getExpiredPublicKeyUUIDs();
        final List<UUID> liveUUIDS    = DATABASE_KEYSTORE.getLivePublicKeyUUIDs();

        for (UUID e : expiredUUIDs) { DATABASE_KEYSTORE.deletePublicKey(e); }
        for (UUID l : liveUUIDS)    { DATABASE_KEYSTORE.deletePublicKey(l); }
    }

    // --- addPublicKey
    /*
        [OK]        local keystore
        [OK]        database keystore
        [not touch] toAdd
        [not touch] toDelete
        [valid]     public key
     */
    @Test
    public void addPublicKey_shouldAddPublicKeyToLocalKeyStoreAndThenDatabaseKeyStore_whenEverythingIsOK_PublicKey() throws KeyManagerException, DatabaseKeyStoreException {
        final KeyManager      keyManager      = new KeyManagerImpl(CONFIG_LOADER, LOCAL_KEYSTORE, DATABASE_KEYSTORE,null,null);
        final KeyPairWithUUID keyPair         = new KeyPairWithUUID();
        final UUID            uuid            = keyPair.uuid;
        final PublicKey       publicKey       = keyPair.keyPair.getPublic();
        final String          base64PublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());


        keyManager.addPublicKey(uuid, publicKey);


        assertEquals(publicKey, LOCAL_KEYSTORE.getPublicKey(uuid));
        assertEquals(base64PublicKey, DATABASE_KEYSTORE.getPublicKey(uuid).getBase64Key());
    }

    /*
        [OK]        local keystore
        [OK]        database keystore
        [not touch] toAdd
        [not touch] toDelete
        (base64)
     */
    @Test
    public void addPublicKey_shouldAddPublicKeyToLocalKeyStoreAndThenDatabaseKeyStore_whenEverythingIsOK_Base64() throws KeyManagerException, DatabaseKeyStoreException {
        final KeyManager      keyManager      = new KeyManagerImpl(CONFIG_LOADER, LOCAL_KEYSTORE, DATABASE_KEYSTORE,null,null);
        final KeyPairWithUUID keyPair         = new KeyPairWithUUID();
        final UUID            uuid            = keyPair.uuid;
        final PublicKey       pubKey          = keyPair.keyPair.getPublic();
        final String          base64PublicKey = Base64.getEncoder().encodeToString(pubKey.getEncoded());


        keyManager.addPublicKey(uuid, base64PublicKey);


        assertEquals(pubKey, LOCAL_KEYSTORE.getPublicKey(uuid));
        assertEquals(base64PublicKey, DATABASE_KEYSTORE.getPublicKey(uuid).getBase64Key());
    }

    /*
        [throws exception] local keystore
        [should not call ] database keystore
        [not touch]        toAdd
        [not touch]        toDelete
        [null]             public key
     */
    @Test
    public void addPublicKey_shouldThrowKeyManagerException_whenPublicKeyIsNull() throws InvalidKeySpecException, NoSuchAlgorithmException, DatabaseKeyStoreException {
        final LocalKeyStore    localKeyStore    = Mockito.mock(LocalKeyStore.class);
        final DatabaseKeyStore databaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        // mocks
        Mockito.doThrow(new InvalidKeySpecException())
               .when(localKeyStore).addPublicKey(Mockito.any(), (String) Mockito.any());
        Mockito.doReturn(1)
               .when(databaseKeyStore).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));


        final KeyManager keyManager = new KeyManagerImpl(CONFIG_LOADER, localKeyStore, databaseKeyStore,null,null);


        assertThrows(KeyManagerException.class, () -> keyManager.addPublicKey(UUID.randomUUID(), (PublicKey) null));
        Mockito.verify(databaseKeyStore, Mockito.times(0)).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));
    }

    /*
        [throws exception] local keystore
        [should not call]  database keystore
        [not touch]        toAdd
        [not touch]        toDelete
        [invalid]          public key
    */
    @Test
    public void addPublicKey_shouldThrowKeyManagerException_whenPublicKeyIsInvalid_Base64() throws InvalidKeySpecException, NoSuchAlgorithmException, DatabaseKeyStoreException {
        final LocalKeyStore    localKeyStore    = Mockito.mock(LocalKeyStore.class);
        final DatabaseKeyStore databaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        // mocks
        Mockito.doThrow(new InvalidKeySpecException())
               .when(localKeyStore).addPublicKey(Mockito.any(), (String) Mockito.any());
        Mockito.doReturn(1)
               .when(databaseKeyStore).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));


        final KeyManager keyManager = new KeyManagerImpl(CONFIG_LOADER, localKeyStore, databaseKeyStore, null, null);


        assertThrows(KeyManagerException.class, () -> keyManager.addPublicKey(UUID.randomUUID(), "thisIsNotaKey"));
        Mockito.verify(databaseKeyStore, Mockito.times(0)).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));
    }

    /*
        [OK]               local keystore
        [throws exception] database keystore
        [should use cache] toAdd
        [not touch]        toDelete
        [valid]            public key
    */
    @Test
    public void addPublicKey_shouldCachePublicKeyForAddition_whenDatabaseThrowsException_PublicKey() throws DatabaseKeyStoreException, KeyManagerException, InvalidKeySpecException, NoSuchAlgorithmException {
        final KeyPairWithUUID  keyPair          = new KeyPairWithUUID();
        final String           base64PublicKey  = Base64.getEncoder().encodeToString(keyPair.keyPair.getPublic().getEncoded());
        final Map<UUID,String> toAdd            = new HashMap<>();
        final LocalKeyStore    localKeyStore    = Mockito.mock(LocalKeyStore.class);
        final DatabaseKeyStore databaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        final KeyManager       keyManager       = new KeyManagerImpl(CONFIG_LOADER, localKeyStore, databaseKeyStore, toAdd, null);
        // mocks
        Mockito.doThrow(new DatabaseKeyStoreException("DB error."))
               .when(databaseKeyStore).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.any(Integer.class), Mockito.any(String.class));


        keyManager.addPublicKey(keyPair.uuid, keyPair.keyPair.getPublic());


        assertTrue(toAdd.containsValue(base64PublicKey));
        Mockito.verify(localKeyStore,    Mockito.times(1)).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class));
        Mockito.verify(databaseKeyStore, Mockito.times(1)).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));
    }

    /*
        [OK]               local keystore
        [throws exception] database keystore
        [should use cache] toAdd
        [not touch]        toDelete
        [valid]            base64key
    */
    @Test
    public void addPublicKey_shouldCachePublicKeyForAddition_whenDatabaseThrowsException_Base64() throws DatabaseKeyStoreException, KeyManagerException, InvalidKeySpecException, NoSuchAlgorithmException {
        final Map<UUID,String> toAdd                = new HashMap<>();
        final KeyPairWithUUID  keyPair              = new KeyPairWithUUID();
        final String           base64PublicKey      = Base64.getEncoder().encodeToString(keyPair.keyPair.getPublic().getEncoded());
        final LocalKeyStore    localKeyStore        = Mockito.mock(LocalKeyStore.class);
        final DatabaseKeyStore databaseKeyStore     = Mockito.mock(DatabaseKeyStore.class);
        // mocks
        Mockito.doThrow(new DatabaseKeyStoreException("DB error."))
               .when(databaseKeyStore).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.any(Integer.class), Mockito.any(String.class));


        final KeyManager keyManager = new KeyManagerImpl(CONFIG_LOADER, localKeyStore, databaseKeyStore, toAdd, null);
        keyManager.addPublicKey(keyPair.uuid, base64PublicKey);


        assertTrue(toAdd.containsValue(base64PublicKey));
        Mockito.verify(localKeyStore,    Mockito.times(1)).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class));
        Mockito.verify(databaseKeyStore, Mockito.times(1)).addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class));
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
        final Map<UUID,String> toAdd            = Mockito.mock(HashMap.class);
        final Set<UUID>        toDelete         = Mockito.mock(HashSet.class);
        final LocalKeyStore    localKeyStore    = Mockito.mock(LocalKeyStore.class);
        final DatabaseKeyStore databaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        final UUID             uuid             = UUID.randomUUID();
        final KeyManager       keyManager       = new KeyManagerImpl(CONFIG_LOADER, localKeyStore, databaseKeyStore, toAdd, toDelete);


        keyManager.deletePublicKey(uuid);


        Mockito.verify(localKeyStore,    Mockito.times(1)).deletePublicKey(Mockito.eq(uuid));
        Mockito.verify(databaseKeyStore, Mockito.times(1)).deletePublicKey(Mockito.eq(uuid));
        Mockito.verify(toAdd,            Mockito.times(1)).remove(Mockito.eq(uuid));
        Mockito.verify(toDelete,         Mockito.times(1)).remove(Mockito.eq(uuid));
    }

    /*
        [OK]               local keystore
        [throws exception] database keystore
        [should use it 1x] toAdd    - remove uuid
        [should use it 1x] toDelete - cache
    */
    @Test
    public void deleteKey_shouldRemoveUuidFromToAddAndAddUuidTotoDelete_whenDatabaseKeyStoreThrowsExceptionMocked() throws KeyManagerException, DatabaseKeyStoreException {
        final UUID             uuid             = UUID.randomUUID();
        final Map<UUID,String> toAdd            = Mockito.mock(HashMap.class);
        final Set<UUID>        toDelete         = Mockito.mock(HashSet.class);
        final LocalKeyStore    localKeyStore    = Mockito.mock(LocalKeyStoreImpl.class);
        final DatabaseKeyStore databaseKeyStore = Mockito.mock(DatabaseKeyStoreImpl.class);
        // mocks
        Mockito.doThrow(new DatabaseKeyStoreException("DB error."))
               .when(databaseKeyStore).deletePublicKey(Mockito.any(UUID.class));
        final KeyManager keyManager = new KeyManagerImpl(CONFIG_LOADER, localKeyStore, databaseKeyStore, toAdd, toDelete);


        keyManager.deletePublicKey(uuid);


        Mockito.verify(localKeyStore, Mockito.times(1)).deletePublicKey(Mockito.eq(uuid));
        Mockito.verify(toAdd,         Mockito.times(1)).remove(Mockito.eq(uuid));
        Mockito.verify(toDelete,      Mockito.times(1)).add(Mockito.eq(uuid));
    }
    // -- end of deleteKey


    // getPublicKey
    /*
        [OK] local keystore
        [OK] database keystore
    */
    @Test
    public void getPublicKey_shouldTryToGetKeyFromLocalKeyStoreThenGoToDatabaseFetchItThenCacheIt_whenLocalKeyStoreDoesNotHaveTheKeyMock() throws NoSuchElementException, DatabaseKeyStoreException, KeyManagerException {
        final KeyPairWithUUID keyPair         = new KeyPairWithUUID();
        final UUID            uuid            = keyPair.uuid;
        final PublicKey       publicKey       = keyPair.keyPair.getPublic();
        final String          base64PublicKey = Base64.getEncoder().encodeToString(keyPair.keyPair.getPublic().getEncoded());
        final LocalKeyStore   localKeyStore   = new LocalKeyStoreImpl();
        final KeyManager      keyManager      = new KeyManagerImpl(CONFIG_LOADER, localKeyStore, DATABASE_KEYSTORE,null,null);
        DATABASE_KEYSTORE.addPublicKey(uuid, this.getClass().getSimpleName(),300, base64PublicKey);

        assertThrows(NoSuchElementException.class, () -> localKeyStore.getPublicKey(uuid));
        assertEquals(publicKey, keyManager.getPublicKey(uuid));
        assertEquals(publicKey, localKeyStore.getPublicKey(uuid));
    }

    /*
        [OK] local keystore
        [OK] database keystore
    */
    @Test
    public void getPublicKey_shouldTryToGetKeyFromLocalKeyStoreThenGoToDatabaseFetchItThenCacheIt_whenLocalKeyStoreDoesNotHaveTheKey() throws NoSuchElementException, DatabaseKeyStoreException, KeyManagerException {
        final KeyPairWithUUID keyPair         = new KeyPairWithUUID();
        final UUID            uuid            = keyPair.uuid;
        final PublicKey       publicKey       = keyPair.keyPair.getPublic();
        final String          base64PublicKey = Base64.getEncoder().encodeToString(keyPair.keyPair.getPublic().getEncoded());
        final KeyManager      keyManager      = new KeyManagerImpl(CONFIG_LOADER, LOCAL_KEYSTORE, DATABASE_KEYSTORE,null,null);
        DATABASE_KEYSTORE.addPublicKey(uuid, this.getClass().getSimpleName(),300, base64PublicKey);


        assertThrows(NoSuchElementException.class, () -> LOCAL_KEYSTORE.getPublicKey(uuid)); // validate that is not in the local store
        assertEquals(publicKey, keyManager.getPublicKey(uuid));                             // keyManager should fetch it, and cache it in local store
        assertEquals(publicKey, LOCAL_KEYSTORE.getPublicKey(uuid));                          // validate that key is in the local store
    }

    /*
        [OK]              local keystore
        [returns garbage] database keystore
    */
    @Test
    public void getPublicKey_shouldThrowKeyManagerException_whenDatabaseReturnsGarbageAndTriesToCacheTheBase64KeyInTheLocalStore() throws DatabaseKeyStoreException {
        final UUID       uuid                   = UUID.randomUUID();
        final String     base64InvalidPublicKey = Base64.getEncoder().encodeToString("garbage".getBytes());
        final KeyManager keyManager             = new KeyManagerImpl(CONFIG_LOADER, LOCAL_KEYSTORE, DATABASE_KEYSTORE, null, null);
        DATABASE_KEYSTORE.addPublicKey(uuid, this.getClass().getSimpleName(), 300, base64InvalidPublicKey);

        assertThrows(KeyManagerException.class, () -> keyManager.getPublicKey(uuid));
    }

    /*
        [OK]               local keystore
        [throws exception] database keystore
    */
    @Test
    public void getPublicKey_shouldThrowKeyManagerException_whenDatabaseThrowsException() throws DatabaseKeyStoreException {
        final UUID             uuid             = UUID.randomUUID();
        final DatabaseKeyStore databaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        final KeyManager       keyManager       = new KeyManagerImpl(CONFIG_LOADER, LOCAL_KEYSTORE, databaseKeyStore, null, null);
        // mocks
        Mockito.doThrow(new DatabaseKeyStoreException("DB error."))
               .when(databaseKeyStore).getPublicKey(Mockito.any(UUID.class));

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
        final LocalKeyStore   localKeyStore = new LocalKeyStoreImpl();
        final KeyPairWithUUID keyPair       = new KeyPairWithUUID();
        final KeyManager      keyManager    = new KeyManagerImpl(null, localKeyStore,null,null,null);
        localKeyStore.setSigningKeys(keyPair.uuid, keyPair.keyPair.getPrivate(), keyPair.keyPair.getPublic());


        final SigningKeys signingKeys = keyManager.getSigningKeys();


        assertEquals(keyPair.keyPair.getPrivate(), signingKeys.privateKey);
        assertEquals(keyPair.keyPair.getPublic(),  signingKeys.publicKey);
    }

    /*
        [OK]      local keystore
        [not set] signing keys
    */
    @Test
    public void getSigningKeys_shouldThrowSigningKeysException_whenSigningKeysAreNotSet() {
        final LocalKeyStore localKeyStore = new LocalKeyStoreImpl();
        final KeyManager    keyManager    = new KeyManagerImpl(null, localKeyStore,null,null,null);


        assertThrows(SigningKeysException.class, keyManager::getSigningKeys);
    }
    // -- end of getSigningKeys


    // removeExpiredKeys
    /*
        [OK] DatabaseKeyStore
    */
    @Test
    public void removeExpiredKeys_shouldCallDatabaseKeyStoreDeleteExpiredPublicKeys_whenRemoveExpiredKeysCalled() throws DatabaseKeyStoreException {
        final DatabaseKeyStore databaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        final KeyManager       keyManager       = new KeyManagerImpl(null, null, databaseKeyStore,null,null);


        keyManager.removeExpiredKeys();


        Mockito.verify(databaseKeyStore, Mockito.times(1)).deleteExpiredPublicKeys();
    }

    /*
        [throws exception] DatabaseKeyStore
    */
    @Test
    public void removeExpiredKeys_shouldThrowDatabaseKeyStoreException_whenRemoveExpiredKeysThrowsException() throws DatabaseKeyStoreException {
        final DatabaseKeyStore databaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        final KeyManager keyManager             = new KeyManagerImpl(null, null, databaseKeyStore,null,null);
        // mocks
        Mockito.doThrow(new DatabaseKeyStoreException("DB error."))
               .when(databaseKeyStore).deleteExpiredPublicKeys();


        assertThrows(DatabaseKeyStoreException.class, keyManager::removeExpiredKeys);


        Mockito.verify(databaseKeyStore, Mockito.times(1)).deleteExpiredPublicKeys();
    }
    // -- end of removeExpiredKeys


    // setSigningKeys
    /*
        [OK] local keystore
        [OK] database keystore
        [OK] signing keys
    */
    @Test
    public void setSigningKeys_shouldReplaceSigningKeysAndStoreThePublicPartInTheDatabase_whenEverythingIsOK() throws SigningKeysException, KeyManagerException, DatabaseKeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException {
        final KeyPairWithUUID keyPair       = new KeyPairWithUUID();
        final LocalKeyStore   localKeyStore = new LocalKeyStoreImpl();
        final KeyManager      keyManager    = new KeyManagerImpl(CONFIG_LOADER, localKeyStore, DATABASE_KEYSTORE,null,null);

        // verify signing keys are not set
        assertThrows(SigningKeysException.class, LOCAL_KEYSTORE::getSigningKeys);
        // verify key is not in the database
        assertThrows(NoSuchElementException.class, () -> DATABASE_KEYSTORE.getPublicKey(keyPair.uuid));

        // set the keys
        keyManager.setSigningKeys(keyPair.uuid, keyPair.keyPair.getPrivate(), keyPair.keyPair.getPublic());
        // get and verify from local keystore
        final SigningKeys signingKeys = keyManager.getSigningKeys();
        assertEquals(keyPair.keyPair.getPublic(),  signingKeys.publicKey);
        assertEquals(keyPair.keyPair.getPrivate(), signingKeys.privateKey);

        // verify public part from the database
        final PublicKeyData      publicKeyData = DATABASE_KEYSTORE.getPublicKey(keyPair.uuid);
        final byte[]             ba            = Base64.getDecoder().decode(publicKeyData.getBase64Key().getBytes());
        final X509EncodedKeySpec keySpec       = new X509EncodedKeySpec(ba);
        final PublicKey          publicKey     = KeyFactory.getInstance("RSA").generatePublic(keySpec);
        assertEquals(keyPair.keyPair.getPublic(), publicKey);
    }

    /*
        [OK]               local keystore
        [throws exception] database keystore
        [OK]               signing keys
    */
    @Test
    public void setSigningKeys_shouldChangeKeysAndCacheItForReInsert_whenDatabaseKeyStoreThrowsException() throws SigningKeysException, KeyManagerException, DatabaseKeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException {
        final KeyPairWithUUID   keyPair          = new KeyPairWithUUID();
        final String            base64PublicKey  = Base64.getEncoder().encodeToString(keyPair.keyPair.getPublic().getEncoded());
        final UUID              uuid             = keyPair.uuid;
        final Map<UUID, String> toAdd            = new HashMap<>();
        final LocalKeyStore     localKeyStore    = new LocalKeyStoreImpl();
        final DatabaseKeyStore  databaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        final KeyManager        keyManager       = new KeyManagerImpl(CONFIG_LOADER, localKeyStore, databaseKeyStore, toAdd,null);
        // mocks use when instead of doThrow when the method has a return value, otherwise go for doThrow
        Mockito.when(databaseKeyStore.addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.any(String.class)))
               .thenThrow(new DatabaseKeyStoreException("DB error."));


        // verify signing keys are not set
        assertThrows(SigningKeysException.class, LOCAL_KEYSTORE::getSigningKeys);
        keyManager.setSigningKeys(keyPair.uuid, keyPair.keyPair.getPrivate(), keyPair.keyPair.getPublic());

        // verify toAdd has been updated
        assertTrue(toAdd.containsKey(uuid));
        assertEquals(base64PublicKey, toAdd.get(uuid));

        // get and verify from local keystore
        final SigningKeys signingKeys = keyManager.getSigningKeys();
        assertEquals(keyPair.keyPair.getPublic(),  signingKeys.publicKey);
        assertEquals(keyPair.keyPair.getPrivate(), signingKeys.privateKey);
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
        final Base64.Encoder           encoder       = Base64.getEncoder();
        final String                   nodeName      = this.getClass().getSimpleName();
        final HashMap<UUID, PublicKey> expired_keys  = new HashMap<>();
        final HashMap<UUID, PublicKey> live_keys     = new HashMap<>();
        final LocalKeyStore            localKeyStore = new LocalKeyStoreImpl();

        // expired keys
        System.out.println("... expired keys ...");
        for ( int i = 0; i < 3; i++) {
            final KeyPairWithUUID k = new KeyPairWithUUID();
            expired_keys.put(k.uuid, k.keyPair.getPublic());
            localKeyStore.addPublicKey(k.uuid, k.keyPair.getPublic());
            DATABASE_KEYSTORE.addPublicKey(k.uuid, nodeName, -10000, encoder.encodeToString(k.keyPair.getPublic().getEncoded()));
        }
        // live keys
        System.out.println("... live keys ...");
        for ( int i = 0; i < 2; i++) {
            final KeyPairWithUUID k = new KeyPairWithUUID();
            live_keys.put(k.uuid, k.keyPair.getPublic());
            localKeyStore.addPublicKey(k.uuid, k.keyPair.getPublic());
            DATABASE_KEYSTORE.addPublicKey(k.uuid, nodeName, 30000, encoder.encodeToString(k.keyPair.getPublic().getEncoded()));
        }


        // key manager
        final KeyManager keyManager = new KeyManagerImpl(null, localKeyStore, DATABASE_KEYSTORE, null, null);
        keyManager.syncLiveKeys();


        // verify all the expired keys are gone from the local store
        System.out.println("... verify expired keys are removed ...");
        for ( final Map.Entry<UUID, PublicKey> e : expired_keys.entrySet()) {
            assertThrows(NoSuchElementException.class, () -> localKeyStore.getPublicKey(e.getKey()));
        }

        // verify all the live keys are present in the local store
        System.out.println("... verify live keys are present ...");
        for ( final Map.Entry<UUID, PublicKey> e : live_keys.entrySet()) {
            assertEquals(e.getValue(), localKeyStore.getPublicKey(e.getKey()));
        }
    }

    /*
        [OK]               local keystore
        [OK]               signing keys
        [throws exception] database keystore
    */
    @Test
    public void syncLiveKeys_shouldThrowDatabasKeyStoreException_whenDatabaseThrowsException() throws DatabaseKeyStoreException {
        final HashMap<UUID, PublicKey> expired_keys     = new HashMap<>();
        final HashMap<UUID, PublicKey> live_keys        = new HashMap<>();
        final LocalKeyStore            localKeyStore    = new LocalKeyStoreImpl();
        final DatabaseKeyStore         databaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        Mockito.doThrow(new DatabaseKeyStoreException("DB error.")).when(databaseKeyStore).getLivePublicKeyUUIDs();

        // expired keys
        System.out.println("... expired keys ...");
        for ( int i = 0; i < 3; i++) {
            final KeyPairWithUUID k = new KeyPairWithUUID();
            expired_keys.put(k.uuid, k.keyPair.getPublic());
            localKeyStore.addPublicKey(k.uuid, k.keyPair.getPublic());
        }
        // live keys
        System.out.println("... live keys ...");
        for ( int i = 0; i < 2; i++) {
            final KeyPairWithUUID k = new KeyPairWithUUID();
            live_keys.put(k.uuid, k.keyPair.getPublic());
            localKeyStore.addPublicKey(k.uuid, k.keyPair.getPublic());
        }


        // key manager
        final KeyManager keyManager = new KeyManagerImpl(null, localKeyStore, databaseKeyStore, null, null);
        assertThrows(DatabaseKeyStoreException.class, keyManager::syncLiveKeys);


        // verify all the expired keys are still present from the local store
        System.out.println("... verify expired keys are still present ...");
        for ( final Map.Entry<UUID, PublicKey> e : expired_keys.entrySet()) {
            assertEquals(e.getValue(), localKeyStore.getPublicKey(e.getKey()));
        }

        // verify all the live keys are present in the local store
        System.out.println("... verify live keys are also present ...");
        for ( final Map.Entry<UUID, PublicKey> e : live_keys.entrySet()) {
            assertEquals(e.getValue(), localKeyStore.getPublicKey(e.getKey()));
        }
    }
    // -- end of syncLiveKeys
}
