package cornerstone.workflow.restapi.rest.endpoint.admin;

import cornerstone.workflow.restapi.config.ConfigurationProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
    public Properties gg() {
        return configurationProvider.getProperties();
    }
}
