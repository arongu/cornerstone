package cornerstone.webapp.rest.endpoint.test;

import cornerstone.webapp.rest.security.Secured;
import cornerstone.webapp.services.rsakey.rotation.KeyRotatorInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;

@Path("/")
@Singleton
//@Produces(MediaType.APPLICATION_JSON)
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    @Inject
    public Test(final KeyRotatorInterface keyRotator){
        logger.info("aaaaaaaaaaaaaaaaaaaaaaaaaa               xxxxxxxxxxxxxxxxxxxxxxx");
    }

    // TODO
    @Secured
    @GET
    public String getProperties() {
        logger.info("mi vna mi<");
        //return LocalDateTime.now();
        return "boci";
    }
}
