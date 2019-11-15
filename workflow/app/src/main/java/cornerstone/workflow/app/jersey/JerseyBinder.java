package cornerstone.workflow.app.jersey;

import cornerstone.workflow.app.configuration.ConfigurationProvider;
import cornerstone.workflow.app.datasource.UserDB;
import cornerstone.workflow.app.datasource.MainDB;
import cornerstone.workflow.app.services.admin.AccountManager;
import cornerstone.workflow.app.services.admin.AccountManagerImpl;
import cornerstone.workflow.app.services.login.LoginManager;
import cornerstone.workflow.app.services.login.LoginManagerImpl;
import cornerstone.workflow.app.rest.endpoint.brand.BrandService;
import cornerstone.workflow.app.rest.endpoint.brand.BrandServiceImpl;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import javax.inject.Singleton;
import java.io.IOException;

public class JerseyBinder extends AbstractBinder {
    @Override
    protected void configure() {
        ConfigurationProvider configurationProvider;
        try {
            // Bootstrapping binder, config provider
            configurationProvider = new ConfigurationProvider();
            bind(configurationProvider).to(ConfigurationProvider.class).in(Singleton.class);

            // DB Pool bindings
            bind(new MainDB(configurationProvider)).to(MainDB.class).in(Singleton.class);
            bind(new UserDB(configurationProvider)).to(UserDB.class).in(Singleton.class);

            // Admin, Login
            bind(AccountManagerImpl.class).to(AccountManager.class).in(Singleton.class);
            bind(LoginManagerImpl.class).to(LoginManager.class).in(Singleton.class);

            // REST services
            bind(BrandServiceImpl.class).to(BrandService.class).in(Singleton.class);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
