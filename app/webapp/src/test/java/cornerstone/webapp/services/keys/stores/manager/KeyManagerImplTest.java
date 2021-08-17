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
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void addPublicKey_shouldThrowKeyManagerException_whenKeyIsNull() throws InvalidKeySpecException, NoSuchAlgorithmException {
        final LocalKeyStore mockLocalKeyStore       = Mockito.mock(LocalKeyStore.class);
        final DatabaseKeyStore mockDatabaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        Mockito.doThrow(new InvalidKeySpecException()).when(mockLocalKeyStore).addPublicKey(Mockito.any(), (String) Mockito.any());
        final KeyManager keyManager = new KeyManagerImpl(configLoader, mockLocalKeyStore, mockDatabaseKeyStore, null, null);

        assertThrows(KeyManagerException.class, () -> keyManager.addPublicKey(UUID.randomUUID(), (PublicKey) null));
    }

    @Test
    public void addPublicKey_shouldThrowKeyManagerException_whenKeyIsInvalid() throws InvalidKeySpecException, NoSuchAlgorithmException {
        final LocalKeyStore mockLocalKeyStore       = Mockito.mock(LocalKeyStore.class);
        final DatabaseKeyStore mockDatabaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        Mockito.doThrow(new InvalidKeySpecException()).when(mockLocalKeyStore).addPublicKey(Mockito.any(), (String) Mockito.any());
        final KeyManager keyManager = new KeyManagerImpl(configLoader, mockLocalKeyStore, mockDatabaseKeyStore, null, null);

        assertThrows(KeyManagerException.class, () -> keyManager.addPublicKey(UUID.randomUUID(), "thisIsNotaKey"));
    }

    // todo refine it
    @Test
    public void addPublicKey_shouldCacheKeyForAddition_whenDatabaseThrowsException() throws KeyManagerException, DatabaseKeyStoreException {
        final Set<KeyManagerImpl.KeyData> toAdd     = new HashSet<>();
        final LocalKeyStore mockLocalKeyStore       = Mockito.mock(LocalKeyStore.class);
        final DatabaseKeyStore mockDatabaseKeyStore = Mockito.mock(DatabaseKeyStore.class);
        final UUID uuid                             = UUID.randomUUID();
        Mockito.doThrow(new DatabaseKeyStoreException("Nope."))
                .when(mockDatabaseKeyStore)
                .addPublicKey(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.any(Integer.class), Mockito.any(String.class));
        final KeyManager keyManager = new KeyManagerImpl(configLoader, mockLocalKeyStore, mockDatabaseKeyStore, toAdd, null);

        keyManager.addPublicKey(uuid, "mockedlocalkeystoresodoesntmatter");
    }
}
