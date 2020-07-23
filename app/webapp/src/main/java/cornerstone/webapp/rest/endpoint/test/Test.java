package cornerstone.webapp.rest.endpoint.test;

import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.rest.security.Secured;
import cornerstone.webapp.services.rsa.rotation.KeyRotatorInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    @Inject
    public Test(final KeyRotatorInterface keyRotator){
        logger.info(String.format(DefaultLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    @Secured
    @GET
    public String get() {
        return "boci";
    }
}
