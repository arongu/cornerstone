package cornerstone.webapp.rest.endpoint.test;

import cornerstone.webapp.rest.security.Secured;
import cornerstone.webapp.services.rsa.common.PublicKeyData;
import cornerstone.webapp.services.rsa.rotation.KeyRotatorInterface;
import cornerstone.webapp.services.rsa.store.db.DbPublicKeyStoreException;
import cornerstone.webapp.services.rsa.store.db.DbPublicKeyStoreInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class RunMe {
    private static final Logger logger = LoggerFactory.getLogger(RunMe.class);
    private final DbPublicKeyStoreInterface dbPublicKeyStore;

    @Inject
    public RunMe(final KeyRotatorInterface keyRotator, final DbPublicKeyStoreInterface dbPublicKeyStore){
        this.dbPublicKeyStore = dbPublicKeyStore;
    }

    @GET
    public List<PublicKeyData> w() throws DbPublicKeyStoreException {
        return dbPublicKeyStore.getActivePublicKeys();
    }

//    @Secured
//    @GET
//    public List<PublicKeyData> get() {
//        logger.info("here we go....");
//        return dbPublicKeyStore.getActivePublicKeys();
//    }
}
