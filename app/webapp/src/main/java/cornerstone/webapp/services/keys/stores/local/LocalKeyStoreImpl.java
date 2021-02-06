package cornerstone.webapp.services.keys.stores.local;

import cornerstone.webapp.common.AlignedLogMessages;
import cornerstone.webapp.services.keys.stores.log.MessageElements;
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

public class LocalKeyStoreImpl implements LocalKeyStore {
    private static final Logger logger = LoggerFactory.getLogger(LocalKeyStoreImpl.class);

    private Map<UUID, PublicKey> publicKeys;
    private PrivateKey livePrivateKey = null;
    private PublicKey  livePublicKey  = null;
    private UUID       live_uuid      = null;

    public LocalKeyStoreImpl() {
        publicKeys = new ConcurrentHashMap<>();
    }

    @Override
    public void addPublicKey(final UUID uuid, final PublicKey publicKey) {
        publicKeys.put(uuid, publicKey);
        final String logMsg = String.format(
                AlignedLogMessages.FORMAT__OFFSET_35C_35C_C_STR,
                AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                MessageElements.PREFIX_LOCAL + MessageElements.ADDED, MessageElements.PUBLIC_KEY, uuid
        );

        logger.info(logMsg);
    }

    @Override
    public void addPublicKey(final UUID uuid, final String base64KeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final byte[] ba                  = Base64.getDecoder().decode(base64KeyString.getBytes());
        final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(ba);
        final PublicKey publicKey        = KeyFactory.getInstance("RSA").generatePublic(keySpec);
        addPublicKey(uuid, publicKey);
    }

    @Override
    public void deletePublicKey(final UUID uuid) {
        publicKeys.remove(uuid);
        final String logMsg = String.format(
                AlignedLogMessages.FORMAT__OFFSET_35C_35C_C_STR,
                AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                MessageElements.PREFIX_LOCAL + MessageElements.DELETED, MessageElements.PUBLIC_KEY, uuid
        );

        logger.info(logMsg);
    }

    @Override
    public PublicKey getPublicKey(final UUID uuid) throws NoSuchElementException {
        final PublicKey keyData = publicKeys.get(uuid);
        if ( null != keyData) {
            return keyData;
        } else {
            final String logMsg = String.format(
                    AlignedLogMessages.FORMAT__OFFSET_35C_35C_C_STR,
                    AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                    MessageElements.PREFIX_LOCAL + MessageElements.NO_SUCH, MessageElements.PUBLIC_KEY, uuid
            );

            logger.info(logMsg);
            throw new NoSuchElementException();
        }
    }

    @Override
    public void deletePublicKeys(final List<UUID> uuidsToBeRemoved) {
        publicKeys.keySet().forEach(uuid -> {
            if ( uuidsToBeRemoved.contains(uuid)){
                deletePublicKey(uuid);
            }
        });
    }

    @Override
    public void sync(final List<UUID> toBeKept) {
        int deleted = 0;
        for ( final UUID uuid : publicKeys.keySet()) {
            if ( uuid.equals(live_uuid) || toBeKept.contains(uuid)) {
                final String logMsg = String.format(
                        AlignedLogMessages.FORMAT__OFFSET_35C_35C_C_STR,
                        AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                        MessageElements.PREFIX_LOCAL + MessageElements.SYNC, MessageElements.PUBLIC_KEY + " " + MessageElements.KEPT, uuid
                );

                logger.info(logMsg);

            } else {
                publicKeys.remove(uuid);
                deleted++;

                final String logMsg = String.format(
                        AlignedLogMessages.FORMAT__OFFSET_35C_35C_C_STR,
                        AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                        MessageElements.PREFIX_LOCAL + MessageElements.SYNC, MessageElements.PUBLIC_KEY + " " + MessageElements.DELETED, uuid
                );

                logger.info(logMsg);
            }
        }

        final String logMsg = String.format(
                AlignedLogMessages.FORMAT__OFFSET_35C_35C_STR_STR,
                AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                MessageElements.PREFIX_LOCAL + MessageElements.SYNC, MessageElements.PUBLIC_KEY + " " + MessageElements.KEPT + ", " + MessageElements.DELETED, toBeKept.size(), deleted
        );

        logger.info(logMsg);
    }

    @Override
    public void setupSigning(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey){
        this.live_uuid = uuid;
        this.livePrivateKey = privateKey;
        this.livePublicKey  = publicKey;
        publicKeys.put(uuid, publicKey);

        final String logMsg = String.format(
                AlignedLogMessages.FORMAT__OFFSET_35C_35C_C_STR,
                AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                MessageElements.PREFIX_LOCAL + MessageElements.SET, MessageElements.PUBLIC_AND_PRIVATE_KEY, uuid
        );

        logger.info(logMsg);
    }

    @Override
    public Set<UUID> getPublicKeyUUIDs() {
        return publicKeys.keySet();
    }

    @Override
    public SigningKeySetup getSigningKeySetup() throws SigningKeySetupException {
        if ( null == livePrivateKey) {
            final String errorLog = String.format(
                    AlignedLogMessages.FORMAT__OFFSET_35C_35C,
                    AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                    MessageElements.PREFIX_LOCAL + MessageElements.NOT_SET, MessageElements.PUBLIC_AND_PRIVATE_KEY
            );

            logger.error(errorLog);
            throw new SigningKeySetupException("Signing keys are not initialized!");

        } else {
            return new SigningKeySetup(live_uuid, livePrivateKey, livePublicKey);
        }
    }

    @Override
    public void resetAll() {
        publicKeys = new HashMap<>();
        live_uuid = null;
        livePrivateKey = null;

        final String logMsg = String.format(
                AlignedLogMessages.FORMAT__OFFSET_35C_35C,
                AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                MessageElements.PREFIX_LOCAL + "DROPPED ALL", MessageElements.PUBLIC_AND_PRIVATE_KEY
        );

        logger.info(logMsg);
    }
}
