package com.aron.jcore.rest.endpoint.x;

import com.aron.jcore.config.ConfigurationProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.Properties;

@Path("/x")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class X {
    private ConfigurationProvider configurationProvider;

    @Inject
    public X(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    @GET
    public Properties gg() throws SQLException {
        return configurationProvider.getProperties();
    }
}
