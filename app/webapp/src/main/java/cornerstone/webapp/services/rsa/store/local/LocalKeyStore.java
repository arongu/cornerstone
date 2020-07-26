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

    private static final String MESSAGE_SYNC_KEEP_PUBLIC_KEY       = "... sync - public key KEPT    (UUID: '%s')";
    private static final String MESSAGE_SYNC_REMOVE_PUBLIC_KEY     = "... sync - public key DELETED (UUID: '%s')";

    private static final String MESSAGE_KEY_PAIR_SET      = "... public,private keys SET (UUID: '%s')";
    private static final String MESSAGE_KEY_PAIR_DELETED  = "... public,private keys DELETED (UUID: '%s')";
    private static final String MESSAGE_KEY_PAIR_RETURNED = "... public,private keys RETURNED (UUID: '%s')";
    private static final String MESSAGE_KEY_PAIR_NOT_SET  = "... public,private keys ARE NOT SET";
    private static final String MESSAGE_DROP_ALL          = "... DROP ALL";

    private static final Logger logger = LoggerFactory.getLogger(LocalKeyStore.class);

    private Map<UUID, PublicKey> publicKeys;
    private PrivateKey privateKey = null;
    private UUID currentUUID = null;

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
    public void deletePublicKey(final UUID uuid) {
        publicKeys.remove(uuid);
        logger.info(String.format(MESSAGE_PUBLIC_KEY_DELETED, uuid.toString()));
    }

    @Override
    public void deletePublicKeys(final List<UUID> uuidsToBeRemoved) {
        publicKeys.keySet().forEach(uuid -> {
            if (uuidsToBeRemoved.contains(uuid)){
                deletePublicKey(uuid);
            }
        });
    }

    @Override
    public void sync(final List<UUID> toBeKept){
        publicKeys.keySet().forEach(uuid -> {
            if (uuid == currentUUID || toBeKept.contains(uuid)){
                logger.info(String.format(MESSAGE_SYNC_KEEP_PUBLIC_KEY, uuid));
            } else {
                publicKeys.remove(uuid);
                logger.info(String.format(MESSAGE_SYNC_REMOVE_PUBLIC_KEY, uuid));
            }
        });
    }

    @Override
    public void setPublicAndPrivateKeys(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey){
        currentUUID = uuid;
        this.privateKey = privateKey;
        publicKeys.put(uuid, publicKey);
        logger.info(String.format(MESSAGE_KEY_PAIR_SET, uuid.toString()));
    }

    @Override
    public Set<UUID> getPublicKeyUUIDs() {
        return publicKeys.keySet();
    }

    @Override
    public PrivateKeyWithUUID getPrivateKey() throws NoSuchElementException {
        if (null != privateKey) {
            logger.info(String.format(MESSAGE_KEY_PAIR_RETURNED, currentUUID.toString()));
            return new PrivateKeyWithUUID(currentUUID, privateKey);
        } else {
            logger.info(MESSAGE_KEY_PAIR_NOT_SET);
            throw new NoSuchElementException();
        }
    }

    @Override
    public void dropPrivateKey() {
        privateKey  = null;
        currentUUID = null;
        logger.info(MESSAGE_KEY_PAIR_DELETED);
    }

    @Override
    public void dropEverything() {
        publicKeys = new HashMap<>();
        currentUUID = null;
        privateKey = null;
        logger.info(MESSAGE_DROP_ALL);
    }
}
