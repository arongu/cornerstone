package cornerstone.webapp.services.rsakey.storage.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

public class LocalKeyStore implements LocalKeyStoreInterface {
    private static final String MESSAGE_PUBLIC_KEY_ADDED          = "... PublicKey with UUID: '%s' ADDED ...";
    private static final String MESSAGE_PUBLIC_KEY_REMOVED        = "... PublicKey with UUID: '%s' REMOVED ...";
    private static final String MESSAGE_PUBLIC_KEY_RETURNED       = "... PublicKey with UUID: '%s' RETURNED ...";
    private static final String MESSAGE_PUBLIC_KEY_DOES_NOT_EXIST = "... PublicKey with UUID: '%s' DOES NOT EXIST ...";

    private static final String MESSAGE_SIGNING_KEYS_SET          = "... Signing keys with UUID: '%s' HAVE BEEN SET ...";
    private static final String MESSAGE_SIGNING_KEYS_REMOVED      = "... Signing keys REMOVED ...";
    private static final String MESSAGE_SIGNING_KEYS_RETURNED     = "... Signing keys with UUID: '%s' RETURNED ...";
    private static final String MESSAGE_SIGNING_KEYS_ARE_NOT_SET  = "... Signing keys ARE NOT SET ...";
    private static final String MESSAGE_RESET                     = "... !!! RESET COMPLETE !!! ...";

    private static final Logger logger = LoggerFactory.getLogger(LocalKeyStore.class);

    private Map<UUID, PublicKey> publicKeys;
    private UUID signingKeyUUID = null;
    private PrivateKey privateKey = null;

    public LocalKeyStore() {
        publicKeys = new HashMap<>();
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
    public void storePublicKey(final UUID uuid, final PublicKey publicKey) {
        publicKeys.put(uuid, publicKey);
        logger.info(String.format(MESSAGE_PUBLIC_KEY_ADDED, uuid.toString()));
    }

    @Override
    public void removePublicKey(final UUID uuid) {
        publicKeys.remove(uuid);
        logger.info(String.format(MESSAGE_PUBLIC_KEY_REMOVED, uuid.toString()));
    }

    @Override
    public void setSigningKey(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey){
        signingKeyUUID = uuid;
        this.privateKey = privateKey;
        publicKeys.put(uuid, publicKey);
        logger.info(String.format(MESSAGE_SIGNING_KEYS_SET, uuid.toString()));
    }

    @Override
    public PrivateKeyWithUUID getSigningKey() throws NoSuchElementException {
        if (null != privateKey) {
            logger.info(String.format(MESSAGE_SIGNING_KEYS_RETURNED, signingKeyUUID.toString()));
            return new PrivateKeyWithUUID(signingKeyUUID, privateKey);
        } else {
            logger.info(MESSAGE_SIGNING_KEYS_ARE_NOT_SET);
            throw new NoSuchElementException();
        }
    }

    @Override
    public void unsetSigningKey() {
        privateKey  = null;
        signingKeyUUID = null;
        logger.info(MESSAGE_SIGNING_KEYS_REMOVED);
    }

    @Override
    public void resetAll() {
        publicKeys.clear();
        publicKeys = new HashMap<>();

        signingKeyUUID = null;
        privateKey = null;
        logger.info(MESSAGE_RESET);
    }
}
