package cornerstone.webapp.services.rsa.store.local;

import cornerstone.webapp.services.rsa.store.LoggingMessageFormats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LocalKeyStore implements LocalKeyStoreInterface {
    private static final Logger logger = LoggerFactory.getLogger(LocalKeyStore.class);

    private Map<UUID, PublicKey> publicKeys;
    private PrivateKey privateKey = null;
    private UUID currentUUID = null;

    public LocalKeyStore() {
        publicKeys = new ConcurrentHashMap<>();
    }

    @Override
    public void addPublicKey(final UUID uuid, final PublicKey publicKey) {
        publicKeys.put(uuid, publicKey);
        logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "ADDED KEY", uuid));
    }

    @Override
    public void deletePublicKey(final UUID uuid) {
        publicKeys.remove(uuid);
        logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "DELETED KEY", uuid));
    }

    @Override
    public PublicKey getPublicKey(final UUID uuid) throws NoSuchElementException {
        final PublicKey keyData = publicKeys.get(uuid);
        if (null != keyData){
            logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "RETRIEVED KEY", keyData));
            return keyData;
        } else {
            logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "NO SUCH KEY", uuid));
            throw new NoSuchElementException();
        }
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
                logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "", "SYNC with DB KEEPING", uuid));
            } else {
                logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "", "SYNC with DB DELETED", uuid));
                publicKeys.remove(uuid);
            }
        });
    }

    @Override
    public void setPublicAndPrivateKeys(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey){
        currentUUID = uuid;
        this.privateKey = privateKey;
        publicKeys.put(uuid, publicKey);
        logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "", "NEW KEY PAIR SET", uuid));
    }

    @Override
    public Set<UUID> getPublicKeyUUIDs() {
        return publicKeys.keySet();
    }

    @Override
    public PrivateKeyWithUUID getPrivateKey() throws NoSuchElementException {
        if (null != privateKey) {
            logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "", "KEY PAIR RETURNED", currentUUID));
            return new PrivateKeyWithUUID(currentUUID, privateKey);
        } else {
            logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1, "", "KEY PAIR IS NOT SET"));
            throw new NoSuchElementException();
        }
    }

    @Override
    public void dropPrivateKey() {
        privateKey  = null;
        currentUUID = null;
        logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1, "", "PRIVATE KEY DELETED"));
    }

    @Override
    public void dropEverything() {
        publicKeys = new HashMap<>();
        currentUUID = null;
        privateKey = null;
        logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1, "", "DROPPED ALL"));
    }
}
