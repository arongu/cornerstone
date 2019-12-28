package cornerstone.workflow.app.rest.info;

import cornerstone.workflow.app.configuration.ConfigurationProvider;
import cornerstone.workflow.app.rest.rest_security.Secured;

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
    private ConfigurationProvider configurationProvider;

    @Inject
    public Info(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    // TODO
    @Secured
    @GET
    public Properties getProperties() {
        return configurationProvider.getRaw_properties();
    }
}
