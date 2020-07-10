package cornerstone.workflow.webapp.jersey;

import cornerstone.workflow.webapp.configuration.ConfigurationLoader;
import cornerstone.workflow.webapp.configuration.ConfigurationLoaderException;
import cornerstone.workflow.webapp.datasource.DataSourceUsersDB;
import cornerstone.workflow.webapp.datasource.DataSourceWorkDB;
import cornerstone.workflow.webapp.services.account_service.AccountService;
import cornerstone.workflow.webapp.services.account_service.AccountServiceInterface;
import cornerstone.workflow.webapp.services.authorization_service.AuthorizationServiceInterface;
import cornerstone.workflow.webapp.services.authorization_service.AuthorizationService;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import javax.inject.Singleton;
import java.io.IOException;

public class JerseyBinder extends AbstractBinder {
    @Override
    protected void configure() {
        try {
            // Bootstrapping binder, config provider
            final ConfigurationLoader configurationLoader = new ConfigurationLoader();
            configurationLoader.loadAndDecryptConfig();
            bind(configurationLoader).to(ConfigurationLoader.class).in(Singleton.class);

            // DB Pool bindings
            bind(new DataSourceUsersDB(configurationLoader)).to(DataSourceUsersDB.class).in(Singleton.class);
            bind(new DataSourceWorkDB(configurationLoader)).to(DataSourceWorkDB.class).in(Singleton.class);

            // AccountCrudService, Authentication, Authorization services
            bind(AccountService.class).to(AccountServiceInterface.class).in(Singleton.class);
            bind(AuthorizationService.class).to(AuthorizationServiceInterface.class).in(Singleton.class);

        } catch ( final IOException | ConfigurationLoaderException e ) {
            e.printStackTrace();
        }
    }
}
