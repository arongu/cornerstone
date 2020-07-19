package cornerstone.workflow.webapp.jersey;

import cornerstone.workflow.webapp.configuration.ConfigurationLoader;
import cornerstone.workflow.webapp.configuration.ConfigurationLoaderException;
import cornerstone.workflow.webapp.datasources.UsersDB;
import cornerstone.workflow.webapp.datasources.WorkDB;
import cornerstone.workflow.webapp.services.account_service.AccountService;
import cornerstone.workflow.webapp.services.account_service.AccountServiceInterface;
import cornerstone.workflow.webapp.services.authorization_service.AuthorizationService;
import cornerstone.workflow.webapp.services.authorization_service.AuthorizationServiceInterface;
import cornerstone.workflow.webapp.services.rsa_key_services.db.PublicKeyStorageService;
import cornerstone.workflow.webapp.services.rsa_key_services.db.PublicKeyStorageServiceInterface;
import cornerstone.workflow.webapp.services.rsa_key_services.local.KeyService;
import cornerstone.workflow.webapp.services.rsa_key_services.local.KeyServiceInterface;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import javax.inject.Singleton;
import java.io.IOException;

public class JerseyBinder extends AbstractBinder {
    @Override
    protected void configure() {
        try {
            // configuration loader
            final ConfigurationLoader configurationLoader = new ConfigurationLoader();
            configurationLoader.loadAndDecryptConfig();
            bind(configurationLoader).to(ConfigurationLoader.class).in(Singleton.class);

            // data sources
            bind(UsersDB.class).in(Singleton.class);
            bind(WorkDB.class).in(Singleton.class);

            // account service, authorization service
            bind(AccountService.class).to(AccountServiceInterface.class).in(Singleton.class);
            bind(AuthorizationService.class).to(AuthorizationServiceInterface.class).in(Singleton.class);

            // key services
            bind(PublicKeyStorageService.class).to(PublicKeyStorageServiceInterface.class).in(Singleton.class);
            bind(KeyService.class).to(KeyServiceInterface.class).in(Singleton.class);

        } catch (final IOException | ConfigurationLoaderException e) {
            e.printStackTrace();
        }
    }
}
