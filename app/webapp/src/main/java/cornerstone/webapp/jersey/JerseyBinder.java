package cornerstone.webapp.jersey;

import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.datasources.UsersDB;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.services.account_service.AccountService;
import cornerstone.webapp.services.account_service.AccountServiceInterface;
import cornerstone.webapp.services.authorization_service.AuthorizationService;
import cornerstone.webapp.services.authorization_service.AuthorizationServiceInterface;
import cornerstone.webapp.services.rsa_key_services.db.PublicKeyStorageService;
import cornerstone.webapp.services.rsa_key_services.db.PublicKeyStorageServiceInterface;
import cornerstone.webapp.services.rsa_key_services.local.KeyService;
import cornerstone.webapp.services.rsa_key_services.local.KeyServiceInterface;
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
            bind(AccountService.class).to(AccountServiceInterface.class).in(Singleton.class);
            bind(AuthorizationService.class).to(AuthorizationServiceInterface.class).in(Singleton.class);
            bind(PublicKeyStorageService.class).to(PublicKeyStorageServiceInterface.class).in(Singleton.class);
            bind(KeyService.class).to(KeyServiceInterface.class).in(Singleton.class);

        } catch (final IOException e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
