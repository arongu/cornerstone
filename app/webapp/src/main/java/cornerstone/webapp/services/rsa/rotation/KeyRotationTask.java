package cornerstone.webapp.services.rsa.rotation;

import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreException;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreInterface;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStoreInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.TimerTask;

public class KeyRotationTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(KeyRotationTask.class);
    private static final String MESSAGE_ROTATION_TASK_STARTED                       = "STARTED";
    private static final String MESSAGE_ROTATION_TASK_FINISHED                      = "FINISHED";
    private static final String ERROR_MESSAGE_FAILED_TO_STORE_PUBLIC_KEY_IN_DB      = "FAILED TO STORE PUBLIC KEY IN DB (%s)";
    private static final String ERROR_MESSAGE_FAILED_TO_SYNC_LOCAL_STORE_WITH_DB    = "FAILED TO SYNC KEYS WITH DB (KEEPING EVERYTHING IN MEMORY)";
    private static final String ERROR_MESSAGE_FAILED_TO_DELETE_EXPIRED_KEYS_FROM_DB = "FAILED TO DELETE EXPIRED KEYS FROM DB";


    private final LocalKeyStoreInterface localKeyStore;
    private final PublicKeyStoreInterface dbPublicKeyStore;
    private final int rsaTTL;
    private final String nodeName;

    public KeyRotationTask(final LocalKeyStoreInterface localKeyStore,
                           final PublicKeyStoreInterface dbPublicKeyStore,
                           final int rsaTTL,
                           final String nodeName) {

        this.localKeyStore = localKeyStore;
        this.dbPublicKeyStore = dbPublicKeyStore;
        this.rsaTTL = rsaTTL;
        this.nodeName = nodeName;
        logger.info(String.format(DefaultLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    // step 1
    private void changeLocalKeysThenStorePublicKeyAndItsUUIDinDb() {
        final KeyPairWithUUID kp = new KeyPairWithUUID();
        final String base64_pub_key = Base64.getEncoder().encodeToString(kp.keyPair.getPublic().getEncoded());

        localKeyStore.setPublicAndPrivateKeys(kp.uuid, kp.keyPair.getPrivate(), kp.keyPair.getPublic());

        try {
            dbPublicKeyStore.addKey(kp.uuid, nodeName, rsaTTL, base64_pub_key);
        } catch (final PublicKeyStoreException e) {
            logger.error(String.format(ERROR_MESSAGE_FAILED_TO_STORE_PUBLIC_KEY_IN_DB, kp.uuid));
        }
    }

    // step 2
    private void cleanUpLocalPublicKeysKeepOnlyActiveKeysFromDb() {
        try{
            localKeyStore.sync(dbPublicKeyStore.getLiveKeyUUIDs());

        } catch (final PublicKeyStoreException e){
            logger.error(ERROR_MESSAGE_FAILED_TO_SYNC_LOCAL_STORE_WITH_DB);
        }
    }

    // step 3
    private void cleanUpDbRemoveExpiredPublicKeys() {
        try {
            dbPublicKeyStore.deleteExpiredKeys();

        } catch (final PublicKeyStoreException e) {
            logger.error(ERROR_MESSAGE_FAILED_TO_DELETE_EXPIRED_KEYS_FROM_DB);
        }
    }

    @Override
    public void run() {
        logger.info(MESSAGE_ROTATION_TASK_STARTED);
        changeLocalKeysThenStorePublicKeyAndItsUUIDinDb();
        cleanUpLocalPublicKeysKeepOnlyActiveKeysFromDb();
        cleanUpDbRemoveExpiredPublicKeys();
        logger.info(MESSAGE_ROTATION_TASK_FINISHED);
    }
}
