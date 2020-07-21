package cornerstone.webapp.jersey;

import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.datasources.UsersDB;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.services.account.administration.AccountAdministration;
import cornerstone.webapp.services.account.administration.AccountAdministrationInterface;
import cornerstone.webapp.services.jwt.AuthorizationService;
import cornerstone.webapp.services.jwt.AuthorizationServiceInterface;
import cornerstone.webapp.services.rsakey.rotation.KeyRotator;
import cornerstone.webapp.services.rsakey.rotation.KeyRotatorInterface;
import cornerstone.webapp.services.rsakey.store.remote.DBPublicKeyStore;
import cornerstone.webapp.services.rsakey.store.remote.DBPublicKeyStoreInterface;
import cornerstone.webapp.services.rsakey.store.local.LocalKeyStore;
import cornerstone.webapp.services.rsakey.store.local.LocalKeyStoreInterface;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import javax.inject.Singleton;
import java.io.IOException;

public class JerseyBinder extends AbstractBinder {
    @Override
    protected void configure() {
        // TODO make function to get environment variables
        // if missing use the default values log them
        // configuration loader should not have fall back parts
        // remove them
        // make TCs green again
        try {
            // configuration
            final ConfigurationLoader configurationLoader = new ConfigurationLoader();
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

        } catch (final IOException e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
