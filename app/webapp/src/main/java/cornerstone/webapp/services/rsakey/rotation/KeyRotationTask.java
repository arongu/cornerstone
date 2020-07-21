package cornerstone.webapp.services.rsakey.rotation;

import cornerstone.webapp.services.rsakey.store.local.LocalKeyStore;
import cornerstone.webapp.services.rsakey.store.local.LocalKeyStoreInterface;
import cornerstone.webapp.services.rsakey.store.remote.DBPublicKeyStore;
import cornerstone.webapp.services.rsakey.store.remote.DBPublicKeyStoreException;
import cornerstone.webapp.services.rsakey.store.remote.DBPublicKeyStoreInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.TimerTask;

public class KeyRotationTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(KeyRotationTask.class);

    private final LocalKeyStoreInterface localKeyStore;
    private final DBPublicKeyStoreInterface dbPublicKeyStore;
    private final int rsaTTL;
    private final String nodeName;

    public KeyRotationTask(final LocalKeyStoreInterface localKeyStore,
                           final DBPublicKeyStoreInterface dbPublicKeyStore,
                           final int rsaTTL,
                           final String nodeName) {

        this.localKeyStore = localKeyStore;
        this.dbPublicKeyStore = dbPublicKeyStore;
        this.rsaTTL = rsaTTL;
        this.nodeName = nodeName;
    }

    @Override
    public void run() {
        final KeyPairWithUUID kpu = new KeyPairWithUUID();
        final String base64_key = Base64.getEncoder().encodeToString(kpu.keyPair.getPublic().getEncoded());

        localKeyStore.setSigningKey(kpu.uuid, kpu.keyPair.getPrivate(), kpu.keyPair.getPublic());

        try {
            dbPublicKeyStore.addPublicKey(kpu.uuid, nodeName, rsaTTL, base64_key);

        } catch (final DBPublicKeyStoreException e){
            logger.error(e.getMessage());
        }
    }
}
