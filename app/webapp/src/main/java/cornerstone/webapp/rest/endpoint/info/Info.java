package cornerstone.webapp.rest.endpoint.info;

import cornerstone.webapp.rest.security.Secured;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;

@Path("/")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class Info {

    // TODO
    @Secured
    @GET
    public LocalDateTime getProperties() {
        return LocalDateTime.now();
    }
}
