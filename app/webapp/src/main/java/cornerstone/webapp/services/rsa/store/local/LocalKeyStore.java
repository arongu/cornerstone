package cornerstone.webapp.services.rsa.store.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LocalKeyStore implements LocalKeyStoreInterface {
    private static final String MESSAGE_PUBLIC_KEY_ADDED           = "... public key ADDED    (UUID: '%s')";
    private static final String MESSAGE_PUBLIC_KEY_DELETED         = "... public key DELETED  (UUID: '%s')";
    private static final String MESSAGE_PUBLIC_KEY_RETURNED        = "... public key RETURNED (UUID: '%s')";
    private static final String MESSAGE_PUBLIC_KEY_DOES_NOT_EXIST  = "... public key DOES NOT EXIST (UUID: '%s')";

    private static final String MESSAGE_CLEANUP_KEEP_PUBLIC_KEY    = "... cleanup - public key KEPT    (UUID: '%s')";
    private static final String MESSAGE_CLEANUP_REMOVE_PUBLIC_KEY  = "... cleanup - public key DELETED (UUID: '%s')";

    private static final String MESSAGE_SIGNING_KEYS_SET           = "... signing keys SET (UUID: '%s')";
    private static final String MESSAGE_SIGNING_KEYS_DELETED       = "... signing keys DELETED (UUID: '%s')";
    private static final String MESSAGE_SIGNING_KEYS_RETURNED      = "... signing keys RETURNED (UUID: '%s')";
    private static final String MESSAGE_SIGNING_KEYS_ARE_NOT_SET   = "... signing keys ARE NOT SET";
    private static final String MESSAGE_RESET                      = "... RESET";

    private static final Logger logger = LoggerFactory.getLogger(LocalKeyStore.class);

    private Map<UUID, PublicKey> publicKeys;
    private UUID signingKeyUUID = null;
    private PrivateKey privateKey = null;

    public LocalKeyStore() {
        publicKeys = new ConcurrentHashMap<>();
    }

    @Override
    public PublicKey getPublicKey(final UUID uuid) throws NoSuchElementException {
        final PublicKey key = publicKeys.get(uuid);
        if (null != key){
            logger.info(String.format(MESSAGE_PUBLIC_KEY_RETURNED, uuid.toString()));
            return key;
        } else {
            logger.info(String.format(MESSAGE_PUBLIC_KEY_DOES_NOT_EXIST, uuid.toString()));
            throw new NoSuchElementException();
        }
    }

    @Override
    public void addPublicKey(final UUID uuid, final PublicKey publicKey) {
        publicKeys.put(uuid, publicKey);
        logger.info(String.format(MESSAGE_PUBLIC_KEY_ADDED, uuid.toString()));
    }

    @Override
    public void removePublicKey(final UUID uuid) {
        publicKeys.remove(uuid);
        logger.info(String.format(MESSAGE_PUBLIC_KEY_DELETED, uuid.toString()));
    }

    @Override
    public void removePublicKeys(final List<UUID> uuidsToBeRemoved) {
        publicKeys.keySet().forEach(uuid -> {
            if (uuidsToBeRemoved.contains(uuid)){
                publicKeys.remove(uuid);
                logger.info(String.format(MESSAGE_CLEANUP_REMOVE_PUBLIC_KEY, uuid));
            } else {
                logger.info(String.format(MESSAGE_CLEANUP_KEEP_PUBLIC_KEY, uuid));
            }
        });
    }

    @Override
    public void keepOnly(final List<UUID> toBeKept){
        publicKeys.keySet().forEach(uuid -> {
            if (uuid == signingKeyUUID || toBeKept.contains(uuid)){
                logger.info(String.format(MESSAGE_CLEANUP_KEEP_PUBLIC_KEY, uuid));
            } else {
                publicKeys.remove(uuid);
                logger.info(String.format(MESSAGE_CLEANUP_REMOVE_PUBLIC_KEY, uuid));
            }
        });
    }

    @Override
    public void setKeysForSigning(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey){
        signingKeyUUID = uuid;
        this.privateKey = privateKey;
        publicKeys.put(uuid, publicKey);
        logger.info(String.format(MESSAGE_SIGNING_KEYS_SET, uuid.toString()));
    }

    @Override
    public Set<UUID> getUUIDs() {
        return publicKeys.keySet();
    }

    @Override
    public PrivateKeyWithUUID getPrivateKey() throws NoSuchElementException {
        if (null != privateKey) {
            logger.info(String.format(MESSAGE_SIGNING_KEYS_RETURNED, signingKeyUUID.toString()));
            return new PrivateKeyWithUUID(signingKeyUUID, privateKey);
        } else {
            logger.info(MESSAGE_SIGNING_KEYS_ARE_NOT_SET);
            throw new NoSuchElementException();
        }
    }

    @Override
    public void dropPrivateKey() {
        privateKey  = null;
        signingKeyUUID = null;
        logger.info(MESSAGE_SIGNING_KEYS_DELETED);
    }

    @Override
    public void dropEverything() {
        publicKeys = new HashMap<>();

        signingKeyUUID = null;
        privateKey = null;
        logger.info(MESSAGE_RESET);
    }
}
