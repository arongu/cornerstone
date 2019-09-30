package com.aron.jcore.app_base;

import com.aron.jcore.config.ConfigurationProvider;
import com.aron.jcore.datasource.DBAdmin;
import com.aron.jcore.datasource.DBMain;
import com.aron.jcore.service.admin.AccountManager;
import com.aron.jcore.service.admin.AccountManagerImpl;
import com.aron.jcore.service.login.LoginManager;
import com.aron.jcore.service.login.LoginManagerImpl;
import com.aron.jcore.rest.endpoint.brand.BrandService;
import com.aron.jcore.rest.endpoint.brand.BrandServiceImpl;
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
            bind(new DBMain(configurationProvider)).to(DBMain.class).in(Singleton.class);
            bind(new DBAdmin(configurationProvider)).to(DBAdmin.class).in(Singleton.class);

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
