package cornerstone.webapp.services.rsa.rotation;

import cornerstone.webapp.common.AlignedLogMessages;
import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStore;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreException;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.TimerTask;

public class KeyRotationTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(KeyRotationTask.class);

    private final LocalKeyStore localKeyStore;
    private final PublicKeyStore publicKeyStore;
    private final int rsaTTL;
    private final String nodeName;

    public KeyRotationTask(final LocalKeyStore localKeyStore,
                           final PublicKeyStore publicKeyStore,
                           final int rsaTTL,
                           final String nodeName) {

        this.localKeyStore = localKeyStore;
        this.publicKeyStore = publicKeyStore;
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
            publicKeyStore.addKey(kp.uuid, nodeName, rsaTTL, base64_pub_key);

        } catch (final PublicKeyStoreException e) {
            logger.error(String.format(AlignedLogMessages.FORMAT__OFFSET_S_S,
                    AlignedLogMessages.OFFSETS_KEYSTORE_CLASSES.get(getClass().getName()),
                    "FAILED TO STORE PUBLIC KEY IN DB",
                    kp.uuid)
            );
        }
    }

    // step 2
    private void cleanUpLocalPublicKeysKeepOnlyActiveKeysFromDb() {
        try {
            localKeyStore.sync(publicKeyStore.getLiveKeyUUIDs());

        } catch (final PublicKeyStoreException e) {
            logger.error(String.format(AlignedLogMessages.FORMAT__OFFSET_S,
                    AlignedLogMessages.OFFSETS_KEYSTORE_CLASSES.get(getClass().getName()),
                    "FAILED TO SYNC LOCAL <- DB (KEEPING EVERYTHING IN LOCAL STORE)")
            );
        }
    }

    // step 3
    private void cleanUpDbRemoveExpiredPublicKeys() {
        try {
            publicKeyStore.deleteExpiredKeys();

        } catch (final PublicKeyStoreException e) {
            logger.error(String.format(AlignedLogMessages.FORMAT__OFFSET_S,
                    AlignedLogMessages.OFFSETS_KEYSTORE_CLASSES.get(getClass().getName()),
                    "FAILED TO DELETE EXPIRED KEYS FROM DB")
            );
        }
    }

    @Override
    public void run() {
        logger.info(String.format(AlignedLogMessages.FORMAT__OFFSET_S,
                AlignedLogMessages.OFFSETS_KEYSTORE_CLASSES.get(getClass().getName()),
                "(STARTED)  ------------------------------------------------------------------------------------------")
        );

        changeLocalKeysThenStorePublicKeyAndItsUUIDinDb();
        cleanUpLocalPublicKeysKeepOnlyActiveKeysFromDb();
        cleanUpDbRemoveExpiredPublicKeys();

        logger.info(String.format(AlignedLogMessages.FORMAT__OFFSET_S,
                AlignedLogMessages.OFFSETS_KEYSTORE_CLASSES.get(getClass().getName()),
                "------------------------------------------------------------------------------------------ (FINISHED)")
        );
    }
}
