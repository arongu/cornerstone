package cornerstone.webapp.jersey;

import cornerstone.webapp.config.ConfigDefaults;
import cornerstone.webapp.config.ConfigLoader;
import cornerstone.webapp.datasources.UsersDB;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.service.account.administration.AccountManager;
import cornerstone.webapp.service.account.administration.AccountManagerImpl;
import cornerstone.webapp.service.jwt.JWTService;
import cornerstone.webapp.service.jwt.JWTServiceImpl;
import cornerstone.webapp.service.rsa.rotation.KeyRotator;
import cornerstone.webapp.service.rsa.rotation.KeyRotatorImpl;
import cornerstone.webapp.service.rsa.store.db.PublicKeyStore;
import cornerstone.webapp.service.rsa.store.db.PublicKeyStoreImpl;
import cornerstone.webapp.service.rsa.store.local.LocalKeyStore;
import cornerstone.webapp.service.rsa.store.local.LocalKeyStoreImpl;
import org.glassfish.hk2.api.Immediate;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;

public class ApplicationBinder extends AbstractBinder {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    private static final String LOG_MESSAGE_PROPERTY_SET                        = "[ System.getProperty ][ '{}' ] = '{}'";
    private static final String LOG_MESSAGE_PROPERTY_NOT_SET                    = "[ System.getProperty ][ '{}' ] is not set!";
    private static final String LOG_MESSAGE_PROPERTY_FALL_BACK_TO_DEFAULT_VALUE = "Fall back to default value for [ '{}' ] = '{}'";

    private String keyFile;
    private String confFile;

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

    @Override
    protected void configure() {
        setConfFileFromEnv();
        setKeyFileFromEnv();

        try {
            // REGISTER ALL NON   @Path ANNOTATED CLASSES HERE !!!
            // DO NOT RELY ON     @Singleton or @Immediate !!!
            // THOSE ONLY WORK WELL ON @PATH ANNOTATED CLASSES !!!

            // create an instance and register it as singleton
            bind(new ConfigLoader(keyFile, confFile)).to(ConfigLoader.class).in(Singleton.class);

            // register data sources
            bindAsContract(UsersDB.class).in(Singleton.class);
            bindAsContract(WorkDB.class).in(Singleton.class);

            // implementation -> interface bindings
            bind(AccountManagerImpl.class).to(AccountManager.class).in(Singleton.class);
            bind(JWTServiceImpl.class).to(JWTService.class).in(Singleton.class);
            bind(LocalKeyStoreImpl.class).to(LocalKeyStore.class).in(Singleton.class);
            bind(KeyRotatorImpl.class).to(KeyRotator.class).in(Immediate.class);
            bind(PublicKeyStoreImpl.class).to(PublicKeyStore.class).in(Singleton.class);

        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
