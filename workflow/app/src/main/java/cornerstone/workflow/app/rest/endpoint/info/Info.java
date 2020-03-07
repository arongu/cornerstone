package cornerstone.workflow.app.rest.endpoint.info;

import cornerstone.workflow.app.configuration.ConfigReader;
import cornerstone.workflow.app.rest.security.Secured;

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
    private ConfigReader configReader;

    @Inject
    public Info(ConfigReader configReader) {
        this.configReader = configReader;
    }

    // TODO
    @Secured
    @GET
    public Properties getProperties() {
        return configReader.getAllProperties();
    }
}
