package cornerstone.webapp.services.keys.stores.manager;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.services.keys.common.PublicKeyData;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStore;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStoreException;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.SigningKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class KeyManagerImpl implements KeyManager {
    private static final Logger logger = LoggerFactory.getLogger(KeyManagerImpl.class);

    @Inject
    private LocalKeyStore localKeyStore;
    @Inject
    private DatabaseKeyStore databaseKeyStore;
    @Inject
    private ConfigLoader configLoader;

    private Set<KeyData> toAdd = new HashSet<>();
    private Set<UUID> toDelete = new HashSet<>();

    public class KeyData {
        String b64key;
        UUID uuid;

        KeyData(final UUID uuid, final String b64key) {
            this.uuid = uuid;
            this.b64key = b64key;
        }
    }

    public KeyManagerImpl() {
    }

    public KeyManagerImpl(final ConfigLoader configLoader,
                          final LocalKeyStore localKeyStore,
                          final DatabaseKeyStore databaseKeyStore,
                          final Set<KeyData> toAdd,
                          final Set<UUID> toDelete) {

        this.configLoader = configLoader;
        this.localKeyStore = localKeyStore;
        this.databaseKeyStore = databaseKeyStore;
        this.toAdd = toAdd;
        this.toDelete = toDelete;
    }

    @Override
    public void addPublicKey(final UUID uuid, final String base64_key) throws KeyManagerException {
        if ( uuid == null)       { throw new KeyManagerException("UUID must not be null!"); }
        if ( base64_key == null) { throw new KeyManagerException("Base64 key must not be null!"); }

        // try to add key to local cache
        try {
            localKeyStore.addPublicKey(uuid, base64_key);
        } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error(e.getMessage());
            throw new KeyManagerException(e.getMessage());
        }

        // try to add to database, if it fails cache it
        final String nodeName = configLoader.getAppProperties().getProperty(APP_ENUM.APP_NODE_NAME.key);
        final int rsaTTL      = Integer.parseInt(configLoader.getAppProperties().getProperty(APP_ENUM.APP_RSA_TTL.key));
        final int jwtTTL      = Integer.parseInt(configLoader.getAppProperties().getProperty(APP_ENUM.APP_JWT_TTL.key));

        try {
            databaseKeyStore.addPublicKey(uuid, nodeName, rsaTTL + jwtTTL, base64_key);
        } catch (final DatabaseKeyStoreException dbe) {
            // cache it to add it later
            toAdd.add(new KeyData(uuid,base64_key));
            logger.error("Failed to add {} key to database: {}", uuid, dbe.getMessage());
            logger.info("{} key is cached, for retrial.", uuid);
        }
    }

    @Override
    public void addPublicKey(final UUID uuid, final PublicKey publicKey) throws KeyManagerException {
        if ( uuid == null)      { throw new KeyManagerException("UUID must not be null!"); }
        if ( publicKey == null) { throw new KeyManagerException("Public key must not be null!"); }

        final String base64_key;
        try {
             base64_key = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        } catch (final Exception e){
            final String message = "Error during public key conversion: " + e.getMessage();
            logger.error(message);
            throw new KeyManagerException(message);
        }

        addPublicKey(uuid, base64_key);
    }

    @Override
    public void deletePublicKey(UUID uuid) {
        localKeyStore.deletePublicKey(uuid);
        //databaseKeyStore.deletePublicKey(uuid);
    }

    @Override
    public PublicKey getPublicKey(UUID uuid) throws KeyManagerException {
        return null;
    }

    @Override
    public SigningKeys getSigningKeys() throws KeyManagerException {
        return null;
    }

    @Override
    public void setSigningKeys() throws KeyManagerException {

    }

    @Override
    public void sync(UUID uuid, PrivateKey privateKey, PublicKey publicKey) throws KeyManagerException {

    }
}
