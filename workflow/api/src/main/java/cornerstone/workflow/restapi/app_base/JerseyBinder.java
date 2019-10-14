package cornerstone.workflow.restapi.app_base;

import cornerstone.workflow.restapi.config.ConfigurationProvider;
import cornerstone.workflow.restapi.datasource.UserDB;
import cornerstone.workflow.restapi.datasource.MainDB;
import cornerstone.workflow.restapi.service.admin.AccountManager;
import cornerstone.workflow.restapi.service.admin.AccountManagerImpl;
import cornerstone.workflow.restapi.service.login.LoginManager;
import cornerstone.workflow.restapi.service.login.LoginManagerImpl;
import cornerstone.workflow.restapi.rest.endpoint.brand.BrandService;
import cornerstone.workflow.restapi.rest.endpoint.brand.BrandServiceImpl;
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
