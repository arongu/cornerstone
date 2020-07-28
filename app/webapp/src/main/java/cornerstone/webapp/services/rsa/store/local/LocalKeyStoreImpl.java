package cornerstone.webapp.services.rsa.store.local;

import cornerstone.webapp.services.rsa.store.LoggingMessageFormats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LocalKeyStoreImpl implements LocalKeyStore {
    private static final Logger logger = LoggerFactory.getLogger(LocalKeyStoreImpl.class);

    private Map<UUID, PublicKey> publicKeys;
    private PrivateKey privateKey = null;
    private UUID currentUUID = null;

    public LocalKeyStoreImpl() {
        publicKeys = new ConcurrentHashMap<>();
    }

    @Override
    public void addPublicKey(final UUID uuid, final PublicKey publicKey) {
        publicKeys.put(uuid, publicKey);
        logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "ADDED KEY (LOCAL)", uuid));
    }

    @Override
    public void deletePublicKey(final UUID uuid) {
        publicKeys.remove(uuid);
        logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "DELETED KEY (LOCAL)", uuid));
    }

    @Override
    public PublicKey getPublicKey(final UUID uuid) throws NoSuchElementException {
        final PublicKey keyData = publicKeys.get(uuid);
        if (null != keyData){
            return keyData;
        } else {
            logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "NO SUCH KEY (LOCAL)", uuid));
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
        int deleted = 0;
        for (final UUID uuid : publicKeys.keySet()) {
            if (uuid == currentUUID || toBeKept.contains(uuid)){
                logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "", "SYNCING - KEEPING KEY WITH UUID (LOCAL)", uuid));
            } else {
                publicKeys.remove(uuid);
                deleted++;
                logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "", "SYNCING - DELETED KEY WITH UUID (LOCAL)", uuid));
            }
        }

        logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA1_DATA2, "", "KEEPING, DELETED (LOCAL)", publicKeys.size(), deleted));
    }

    @Override
    public void setPublicAndPrivateKeys(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey){
        currentUUID = uuid;
        this.privateKey = privateKey;
        publicKeys.put(uuid, publicKey);
        logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "", "NEW PUBLIC AND PRIVATE KEY SET (LOCAL)", uuid));
    }

    @Override
    public Set<UUID> getPublicKeyUUIDs() {
        return publicKeys.keySet();
    }

    @Override
    public PrivateKeyWithUUID getPrivateKey() throws NoSuchElementException {
        if (null != privateKey) {
            return new PrivateKeyWithUUID(currentUUID, privateKey);
        } else {
            logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1, "", "PUBLIC AND PRIVATE KEY ARE NOT SET (LOCAL)"));
            throw new NoSuchElementException();
        }
    }

    @Override
    public void dropPrivateKey() {
        privateKey  = null;
        currentUUID = null;
        logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1, "", "PRIVATE KEY DROPPED (LOCAL)"));
    }

    @Override
    public void dropEverything() {
        publicKeys = new HashMap<>();
        currentUUID = null;
        privateKey = null;
        logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1, "", "DROPPED ALL (LOCAL)"));
    }
}
