package cornerstone.workflow.app.jersey;

import cornerstone.workflow.app.configuration.ConfigurationProvider;
import cornerstone.workflow.app.datasource.AccountDB;
import cornerstone.workflow.app.datasource.DataDB;
import cornerstone.workflow.app.services.account_service.AccountService;
import cornerstone.workflow.app.services.account_service.AccountServiceImpl;
import cornerstone.workflow.app.services.authorization_service.AuthorizationService;
import cornerstone.workflow.app.services.authorization_service.AuthorizationServiceImpl;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import javax.inject.Singleton;
import java.io.IOException;

public class JerseyBinder extends AbstractBinder {
    @Override
    protected void configure() {
        try {
            // Bootstrapping binder, config provider
            final ConfigurationProvider configurationProvider = new ConfigurationProvider();
            configurationProvider.loadConfig();
            bind(configurationProvider).to(ConfigurationProvider.class).in(Singleton.class);

            // DB Pool bindings
            bind(new AccountDB(configurationProvider)).to(AccountDB.class).in(Singleton.class);
            bind(new DataDB(configurationProvider)).to(DataDB.class).in(Singleton.class);

            // AccountCrudService, Authentication, Authorization services
            bind(AccountServiceImpl.class).to(AccountService.class).in(Singleton.class);
            bind(AuthorizationServiceImpl.class).to(AuthorizationService.class).in(Singleton.class);

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
