package cornerstone.webapp.services.rsa.rotation;

import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStore;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Properties;
import java.util.Timer;

public class KeyRotatorImpl implements KeyRotator {
    private static final Logger logger = LoggerFactory.getLogger(KeyRotatorImpl.class);

    private final PublicKeyStore databasePublicKeyStore;
    private final LocalKeyStore localKeyStore;
    private final ConfigurationLoader configurationLoader;
    private final Timer timer;

    @Inject
    public KeyRotatorImpl(final ConfigurationLoader configurationLoader,
                          final PublicKeyStore dbPublicKeyStore,
                          final LocalKeyStore localKeyStore) {

        this.configurationLoader = configurationLoader;
        this.localKeyStore = localKeyStore;
        this.databasePublicKeyStore = dbPublicKeyStore;

        timer = new Timer(getClass().getName());
        runRotationTask();
        logger.info(String.format(DefaultLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    @Override
    public void runRotationTask() {
        final Properties appProperties = configurationLoader.getAppProperties();
        final String nodeName = appProperties.getProperty(APP_ENUM.APP_NODE_NAME.key);
        final int rsaTTL = Integer.parseInt(appProperties.getProperty(APP_ENUM.APP_RSA_TTL.key));
        final int jwtTTL = Integer.parseInt(appProperties.getProperty(APP_ENUM.APP_JWT_TTL.key));
        final long period = rsaTTL * 1000;
        timer.schedule(new KeyRotationTask(localKeyStore, databasePublicKeyStore, rsaTTL, jwtTTL, nodeName), 0, period);
    }
}
