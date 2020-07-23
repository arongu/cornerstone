package cornerstone.webapp.services.rsa.rotation;

import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStoreInterface;
import cornerstone.webapp.services.rsa.store.db.DbPublicKeyStoreException;
import cornerstone.webapp.services.rsa.store.db.DbPublicKeyStoreInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.TimerTask;

public class KeyRotationTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(KeyRotationTask.class);

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
        logger.info("... key rotation task STARTED");
        final KeyPairWithUUID kpu = new KeyPairWithUUID();
        final String base64_key = Base64.getEncoder().encodeToString(kpu.keyPair.getPublic().getEncoded());

        localKeyStore.setSigningKey(kpu.uuid, kpu.keyPair.getPrivate(), kpu.keyPair.getPublic());

        try {
            dbPublicKeyStore.addPublicKey(kpu.uuid, nodeName, rsaTTL, base64_key);
            logger.info("... key rotation task FINISHED");

        } catch (final DbPublicKeyStoreException e){
            logger.error(e.getMessage());
        }
    }
}
