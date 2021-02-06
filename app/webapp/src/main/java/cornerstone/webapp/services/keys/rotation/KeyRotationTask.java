package cornerstone.webapp.services.keys.rotation;

import cornerstone.webapp.logmsg.AlignedLogMessages;
import cornerstone.webapp.logmsg.CommonLogMessages;
import cornerstone.webapp.services.keys.stores.db.PublicKeyStore;
import cornerstone.webapp.services.keys.stores.db.PublicKeyStoreException;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.TimerTask;

/**
 * This task is responsible of the RSA key pair rotation invoked by KeyRotator(KeyRotatorImpl)
 * - private key will be stored ONLY in the local store
 * - public key will be uploaded into the database
 */

public class KeyRotationTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(KeyRotationTask.class);

    private final LocalKeyStore localKeyStore;
    private final PublicKeyStore publicKeyStore;
    private final int rsaTTL;
    private final int jwtTTL;
    private final String nodeName;

    /**
     * Class constructor
     * @param localKeyStore LocalKeyStore (stores and manages public and private keys locally (local cache))
     * @param publicKeyStore PublicKeyStore (manages local public keys and syncs them with the db)
     * @param rsaTTL time to live of the RSA key itself, how often the keys need to be rotated (configurable via app conf file)
     * @param jwtTTL time to live of the JWT/JWS, how long a JWS should be valid when issued (configurable via app conf file)
     * @param nodeName name of the node, this will be added to the JWS as well (configurable via app conf file)
     */
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
    /**
     * Changes the local private and public key, then store the public part in the db, with its unique UUID as well.
     * Note:
     * The final TTL value of the keys calculated with the following formula:
     * TTL = the last second when the RSA expires + JWT token TTL
     * SCENARIO: Someone gets a token in the last moment before the RSA key expires.
     *           JWT still needs to be valid for the user, therefore TTL is calculated as worst case scenario:
     *           moment when the key rotation happened( now() ) + RSA TTL + JWT TTL
     */
    private void changeLocalKeysThenStorePublicKeyAndItsUUIDinDb() {
        final KeyPairWithUUID kp = new KeyPairWithUUID();
        final String base64_pub_key = Base64.getEncoder().encodeToString(kp.keyPair.getPublic().getEncoded());

        localKeyStore.setSigningKeys(kp.uuid, kp.keyPair.getPrivate(), kp.keyPair.getPublic());

        try {
            publicKeyStore.addKey(kp.uuid, nodeName, rsaTTL + jwtTTL, base64_pub_key);

        } catch (final PublicKeyStoreException e) {
            logger.error(String.format(AlignedLogMessages.FORMAT__OFFSET_STR_STR,
                    AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                    "FAILED TO STORE PUBLIC KEY IN DB",
                    kp.uuid)
            );
        }
    }

    // step 2
    /**
     * Gets the UUIDs of the live keys from the database.
     * Calls localKeystore and syncs them with the UUIDs.
     * UUIDs that are not listed as 'live' will be removed from the local cache.
     */
    private void cleanUpLocalPublicKeysKeepOnlyActiveKeysFromDb() {
        try {
            localKeyStore.keepOnly(publicKeyStore.getLiveKeyUUIDs());

        } catch (final PublicKeyStoreException e) {
            logger.error(String.format(AlignedLogMessages.FORMAT__OFFSET_STR,
                    AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                    "FAILED TO SYNC LOCAL <- DB (KEEPING EVERYTHING IN LOCAL STORE)")
            );
        }
    }

    // step 3
    /**
     * Calls publicKeystore which runs a query on the db to get rid off the expired keys.
     */
    private void cleanUpDbRemoveExpiredPublicKeys() {
        try {
            publicKeyStore.deleteExpiredKeys();

        } catch (final PublicKeyStoreException e) {
            logger.error(String.format(AlignedLogMessages.FORMAT__OFFSET_STR,
                    AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                    "FAILED TO DELETE EXPIRED KEYS FROM DB")
            );
        }
    }

    @Override
    public void run() {
        logger.info(String.format(AlignedLogMessages.FORMAT__OFFSET_STR,
                AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                "(STARTED)  ------------------------------------------------------------------------------------------")
        );

        changeLocalKeysThenStorePublicKeyAndItsUUIDinDb();
        cleanUpLocalPublicKeysKeepOnlyActiveKeysFromDb();
        cleanUpDbRemoveExpiredPublicKeys();

        logger.info(String.format(AlignedLogMessages.FORMAT__OFFSET_STR,
                AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                "------------------------------------------------------------------------------------------ (FINISHED)")
        );
    }
}
