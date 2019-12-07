package cornerstone.workflow.app.rest.info;

import cornerstone.workflow.app.configuration.ConfigurationProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Properties;

@Path("/info")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class Info {
    private ConfigurationProvider configurationProvider;

    @Inject
    public Info(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    @GET
    public Properties getProperties() {
        return configurationProvider.getProperties();
    }
}
