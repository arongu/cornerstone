package cornerstone.webapp.jersey;

import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.datasources.UsersDB;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.services.account.administration.AccountAdministration;
import cornerstone.webapp.services.account.administration.AccountAdministrationInterface;
import cornerstone.webapp.services.jwt.AuthorizationService;
import cornerstone.webapp.services.jwt.AuthorizationServiceInterface;
import cornerstone.webapp.services.rsakey.storage.db.DatabasePublicKeyStore;
import cornerstone.webapp.services.rsakey.storage.db.DatabasePublicKeyStoreInterface;
import cornerstone.webapp.services.rsakey.storage.local.LocalKeyStore;
import cornerstone.webapp.services.rsakey.storage.local.LocalKeyStoreInterface;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import javax.inject.Singleton;
import java.io.IOException;

public class JerseyBinder extends AbstractBinder {
    @Override
    protected void configure() {
        try {
            final ConfigurationLoader configurationLoader = new ConfigurationLoader();
            bind(configurationLoader).to(ConfigurationLoader.class).in(Singleton.class);
            bind(UsersDB.class).in(Singleton.class);
            bind(WorkDB.class).in(Singleton.class);
            bind(AccountAdministration.class).to(AccountAdministrationInterface.class).in(Singleton.class);
            bind(AuthorizationService.class).to(AuthorizationServiceInterface.class).in(Singleton.class);
            bind(DatabasePublicKeyStore.class).to(DatabasePublicKeyStoreInterface.class).in(Singleton.class);
            bind(LocalKeyStore.class).to(LocalKeyStoreInterface.class).in(Singleton.class);

        } catch (final IOException e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
