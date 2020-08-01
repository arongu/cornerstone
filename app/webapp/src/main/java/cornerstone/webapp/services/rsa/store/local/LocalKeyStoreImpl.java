package cornerstone.webapp.services.rsa.store.local;

import cornerstone.webapp.common.AlignedLogMessages;
import cornerstone.webapp.services.rsa.store.log.MessageElements;
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

        logger.info(String.format(
                AlignedLogMessages.FORMAT__30C_30C_DATA,
                AlignedLogMessages.OFFSETS_KEYSTORE_CLASSES.get(getClass().getName()),
                MessageElements.PREFIX_LOCAL + MessageElements.ADDED,
                MessageElements.PUBLIC_KEY,
                uuid)
        );
    }

    @Override
    public void deletePublicKey(final UUID uuid) {
        publicKeys.remove(uuid);
        logger.info(String.format(
                AlignedLogMessages.FORMAT__30C_30C_DATA,
                AlignedLogMessages.OFFSETS_KEYSTORE_CLASSES.get(getClass().getName()),
                MessageElements.PREFIX_LOCAL + MessageElements.DELETED,
                MessageElements.PUBLIC_KEY,
                uuid)
        );
    }

    @Override
    public PublicKey getPublicKey(final UUID uuid) throws NoSuchElementException {
        final PublicKey keyData = publicKeys.get(uuid);
        if (null != keyData){
            return keyData;
        } else {
            logger.info(String.format(
                    AlignedLogMessages.FORMAT__30C_30C_DATA,
                    AlignedLogMessages.OFFSETS_KEYSTORE_CLASSES.get(getClass().getName()),
                    MessageElements.PREFIX_LOCAL + MessageElements.NO_SUCH,
                    MessageElements.PUBLIC_KEY,
                    uuid)
            );

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
                logger.info(String.format(
                        AlignedLogMessages.FORMAT__30C_30C_DATA,
                        AlignedLogMessages.OFFSETS_KEYSTORE_CLASSES.get(getClass().getName()),
                        MessageElements.PREFIX_LOCAL + MessageElements.SYNC,
                        MessageElements.PUBLIC_KEY + " " + MessageElements.KEPT,
                        uuid)
                );

            } else {
                publicKeys.remove(uuid);
                deleted++;

                logger.info(String.format(
                        AlignedLogMessages.FORMAT__30C_30C_DATA,
                        AlignedLogMessages.OFFSETS_KEYSTORE_CLASSES.get(getClass().getName()),
                        MessageElements.PREFIX_LOCAL + MessageElements.SYNC, MessageElements.PUBLIC_KEY + " " + MessageElements.DELETED,
                        uuid)
                );
            }
        }

        logger.info(String.format(
                AlignedLogMessages.FORMAT__SPACES_30C_30C_DATA_DATA,
                AlignedLogMessages.OFFSETS_KEYSTORE_CLASSES.get(getClass().getName()),
                MessageElements.PREFIX_LOCAL + MessageElements.SYNC,
                MessageElements.PUBLIC_KEY + " " + MessageElements.KEPT + ", " + MessageElements.DELETED,
                toBeKept.size(), deleted)
        );
    }

    @Override
    public void setPublicAndPrivateKeys(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey){
        currentUUID = uuid;
        this.privateKey = privateKey;
        publicKeys.put(uuid, publicKey);

        logger.info(String.format(
                AlignedLogMessages.FORMAT__30C_30C_DATA,
                AlignedLogMessages.OFFSETS_KEYSTORE_CLASSES.get(getClass().getName()),
                MessageElements.PREFIX_LOCAL + MessageElements.SET,
                MessageElements.PUBLIC_AND_PRIVATE_KEY,
                uuid)
        );
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
            logger.info(String.format(
                    AlignedLogMessages.FORMAT__30C_30C,
                    AlignedLogMessages.OFFSETS_KEYSTORE_CLASSES.get(getClass().getName()),
                    MessageElements.PREFIX_LOCAL + MessageElements.NOT_SET,
                    MessageElements.PUBLIC_AND_PRIVATE_KEY)
            );

            throw new NoSuchElementException();
        }
    }

    @Override
    public void dropEverything() {
        publicKeys = new HashMap<>();
        currentUUID = null;
        privateKey = null;
        logger.info(String.format(
                AlignedLogMessages.FORMAT__30C_30C,
                AlignedLogMessages.OFFSETS_KEYSTORE_CLASSES.get(getClass().getName()),
                MessageElements.PREFIX_LOCAL + "DROPPED ALL",
                MessageElements.PUBLIC_AND_PRIVATE_KEY)
        );
    }
}
