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

    private Set<PublicKeyData> toAdd = new HashSet<>();
    private Set<UUID> toDelete = new HashSet<>();

    public KeyManagerImpl() {
    }

    public KeyManagerImpl(final ConfigLoader configLoader, final LocalKeyStore localKeyStore, final DatabaseKeyStore databaseKeyStore) {
        this.configLoader = configLoader;
        this.localKeyStore = localKeyStore;
        this.databaseKeyStore = databaseKeyStore;
    }

    public KeyManagerImpl(final LocalKeyStore localKeyStore, final DatabaseKeyStore databaseKeyStore, final ConfigLoader configLoader) {
        this.localKeyStore = localKeyStore;
        this.databaseKeyStore = databaseKeyStore;
        this.configLoader = configLoader;
    }

    @Override
    public void addPublicKey(final UUID uuid, final String base64_key) throws KeyManagerException {
        // try to add key to local cache
        try {
            localKeyStore.addPublicKey(uuid, base64_key);
        } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new KeyManagerException(e.getMessage());
        }

        // if local cache did not find any error with the key proceed to database
        final String nodeName = configLoader.getAppProperties().getProperty(APP_ENUM.APP_NODE_NAME.key);
        final int rsaTTL      = Integer.parseInt(configLoader.getAppProperties().getProperty(APP_ENUM.APP_RSA_TTL.key));
        final int jwtTTL      = Integer.parseInt(configLoader.getAppProperties().getProperty(APP_ENUM.APP_RSA_TTL.key));

        try {
            databaseKeyStore.addPublicKey(uuid, nodeName, rsaTTL + jwtTTL, base64_key);
        } catch (final DatabaseKeyStoreException dbe) {
            final PublicKeyData publicKeyData = new PublicKeyData(uuid, nodeName, rsaTTL + jwtTTL, null, null, base64_key);
            toAdd.add(publicKeyData);
            logger.error(dbe.getMessage());
        }
    }

    @Override
    public void addPublicKey(final UUID uuid, final PublicKey publicKey) throws KeyManagerException {
        final String nodeName   = configLoader.getAppProperties().getProperty(APP_ENUM.APP_NODE_NAME.key);
        final int rsaTTL        = Integer.parseInt(configLoader.getAppProperties().getProperty(APP_ENUM.APP_RSA_TTL.key));
        final int jwtTTL        = Integer.parseInt(configLoader.getAppProperties().getProperty(APP_ENUM.APP_RSA_TTL.key));
        final String base64_key = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        localKeyStore.addPublicKey(uuid, publicKey);

        try {
            databaseKeyStore.addPublicKey(uuid, nodeName, rsaTTL + jwtTTL, base64_key);
        } catch (final DatabaseKeyStoreException dbe) {
            final PublicKeyData publicKeyData = new PublicKeyData(uuid, nodeName, rsaTTL + jwtTTL, null, null, base64_key);
            toAdd.add(publicKeyData);
            logger.error(dbe.getMessage());
        }
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
