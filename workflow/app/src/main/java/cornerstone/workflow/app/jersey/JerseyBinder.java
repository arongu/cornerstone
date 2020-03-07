package cornerstone.workflow.app.jersey;

import cornerstone.workflow.app.configuration.ConfigReader;
import cornerstone.workflow.app.datasource.DataSourceAccountDB;
import cornerstone.workflow.app.datasource.DataSourceDataDB;
import cornerstone.workflow.app.services.account_service.AccountService;
import cornerstone.workflow.app.services.account_service.AccountServiceInterface;
import cornerstone.workflow.app.services.authorization_service.AuthorizationServiceInterface;
import cornerstone.workflow.app.services.authorization_service.AuthorizationService;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import javax.inject.Singleton;
import java.io.IOException;

public class JerseyBinder extends AbstractBinder {
    @Override
    protected void configure() {
        try {
            // Bootstrapping binder, config provider
            final ConfigReader configReader = new ConfigReader();
            configReader.loadConfig();
            bind(configReader).to(ConfigReader.class).in(Singleton.class);

            // DB Pool bindings
            bind(new DataSourceAccountDB(configReader)).to(DataSourceAccountDB.class).in(Singleton.class);
            bind(new DataSourceDataDB(configReader)).to(DataSourceDataDB.class).in(Singleton.class);

            // AccountCrudService, Authentication, Authorization services
            bind(AccountService.class).to(AccountServiceInterface.class).in(Singleton.class);
            bind(AuthorizationService.class).to(AuthorizationServiceInterface.class).in(Singleton.class);

        } catch ( final IOException e ) {
            e.printStackTrace();
        }
    }
}
