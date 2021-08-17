package cornerstone.webapp.services.keys.stores.local;

import cornerstone.webapp.services.keys.stores.logging.MessageElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores the private, public key of the node, and also caches public keys from other nodes.
 */
public class LocalKeyStoreImpl implements LocalKeyStore {
    private static final Logger logger = LoggerFactory.getLogger(LocalKeyStoreImpl.class);

    private Map<UUID, PublicKey> publicKeys;
    private PrivateKey livePrivateKey = null;
    private PublicKey  livePublicKey  = null;
    private UUID       live_uuid      = null;

    public LocalKeyStoreImpl() {
        publicKeys = new ConcurrentHashMap<>();
    }

    /**
     * Adds a public key to the store.
     * @param uuid UUID of the key.
     * @param publicKey The public key of the key.
     */
    @Override
    public void addPublicKey(final UUID uuid, final PublicKey publicKey) {
        publicKeys.put(uuid, publicKey);
        final String logMsg = MessageElements.PREFIX_LOCAL + " " + MessageElements.ADDED + " " + MessageElements.PUBLIC_KEY + " " + uuid;
        logger.info(logMsg);
    }

    /**
     * Adds a public key to the store.
     * @param uuid UUID of the key.
     * @param base64KeyString Public key in base64 format (useful when data is coming from DB).
     * @throws NoSuchAlgorithmException Thrown when KeyFactory cannot provide "RSA" instance. (Never should occur, unless underlying Java changes.)
     * @throws InvalidKeySpecException Thrown when keySpec is invalid. (Never should occur, unless underlying Java changes.)
     */
    @Override
    public void addPublicKey(final UUID uuid, final String base64KeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final byte[] ba                  = Base64.getDecoder().decode(base64KeyString.getBytes());
        final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(ba);
        final PublicKey publicKey        = KeyFactory.getInstance("RSA").generatePublic(keySpec);
        addPublicKey(uuid, publicKey);
    }

    /**
     * Deletes a public key from the local store based on UUID.
     * @param uuid UUID of the key.
     */
    @Override
    public void deletePublicKey(final UUID uuid) {
        publicKeys.remove(uuid);
        final String logMsg = MessageElements.PREFIX_LOCAL + " " + MessageElements.DELETED + " " + MessageElements.PUBLIC_KEY + " " + uuid;
        logger.info(logMsg);
    }

    /**
     * Deletes a list of public keys based on the passed UUIDs.
     * @param toRemove Takes a list of UUIDs and deletes any matching keys from the store.
     */
    @Override
    public void deletePublicKey(final List<UUID> toRemove) {
        publicKeys.keySet().forEach(uuid -> {
            if ( toRemove.contains(uuid)){
                deletePublicKey(uuid);
            }
        });
    }

    /**
     * Returns a public key based on UUID.
     * @param uuid UUID of the key.
     * @return Returns the PublicKey object based on the UUID.
     * @throws NoSuchElementException Thrown when the key could not be found based on the UUID.
     */
    @Override
    public PublicKey getPublicKey(final UUID uuid) throws NoSuchElementException {
        final PublicKey keyData = publicKeys.get(uuid);
        if ( null != keyData) {
            return keyData;
        } else {
            final String logMsg = MessageElements.PREFIX_LOCAL + " " + MessageElements.NO_SUCH + " " + MessageElements.PUBLIC_KEY + " " + uuid;
            logger.info(logMsg);
            throw new NoSuchElementException();
        }
    }

    /**
     * Keeps only the keys with listed UUIDs, deletes the rest of it.
     * Removes any key from the store which is not present in the passed UUIDs.
     * @param toBeKept List of the key UUIDs that needs to be kept, the rest will be deleted.
     */
    @Override
    public void keepOnlyPublicKeys(final List<UUID> toBeKept) {
        int deleted = 0;
        for ( final UUID uuid : publicKeys.keySet()) {
            if ( uuid.equals(live_uuid) || toBeKept.contains(uuid)) {
                final String logMsg = MessageElements.PREFIX_LOCAL + " " + MessageElements.SYNC + " " + MessageElements.PUBLIC_KEY + " " + MessageElements.KEPT + " " + uuid;
                logger.info(logMsg);

            } else {
                publicKeys.remove(uuid);
                deleted++;

                final String logMsg = MessageElements.PREFIX_LOCAL + " " + MessageElements.SYNC + " " + MessageElements.PUBLIC_KEY + " " + MessageElements.DELETED + " " + uuid;
                logger.info(logMsg);
            }
        }

        final String logMsg = MessageElements.PREFIX_LOCAL + " " + MessageElements.SYNC + " " + MessageElements.PUBLIC_KEY + " " + MessageElements.KEPT + ", " + MessageElements.DELETED + " " + toBeKept.size() + " " + deleted;
        logger.info(logMsg);
    }

    /**
     * Sets the keys used for JWT/JWS signing.
     * @param uuid The UUID of the key.
     * @param privateKey Private key.
     * @param publicKey Public key.
     */
    @Override
    public void setSigningKeys(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey){
        this.live_uuid = uuid;
        this.livePrivateKey = privateKey;
        this.livePublicKey  = publicKey;
        publicKeys.put(uuid, publicKey);

        final String logMsg = MessageElements.PREFIX_LOCAL + " " + MessageElements.SET + " " + MessageElements.PUBLIC_AND_PRIVATE_KEY + " " + uuid;
        logger.info(logMsg);
    }

    /**
     * @return Returns UUIDs of the cached public keys.
     */
    @Override
    public Set<UUID> getPublicKeyUUIDs() {
        return publicKeys.keySet();
    }

    /**
     * Returns the keys and the corresponding UUID used for signing JWT/JWS.
     * @return Returns public key, private key, UUID.
     * @throws SigningKeysException Thrown the keys are not set properly.
     */
    @Override
    public SigningKeys getSigningKeys() throws SigningKeysException {
        if ( null == livePrivateKey || null == livePublicKey || null == live_uuid) {
            final String errorLog = MessageElements.PREFIX_LOCAL + " " + MessageElements.NOT_SET + " " + MessageElements.PUBLIC_AND_PRIVATE_KEY;
            logger.error(errorLog);
            throw new SigningKeysException("Signing keys are not initialized properly!");

        } else {
            return new SigningKeys(live_uuid, livePrivateKey, livePublicKey);
        }
    }

    /**
     * Throws away everything in the local store.
     */
    @Override
    public void resetAll() {
        publicKeys = new HashMap<>();
        live_uuid = null;
        livePrivateKey = null;

        final String logMsg = MessageElements.PREFIX_LOCAL + " " + "DROPPED ALL" + " " + MessageElements.PUBLIC_AND_PRIVATE_KEY;
        logger.info(logMsg);
    }
}
