package cornerstone.webapp.services.keys.stores.manager;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStore;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStoreException;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.SigningKeys;
import cornerstone.webapp.services.keys.stores.logging.MessageElements;
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

    private Map<UUID, String> toAdd = new HashMap<>();
    private Set<UUID> toDelete = new HashSet<>();

    public KeyManagerImpl() {
    }

    public KeyManagerImpl(final ConfigLoader configLoader,
                          final LocalKeyStore localKeyStore,
                          final DatabaseKeyStore databaseKeyStore,
                          final Map<UUID,String> toAdd,
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
            final String msg = MessageElements.PREFIX_MANAGER + " " + MessageElements.PUBLIC_KEY_IS_INVALID + " " + uuid.toString();
            logger.error(msg);
            throw new KeyManagerException(msg);
        }

        // try to add to database, if it fails cache it
        final String nodeName = configLoader.getAppProperties().getProperty(APP_ENUM.APP_NODE_NAME.key);
        final int rsaTTL      = Integer.parseInt(configLoader.getAppProperties().getProperty(APP_ENUM.APP_RSA_TTL.key));
        final int jwtTTL      = Integer.parseInt(configLoader.getAppProperties().getProperty(APP_ENUM.APP_JWT_TTL.key));

        try {
            databaseKeyStore.addPublicKey(uuid, nodeName, rsaTTL + jwtTTL, base64_key);
            logger.info(MessageElements.PREFIX_MANAGER + " " + MessageElements.ADDED + " " + MessageElements.PUBLIC_KEY + " " + uuid.toString());

        } catch (final DatabaseKeyStoreException dbe) {
            logger.error(MessageElements.PREFIX_MANAGER + " " + MessageElements.DATABASE_KEYSTORE_ERROR + ": " + dbe.getMessage());

            if ( ! toAdd.containsKey(uuid)){
                toAdd.put(uuid, base64_key);
                logger.info(MessageElements.PREFIX_MANAGER + " " + MessageElements.PUBLIC_KEY_ADDED_FOR_REINSERT + " " + uuid);
            }
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
            final String msg = MessageElements.PREFIX_MANAGER + " " + MessageElements.PUBLIC_KEY_CONVERSION_ERROR;
            logger.error(msg);
            throw new KeyManagerException(msg);
        }

        addPublicKey(uuid, base64_key);
    }

    @Override
    public void deletePublicKey(final UUID uuid) {
        localKeyStore.deletePublicKey(uuid);
        toAdd.remove(uuid); // in case the key was added earlier, but failed to be published, then give upon it
        try {
            databaseKeyStore.deletePublicKey(uuid);
            toDelete.remove(uuid); // in case it was in a delete list, get rid of it
        } catch (final DatabaseKeyStoreException dbe){
            logger.error(MessageElements.PREFIX_MANAGER + " " + MessageElements.DATABASE_KEYSTORE_ERROR + ": " + dbe.getMessage());
            toDelete.add(uuid); // if it cannot be removed from the db at this time, add it to the removal list
            logger.info(MessageElements.PREFIX_MANAGER + " " + MessageElements.PUBLIC_KEY_ADDED_TO_RE_DELETE + " " + uuid);
        }
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
