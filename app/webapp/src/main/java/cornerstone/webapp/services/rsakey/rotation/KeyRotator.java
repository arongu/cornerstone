package cornerstone.webapp.services.rsakey.rotation;

import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.services.rsakey.store.local.LocalKeyStore;
import cornerstone.webapp.services.rsakey.store.local.LocalKeyStoreInterface;
import cornerstone.webapp.services.rsakey.store.remote.DBPublicKeyStore;
import cornerstone.webapp.services.rsakey.store.remote.DBPublicKeyStoreInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Timer;

public class KeyRotator implements KeyRotatorInterface {
    private static final Logger logger = LoggerFactory.getLogger(KeyRotator.class);

    private final DBPublicKeyStoreInterface databasePublicKeyStore;
    private final LocalKeyStoreInterface localKeyStore;
    private final ConfigurationLoader configurationLoader;
    private Timer timer;

    @Inject
    public KeyRotator(final ConfigurationLoader configurationLoader,
                      final DBPublicKeyStoreInterface dbPublicKeyStore,
                      final LocalKeyStoreInterface localKeyStore) {

        logger.info("-------------------------------------------------------------------------------------------------------------");
        this.configurationLoader = configurationLoader;
        this.localKeyStore = localKeyStore;
        this.databasePublicKeyStore = dbPublicKeyStore;

        final String nodeName = configurationLoader.getAppProperties().getProperty(APP_ENUM.APP_NODE_NAME.key);
        final int rsaTTL = Integer.parseInt(configurationLoader.getAppProperties().getProperty(APP_ENUM.APP_RSA_TTL.key));
        final long period = rsaTTL * 1000;

        final KeyRotationTask task = new KeyRotationTask(localKeyStore, databasePublicKeyStore, rsaTTL, nodeName);
        timer = new Timer();
        timer.schedule(task, 0, period);
    }

    @Override
    public void rotateKeys() {

    }
}
