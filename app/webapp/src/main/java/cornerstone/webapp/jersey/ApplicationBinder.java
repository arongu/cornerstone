package cornerstone.webapp.jersey;

import cornerstone.webapp.configuration.ConfigDefaults;
import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.datasources.AccountsDB;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.services.accounts.management.AccountManager;
import cornerstone.webapp.services.accounts.management.AccountManagerImpl;
import cornerstone.webapp.services.jwt.JWTService;
import cornerstone.webapp.services.jwt.JWTServiceImpl;
import cornerstone.webapp.services.jwt.SigningKeyResolverImpl;
import cornerstone.webapp.services.keys.rotation.KeyRotator;
import cornerstone.webapp.services.keys.rotation.KeyRotatorImpl;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStore;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStoreImpl;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStoreImpl;
import cornerstone.webapp.services.keys.stores.manager.KeyManager;
import cornerstone.webapp.services.keys.stores.manager.KeyManagerImpl;
import io.jsonwebtoken.SigningKeyResolver;
import org.glassfish.hk2.api.Immediate;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;

/**
 * ApplicationBinder is an extension of AbstractBinder.
 * Responsible for bootstrapping the web application:
 *  - loads the configuration
 *  - creates all the singleton classes aka services used by the application
 */
public class ApplicationBinder extends AbstractBinder {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    private static final String LOG_MESSAGE_PROPERTY_SET                        = "[ System.getProperty ][ '{}' ] = '{}'";
    private static final String LOG_MESSAGE_PROPERTY_NOT_SET                    = "[ System.getProperty ][ '{}' ] is not set!";
    private static final String LOG_MESSAGE_PROPERTY_FALL_BACK_TO_DEFAULT_VALUE = "Fall back to default value for [ '{}' ] = '{}'";

    private String keyFile;
    private String confFile;

    /**
     * Sets the keyFile path from the KEY_FILE environment variable if possible,
     * otherwise falls back to the default.
     */
    private void setKeyFileFromEnv() {
        keyFile = System.getProperty(ConfigDefaults.SYSTEM_PROPERTY_KEY_FILE);
        if (null != keyFile) {
            logger.info(LOG_MESSAGE_PROPERTY_SET, ConfigDefaults.SYSTEM_PROPERTY_KEY_FILE, keyFile);

        } else {
            keyFile = ConfigDefaults.DEFAULT_KEY_FILE;
            logger.info(LOG_MESSAGE_PROPERTY_NOT_SET, ConfigDefaults.SYSTEM_PROPERTY_KEY_FILE);
            logger.info(LOG_MESSAGE_PROPERTY_FALL_BACK_TO_DEFAULT_VALUE, ConfigDefaults.SYSTEM_PROPERTY_KEY_FILE, keyFile);
        }
    }

    /**
     * Sets the confFile path from the CONF_FILE environment variable if possible,
     * otherwise falls back to the default.
     */
    private void setConfFileFromEnv() {
        confFile = System.getProperty(ConfigDefaults.SYSTEM_PROPERTY_CONF_FILE);
        if (null != confFile) {
            logger.info(LOG_MESSAGE_PROPERTY_SET, ConfigDefaults.SYSTEM_PROPERTY_CONF_FILE, confFile);

        } else {
            confFile = ConfigDefaults.DEFAULT_CONF_FILE;
            logger.info(LOG_MESSAGE_PROPERTY_NOT_SET, ConfigDefaults.SYSTEM_PROPERTY_CONF_FILE);
            logger.info(LOG_MESSAGE_PROPERTY_FALL_BACK_TO_DEFAULT_VALUE, ConfigDefaults.SYSTEM_PROPERTY_CONF_FILE, confFile);
        }
    }

    /**
     * This where the components of the application gets created and registered.
     */
    @Override
    protected void configure() {
        setConfFileFromEnv();
        setKeyFileFromEnv();

        try {
            // REGISTER ALL NON        @Path ANNOTATED CLASSES HERE !!!
            // DO NOT RELY ON          @Singleton or @Immediate !!!
            // THOSE ONLY WORK WELL ON @Path ANNOTATED CLASSES !!!

            // create an instance and register it as singleton
            bind(new ConfigLoader(keyFile, confFile)).to(ConfigLoader.class).in(Singleton.class);

            // register data sources
            bindAsContract(AccountsDB.class).in(Singleton.class);
            bindAsContract (WorkDB.class).in(Singleton.class);

            // implementation -> interface bindings
            bind     (AccountManagerImpl.class).to     (AccountManager.class).in(Singleton.class);
            bind         (JWTServiceImpl.class).to         (JWTService.class).in(Singleton.class);
            bind      (LocalKeyStoreImpl.class).to      (LocalKeyStore.class).in(Singleton.class);
            bind   (DatabaseKeyStoreImpl.class).to   (DatabaseKeyStore.class).in(Singleton.class);
            bind         (KeyRotatorImpl.class).to         (KeyRotator.class).in(Immediate.class);
            bind         (KeyManagerImpl.class).to         (KeyManager.class).in(Singleton.class);
            bind (SigningKeyResolverImpl.class).to (SigningKeyResolver.class).in(Singleton.class);

        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
