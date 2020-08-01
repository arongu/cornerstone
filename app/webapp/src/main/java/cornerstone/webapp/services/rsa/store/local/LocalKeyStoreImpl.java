package cornerstone.webapp.services.rsa.store.local;

import cornerstone.webapp.common.LogMessageLines;
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
                LogMessageLines.SPACES__STATUS_WORD__FIELD__DATA,
                LogMessageLines.classNameOffsetSpaces.get(getClass().getName()),
                "(LOCAL) ADDED", "PUBLIC KEY", uuid)
        );
    }

    @Override
    public void deletePublicKey(final UUID uuid) {
        publicKeys.remove(uuid);
        logger.info(String.format(
                LogMessageLines.SPACES__STATUS_WORD__FIELD__DATA,
                LogMessageLines.classNameOffsetSpaces.get(getClass().getName()),
                "(LOCAL) DELETED", "PUBLIC KEY", uuid)
        );
    }

    @Override
    public PublicKey getPublicKey(final UUID uuid) throws NoSuchElementException {
        final PublicKey keyData = publicKeys.get(uuid);
        if (null != keyData){
            return keyData;
        } else {
            logger.info(String.format(
                    LogMessageLines.SPACES__STATUS_WORD__FIELD__DATA,
                    LogMessageLines.classNameOffsetSpaces.get(getClass().getName()),
                    "(LOCAL) NO SUCH", "PUBLIC KEY", uuid)
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
                        LogMessageLines.SPACES__STATUS_WORD__FIELD__DATA,
                        LogMessageLines.classNameOffsetSpaces.get(getClass().getName()),
                        "(LOCAL) SYNC", "PUBLIC KEY KEPT", uuid)
                );

            } else {
                publicKeys.remove(uuid);
                deleted++;

                logger.info(String.format(
                        LogMessageLines.SPACES__STATUS_WORD__FIELD__DATA,
                        LogMessageLines.classNameOffsetSpaces.get(getClass().getName()),
                        "(LOCAL) SYNC", "PUBLIC KEY DELETED", uuid)
                );
            }
        }

        logger.info(String.format(
                LogMessageLines.SPACES__STATUS_WORD__FIELD__DATA__DATA,
                LogMessageLines.classNameOffsetSpaces.get(getClass().getName()),
                "(LOCAL) SYNC", "PUBLIC KEY KEPT, DELETED", publicKeys.size(), deleted)
        );
    }

    @Override
    public void setPublicAndPrivateKeys(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey){
        currentUUID = uuid;
        this.privateKey = privateKey;
        publicKeys.put(uuid, publicKey);

        logger.info(String.format(
                LogMessageLines.SPACES__STATUS_WORD__FIELD__DATA,
                LogMessageLines.classNameOffsetSpaces.get(getClass().getName()),
                "(LOCAL) SET", "PUBLIC AND PRIVATE KEY", uuid)
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
                    LogMessageLines.SPACES__STATUS_WORD__FIELD,
                    LogMessageLines.classNameOffsetSpaces.get(getClass().getName()),
                    "(LOCAL) NOT SET", "PUBLIC AND PRIVATE KEY")
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
                LogMessageLines.SPACES__STATUS_WORD__FIELD,
                LogMessageLines.classNameOffsetSpaces.get(getClass().getName()),
                "(LOCAL) DROPPED ALL", "PUBLIC AND PRIVATE KEY")
        );
    }
}
