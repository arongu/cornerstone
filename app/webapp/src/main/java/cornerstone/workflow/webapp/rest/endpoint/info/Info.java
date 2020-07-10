package cornerstone.workflow.webapp.rest.endpoint.info;

import cornerstone.workflow.webapp.configuration.ConfigurationLoader;
import cornerstone.workflow.webapp.rest.security.Secured;

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
    private ConfigurationLoader configurationLoader;

    @Inject
    public Info(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }

    // TODO
    @Secured
    @GET
    public Properties getProperties() {
        return configurationLoader.getAllProperties();
    }
}
