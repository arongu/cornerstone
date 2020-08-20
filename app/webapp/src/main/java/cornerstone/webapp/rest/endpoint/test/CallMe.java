package cornerstone.webapp.rest.endpoint.test;

import cornerstone.webapp.service.rsa.store.local.LocalKeyStore;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("/test")
public class CallMe {
    private static final Logger logger = LoggerFactory.getLogger(CallMe.class);

    private LocalKeyStore localKeyStore;

    @Inject
    public CallMe(final LocalKeyStore localKeyStore) {
        this.localKeyStore = localKeyStore;
        logger.info("Ctor of " + getClass().getName());
        logger.info("aaaaaaaaaaaaaaxxxxxxxxxxxxxxxxxxxxxxxxx");
        logger.info(localKeyStore.toString());
    }

    @GET
    public String aaa(){
        logger.info("OOO");
        return "aaaaaaaaaaaa";
    }
}
