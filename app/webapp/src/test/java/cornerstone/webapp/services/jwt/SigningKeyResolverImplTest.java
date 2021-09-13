package cornerstone.webapp.services.jwt;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.services.keys.rotation.KeyPairWithUUID;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStore;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStoreException;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStoreImpl;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStoreImpl;
import cornerstone.webapp.services.keys.stores.manager.KeyManager;
import cornerstone.webapp.services.keys.stores.manager.KeyManagerImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.Key;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SigningKeyResolverImplTest {
    private static ConfigLoader     CONFIG_LOADER;
    private static LocalKeyStore    LOCAL_KEYSTORE;
    private static DatabaseKeyStore DATABASE_KEYSTORE;

    @BeforeAll
    public static void init() {
        final String test_config_dir = "../../_test_config/";
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

    // resolveSigningKey
    /*
        [OK]           local keystore
        [do not touch] database keystore
    */
    @Test
    public void resolveSigningKey_shouldReturnKeyFromLocalKeyStore_whenUUIDMatches() {
        final LocalKeyStore      localKeyStore      = new LocalKeyStoreImpl();
        final KeyPairWithUUID    keypair            = new KeyPairWithUUID();
        final KeyManager         keyManager         = new KeyManagerImpl(null, localKeyStore, null);
        final SigningKeyResolver signingKeyResolver = new SigningKeyResolverImpl(keyManager);
        localKeyStore.addPublicKey(keypair.uuid, keypair.keyPair.getPublic());
        final JwsHeader<?>       jwsHeader          = Jwts.jwsHeader().setKeyId(keypair.uuid.toString());


        final Key key = signingKeyResolver.resolveSigningKey(jwsHeader, (Claims) null);


        assertEquals(key, keypair.keyPair.getPublic());
    }

    /*
        [OK] local keystore
        [OK] database keystore
    */
    @Test
    public void resolveSigningKey_shouldTryToGetTheKeyFromDB_whenLocalKeyStoreDoesNotHaveTheKey() throws DatabaseKeyStoreException {
        final LocalKeyStore      localKeyStore      = new LocalKeyStoreImpl();
        final KeyPairWithUUID    keypair            = new KeyPairWithUUID();
        final UUID               uuid               = keypair.uuid;
        final String             base64PublicKey    = Base64.getEncoder().encodeToString(keypair.keyPair.getPublic().getEncoded());
        final KeyManager         keyManager         = new KeyManagerImpl(null, localKeyStore, DATABASE_KEYSTORE);
        final SigningKeyResolver signingKeyResolver = new SigningKeyResolverImpl(keyManager);
        final JwsHeader<?>       jwsHeader          = Jwts.jwsHeader().setKeyId(keypair.uuid.toString());
        // mocks
        DATABASE_KEYSTORE.addPublicKey(keypair.uuid, this.getClass().getSimpleName(), 10000, base64PublicKey);
        // make sure local keystore does not have the key
        assertThrows(NoSuchElementException.class, () -> localKeyStore.getPublicKey(uuid));


        final Key key = signingKeyResolver.resolveSigningKey(jwsHeader, (Claims) null);


        assertEquals(key                        , keypair.keyPair.getPublic());
        assertEquals(keypair.keyPair.getPublic(), localKeyStore.getPublicKey(uuid));
    }

    /*
        [does not have it] local keystore
        [throws exception] database keystore
    */
    @Test
    public void resolveSigningKey_shouldThrowNoSuchElementException_whenKeyManagerThrowsException() throws DatabaseKeyStoreException {
        final LocalKeyStore      localKeyStore      = new LocalKeyStoreImpl();
        final DatabaseKeyStore   databaseKeyStore   = Mockito.mock(DatabaseKeyStore.class);
        final KeyManager         keyManager         = new KeyManagerImpl(null, localKeyStore, databaseKeyStore);
        final SigningKeyResolver signingKeyResolver = new SigningKeyResolverImpl(keyManager);
        final JwsHeader<?>       jwsHeader          = Jwts.jwsHeader().setKeyId(UUID.randomUUID().toString());
        // mocks
        Mockito.when(databaseKeyStore.getPublicKey(Mockito.any(UUID.class)))
               .thenThrow(new DatabaseKeyStoreException("DB error."));


        assertThrows(NoSuchElementException.class, () -> signingKeyResolver.resolveSigningKey(jwsHeader, (Claims) null));
    }

    @Test
    public void resolveSigningKey_shouldThrowNoSuchElementException_whenUUIDdoesNotExist() {
        final KeyManager         keyManager         = new KeyManagerImpl(null, LOCAL_KEYSTORE, DATABASE_KEYSTORE);
        final SigningKeyResolver signingKeyResolver = new SigningKeyResolverImpl(keyManager);
        final JwsHeader<?>       jwsHeader          = Jwts.jwsHeader().setKeyId(UUID.randomUUID().toString());


        assertThrows(NoSuchElementException.class, () -> signingKeyResolver.resolveSigningKey(jwsHeader, (Claims) null));
    }
    // -- end of resolveSigningKey
}
