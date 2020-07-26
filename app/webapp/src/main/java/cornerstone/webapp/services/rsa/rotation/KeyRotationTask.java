package cornerstone.webapp.services.rsa.rotation;

import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.services.rsa.store.db.DbPublicKeyStoreException;
import cornerstone.webapp.services.rsa.store.db.DbPublicKeyStoreInterface;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStoreInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;

public class KeyRotationTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(KeyRotationTask.class);
    private static final String MESSAGE_ROTATION_TASK_STARTED                       = "... key rotation task STARTED";
    private static final String MESSAGE_ROTATION_TASK_FINISHED                      = "... key rotation task FINISHED";
    private static final String MESSAGE_N_EXPIRED_KEYS_REMOVED_FROM_DB              = "... %d expired key(s) removed from DB";
    private static final String ERROR_MESSAGE_FAILED_TO_STORE_PUBLIC_KEY_IN_DB      = "... key rotation task FAILURE - FAILED TO STORE PUBLIC KEY IN DB (%s)";
    private static final String ERROR_MESSAGE_FAILED_TO_GET_PUBLIC_KEY_UUIDS        = "... key rotation task FAILURE - FAILED TO GET PUBLIC KEY UUIDs FROM DB";
    private static final String ERROR_MESSAGE_FAILED_TO_CLEANUP_LOCAL_STORE         = "... key rotation task FAILURE - FAILED TO CLEANUP LOCAL PUBLIC KEYS  (MEMORY)";
    private static final String ERROR_MESSAGE_FAILED_TO_CLEANUP_REMOTE_STORE        = "... key rotation task FAILURE - FAILED TO CLEANUP REMOTE PUBLIC KEYS (DB)";

    private final LocalKeyStoreInterface localKeyStore;
    private final DbPublicKeyStoreInterface dbPublicKeyStore;
    private final int rsaTTL;
    private final String nodeName;

    public KeyRotationTask(final LocalKeyStoreInterface localKeyStore,
                           final DbPublicKeyStoreInterface dbPublicKeyStore,
                           final int rsaTTL,
                           final String nodeName) {

        this.localKeyStore = localKeyStore;
        this.dbPublicKeyStore = dbPublicKeyStore;
        this.rsaTTL = rsaTTL;
        this.nodeName = nodeName;
        logger.info(String.format(DefaultLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    // step 1
    private void changeKeysLocallyAndStorePublicKeyAndItsUUIDinDb() {
        final KeyPairWithUUID kp = new KeyPairWithUUID();
        final String base64_pub_key = Base64.getEncoder().encodeToString(kp.keyPair.getPublic().getEncoded());

        localKeyStore.setKeysForSigning(kp.uuid, kp.keyPair.getPrivate(), kp.keyPair.getPublic());
        try {
            dbPublicKeyStore.addPublicKey(kp.uuid, nodeName, rsaTTL, base64_pub_key);
        } catch (final DbPublicKeyStoreException e) {
            logger.error(String.format(ERROR_MESSAGE_FAILED_TO_STORE_PUBLIC_KEY_IN_DB, kp.uuid));
        }
    }

    // step 2
    private void cleanUpLocalStoreKeepOnlyActivePubKeysReceivedFromDb() {
        try{
            final List<UUID> activePubKeyUUIDs = dbPublicKeyStore.getActiveKeyUUIDs();
            localKeyStore.keepOnly(activePubKeyUUIDs);

        } catch (final  DbPublicKeyStoreException e){
            logger.error(ERROR_MESSAGE_FAILED_TO_GET_PUBLIC_KEY_UUIDS);
        }

    }

    // step 3
    private void removeExpiredPubKeysFromDb() {
        try {
            final int n = dbPublicKeyStore.deleteExpiredPublicKeys();
            logger.info(String.format(MESSAGE_N_EXPIRED_KEYS_REMOVED_FROM_DB, n));

        } catch (final DbPublicKeyStoreException e) {
            logger.error(ERROR_MESSAGE_FAILED_TO_CLEANUP_REMOTE_STORE);
        }
    }

    @Override
    public void run() {
        logger.info(MESSAGE_ROTATION_TASK_STARTED);
        changeKeysLocallyAndStorePublicKeyAndItsUUIDinDb();
        cleanUpLocalStoreKeepOnlyActivePubKeysReceivedFromDb();
        removeExpiredPubKeysFromDb();
        logger.info(MESSAGE_ROTATION_TASK_FINISHED);
    }
}
