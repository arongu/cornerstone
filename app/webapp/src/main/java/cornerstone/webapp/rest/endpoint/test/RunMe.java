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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Secured
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

    @Path("/active")
    @GET
    public List<PublicKeyData> active() throws DbPublicKeyStoreException {
        try {
            return dbPublicKeyStore.getActivePublicKeys();
        } catch (NoSuchElementException e){
            return null;
        }
    }

    @Path("/exp")
    @GET
    public List<UUID> exp() throws DbPublicKeyStoreException {
        try {
            return dbPublicKeyStore.getExpiredKeyUUIDs();
        } catch (NoSuchElementException e) {
            return new LinkedList<>();
        }
    }

    @Path("/exp")
    @DELETE
    public int removeExpiredKeys() throws DbPublicKeyStoreException {
        return dbPublicKeyStore.deleteExpiredPublicKeys();
    }
}
