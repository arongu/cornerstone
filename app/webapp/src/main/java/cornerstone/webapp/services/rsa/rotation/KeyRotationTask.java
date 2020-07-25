package cornerstone.webapp.services.rsa.rotation;

import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.services.rsa.store.db.DbPublicKeyStoreException;
import cornerstone.webapp.services.rsa.store.db.DbPublicKeyStoreInterface;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStoreInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.TimerTask;

public class KeyRotationTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(KeyRotationTask.class);
    private static final String MESSAGE_ROTATION_TASK_STARTED                       = "... key rotation task STARTED";
    private static final String MESSAGE_ROTATION_TASK_FINISHED                      = "... key rotation task FINISHED";
    private static final String ERROR_MESSAGE_FAILED_TO_STORE_PUBLIC_KEY_IN_DB      = "... key rotation task FAILURE - FAILED TO STORE PUBLIC KEY IN DB (%s)";

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

    @Override
    public void run() {
        logger.info(MESSAGE_ROTATION_TASK_STARTED);
        final KeyPairWithUUID kpu = new KeyPairWithUUID();
        final String base64_key = Base64.getEncoder().encodeToString(kpu.keyPair.getPublic().getEncoded());

        localKeyStore.setSigningKey(kpu.uuid, kpu.keyPair.getPrivate(), kpu.keyPair.getPublic());

        try {
            dbPublicKeyStore.addPublicKey(kpu.uuid, nodeName, rsaTTL, base64_key);
            logger.info(MESSAGE_ROTATION_TASK_FINISHED);

        } catch (final DbPublicKeyStoreException e){
            logger.error(String.format(ERROR_MESSAGE_FAILED_TO_STORE_PUBLIC_KEY_IN_DB, kpu.uuid));
            logger.error(e.getMessage());
        }
    }
}
