package cornerstone.webapp.services.rsa.rotation;

import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStoreInterface;
import cornerstone.webapp.services.rsa.store.db.DbPublicKeyStoreInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Timer;

public class KeyRotator implements KeyRotatorInterface {
    private static final Logger logger = LoggerFactory.getLogger(KeyRotator.class);

    private final DbPublicKeyStoreInterface databasePublicKeyStore;
    private final LocalKeyStoreInterface localKeyStore;
    private final ConfigurationLoader configurationLoader;
    private final Timer timer;

    @Inject
    public KeyRotator(final ConfigurationLoader configurationLoader,
                      final DbPublicKeyStoreInterface dbPublicKeyStore,
                      final LocalKeyStoreInterface localKeyStore) {

        this.configurationLoader = configurationLoader;
        this.localKeyStore = localKeyStore;
        this.databasePublicKeyStore = dbPublicKeyStore;

        timer = new Timer(getClass().getName());
        runRotationTask();
        logger.info(String.format(DefaultLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    @Override
    public void runRotationTask() {
        final String nodeName = configurationLoader.getAppProperties().getProperty(APP_ENUM.APP_NODE_NAME.key);
        final int rsaTTL = Integer.parseInt(configurationLoader.getAppProperties().getProperty(APP_ENUM.APP_RSA_TTL.key));
        final long period = rsaTTL * 1000;
        timer.schedule(new KeyRotationTask(localKeyStore, databasePublicKeyStore, rsaTTL, nodeName), 0, period);
    }
}
