package cornerstone.webapp.jersey;

import cornerstone.webapp.configuration.ConfigurationDefaults;
import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.datasources.UsersDB;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.services.account.administration.AccountManager;
import cornerstone.webapp.services.account.administration.AccountManagerImpl;
import cornerstone.webapp.services.jwt.AuthorizationService;
import cornerstone.webapp.services.jwt.AuthorizationServiceImpl;
import cornerstone.webapp.services.rsa.rotation.KeyRotator;
import cornerstone.webapp.services.rsa.rotation.KeyRotatorImpl;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStore;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreImpl;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStore;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStoreImpl;
import org.glassfish.hk2.api.Immediate;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;

public class ApplicationBinder extends AbstractBinder {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);

    private static final String LOG_MESSAGE_PROPERTY_SET                        = "[ System.getProperty ][ '{}' ] = '{}'";
    private static final String LOG_MESSAGE_PROPERTY_NOT_SET                    = "[ System.getProperty ][ '{}' ] is not set!";
    private static final String LOG_MESSAGE_PROPERTY_FALL_BACK_TO_DEFAULT_VALUE = "Fall back to default value for [ '{}' ] = '{}'";

    private String keyFile;
    private String confFile;

    private void setKeyFileFromEnv() {
        keyFile = System.getProperty(ConfigurationDefaults.SYSTEM_PROPERTY_KEY_FILE);
        if (null != keyFile) {
            logger.info(LOG_MESSAGE_PROPERTY_SET, ConfigurationDefaults.SYSTEM_PROPERTY_KEY_FILE, keyFile);

        } else {
            keyFile = ConfigurationDefaults.DEFAULT_KEY_FILE;
            logger.info(LOG_MESSAGE_PROPERTY_NOT_SET, ConfigurationDefaults.SYSTEM_PROPERTY_KEY_FILE);
            logger.info(LOG_MESSAGE_PROPERTY_FALL_BACK_TO_DEFAULT_VALUE, ConfigurationDefaults.SYSTEM_PROPERTY_KEY_FILE, keyFile);
        }
    }

    private void setConfFileFromEnv() {
        confFile = System.getProperty(ConfigurationDefaults.SYSTEM_PROPERTY_CONF_FILE);
        if (null != confFile) {
            logger.info(LOG_MESSAGE_PROPERTY_SET, ConfigurationDefaults.SYSTEM_PROPERTY_CONF_FILE, confFile);

        } else {
            confFile = ConfigurationDefaults.DEFAULT_CONF_FILE;
            logger.info(LOG_MESSAGE_PROPERTY_NOT_SET, ConfigurationDefaults.SYSTEM_PROPERTY_CONF_FILE);
            logger.info(LOG_MESSAGE_PROPERTY_FALL_BACK_TO_DEFAULT_VALUE, ConfigurationDefaults.SYSTEM_PROPERTY_CONF_FILE, confFile);
        }
    }

    @Override
    protected void configure() {
        setConfFileFromEnv();
        setKeyFileFromEnv();

        try {
            // Register all NON @Path annotated classes here !!!
            // DO NOT RELY ON @Singleton or @Immediate - works only well on @Path annotated classes
            bind(new ConfigurationLoader(keyFile, confFile)).to(ConfigurationLoader.class).in(Singleton.class);

            // register data sources
            bindAsContract(UsersDB.class).in(Singleton.class);
            bindAsContract(WorkDB.class).in(Singleton.class);

            // implementation -> interface bindings
            bind(AccountManagerImpl.class).to(AccountManager.class).in(Singleton.class);
            bind(AuthorizationServiceImpl.class).to(AuthorizationService.class).in(Singleton.class);
            bind(LocalKeyStoreImpl.class).to(LocalKeyStore.class).in(Singleton.class);
            bind(KeyRotatorImpl.class).to(KeyRotator.class).in(Immediate.class);
            bind(PublicKeyStoreImpl.class).to(PublicKeyStore.class).in(Singleton.class);

        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
