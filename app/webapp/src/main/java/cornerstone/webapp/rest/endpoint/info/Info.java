package cornerstone.webapp.rest.endpoint.info;

import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.rest.security.Secured;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Properties;

@Path("/")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class Info {
    private final ConfigurationLoader configurationLoader;

    @Inject
    public Info(final ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }

    // TODO
    @Secured
    @GET
    public Properties getProperties() {
        return configurationLoader.getAllProperties();
    }
}
