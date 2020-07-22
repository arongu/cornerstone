package cornerstone.webapp.jersey;

import cornerstone.webapp.configuration.ConfigurationDefaults;
import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.datasources.UsersDB;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.services.account.administration.AccountAdministration;
import cornerstone.webapp.services.account.administration.AccountAdministrationInterface;
import cornerstone.webapp.services.jwt.AuthorizationService;
import cornerstone.webapp.services.jwt.AuthorizationServiceInterface;
import cornerstone.webapp.services.rsakey.rotation.KeyRotator;
import cornerstone.webapp.services.rsakey.rotation.KeyRotatorInterface;
import cornerstone.webapp.services.rsakey.store.local.LocalKeyStore;
import cornerstone.webapp.services.rsakey.store.local.LocalKeyStoreInterface;
import cornerstone.webapp.services.rsakey.store.remote.DBPublicKeyStore;
import cornerstone.webapp.services.rsakey.store.remote.DBPublicKeyStoreInterface;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;

public class JerseyBinder extends AbstractBinder {
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
            logger.error(LOG_MESSAGE_PROPERTY_NOT_SET, ConfigurationDefaults.SYSTEM_PROPERTY_KEY_FILE);
            logger.info(LOG_MESSAGE_PROPERTY_FALL_BACK_TO_DEFAULT_VALUE, ConfigurationDefaults.SYSTEM_PROPERTY_KEY_FILE, keyFile);
        }
    }

    private void setConfFileFromEnv() {
        confFile = System.getProperty(ConfigurationDefaults.SYSTEM_PROPERTY_CONF_FILE);
        if (null != confFile) {
            logger.info(LOG_MESSAGE_PROPERTY_SET, ConfigurationDefaults.SYSTEM_PROPERTY_CONF_FILE, confFile);
        } else {
            confFile = ConfigurationDefaults.DEFAULT_CONF_FILE;
            logger.error(LOG_MESSAGE_PROPERTY_NOT_SET, ConfigurationDefaults.SYSTEM_PROPERTY_CONF_FILE);
            logger.info(LOG_MESSAGE_PROPERTY_FALL_BACK_TO_DEFAULT_VALUE, ConfigurationDefaults.SYSTEM_PROPERTY_CONF_FILE, confFile);
        }
    }

    @Override
    protected void configure() {
        try {
            // configuration
            final ConfigurationLoader configurationLoader = new ConfigurationLoader(keyFile, confFile);
            bind(configurationLoader).to(ConfigurationLoader.class).in(Singleton.class);

            // data sources - depends on configuration
            bind(UsersDB.class).in(Singleton.class);
            bind(WorkDB.class).in(Singleton.class);

            // account services - depends on UsersDB
            bind(AccountAdministration.class).to(AccountAdministrationInterface.class).in(Singleton.class);
            bind(AuthorizationService.class).to(AuthorizationServiceInterface.class).in(Singleton.class);

            // key stores, DBKeystore depends on WorkDB
            bind(LocalKeyStore.class).to(LocalKeyStoreInterface.class).in(Singleton.class);
            bind(DBPublicKeyStore.class).to(DBPublicKeyStoreInterface.class).in(Singleton.class);

            // rotation - depends on local and db keystore
            bind(KeyRotator.class).to(KeyRotatorInterface.class).in(Singleton.class);

        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
