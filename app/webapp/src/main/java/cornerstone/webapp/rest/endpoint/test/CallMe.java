package cornerstone.webapp.rest.endpoint.test;

import cornerstone.webapp.services.rsa.store.local.LocalKeyStore;
import org.glassfish.hk2.api.Immediate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

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
