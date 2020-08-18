package cornerstone.webapp.service.rsa.rotation;

import cornerstone.webapp.common.AlignedLogMessages;
import cornerstone.webapp.common.CommonLogMessages;
import cornerstone.webapp.service.rsa.store.db.PublicKeyStore;
import cornerstone.webapp.service.rsa.store.db.PublicKeyStoreException;
import cornerstone.webapp.service.rsa.store.local.LocalKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.TimerTask;

public class KeyRotationTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(KeyRotationTask.class);

    private final LocalKeyStore localKeyStore;
    private final PublicKeyStore publicKeyStore;
    private final int rsaTTL;
    private final int jwtTTL;
    private final String nodeName;

    public KeyRotationTask(final LocalKeyStore localKeyStore,
                           final PublicKeyStore publicKeyStore,
                           final int rsaTTL,
                           final int jwtTTL,
                           final String nodeName) {

        this.localKeyStore = localKeyStore;
        this.publicKeyStore = publicKeyStore;
        this.rsaTTL = rsaTTL;
        this.jwtTTL = jwtTTL;
        this.nodeName = nodeName;
        logger.info(String.format(CommonLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    // step 1
    private void changeLocalKeysThenStorePublicKeyAndItsUUIDinDb() {
        final KeyPairWithUUID kp = new KeyPairWithUUID();
        final String base64_pub_key = Base64.getEncoder().encodeToString(kp.keyPair.getPublic().getEncoded());

        localKeyStore.setLiveKeys(kp.uuid, kp.keyPair.getPrivate(), kp.keyPair.getPublic());

        try {
            // TTL = the last second when the RSA expires + JWT token TTL
            // SCENARIO: someone gets a token in the last moment before the RSA expires
            // JWT still needs to be valid - therefore TTL is calculated as worst case scenario
            // RSA key rotates still as RSA TTL demands it, however public key pairs are available = now() + RSA TTL + JWT TTL
            publicKeyStore.addKey(kp.uuid, nodeName, rsaTTL + jwtTTL, base64_pub_key);

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
