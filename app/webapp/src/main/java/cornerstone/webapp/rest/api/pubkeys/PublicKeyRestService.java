package cornerstone.webapp.rest.api.pubkeys;

import cornerstone.webapp.rest.error_responses.ErrorResponse;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStore;
import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStoreException;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.manager.KeyManager;
import cornerstone.webapp.services.keys.stores.manager.KeyManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Provides public keys based on UUID (first checks the local cache, then tries to retrieve it from the DB)
 * Provides list of live and expired public key UUIDs as well.
 */
@Singleton
@Path("/pubkeys")
@PermitAll
public class PublicKeyRestService {
    private static final Logger logger = LoggerFactory.getLogger(PublicKeyRestService.class);
    private final KeyManager keyManager;

    @Inject
    public PublicKeyRestService(final KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    @GET
    @Path("uuid/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublicKey(@PathParam("uuid") String uuidString) {
        // Send bad request when uuid is malformed
        final UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);

        } catch (final IllegalArgumentException e) {
            final ErrorResponse er = new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(er).build();
        }

        try {
            final PublicKey publicKey       = keyManager.getPublicKey(uuid);
            final String    base64PublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            return Response.status(Response.Status.OK)
                           .entity(new PublicKeyDTO(base64PublicKey)).build();

        } catch (final NoSuchElementException e) {
            final ErrorResponse errorResponse = new ErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), "No such key.");

            return Response.status(Response.Status.NOT_FOUND)
                           .entity(errorResponse).build();

        } catch (final KeyManagerException e) {
            final String msg = "An error occurred during public key retrieval!";
            logger.error(msg);

            final ErrorResponse er = new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(er).build();
        }
    }

    @GET
    @Path("uuid/live")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLiveKeyUUIDs() {
        try {
            final List<UUID> uuids = keyManager.getLivePublicKeyUUIDs();
            return Response.status(Response.Status.OK).entity(uuids).build();

        } catch (final DatabaseKeyStoreException e) {
            final ErrorResponse er = new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "An error occurred during live uuid retrieval.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(er).build();
        }
    }

    @GET
    @Path("uuid/expired")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExpiredKeyUUIDs() {
        try {
            final List<UUID> uuids = keyManager.getExpiredPublicKeyUUIDs();
            return Response.status(Response.Status.OK).entity(uuids).build();

        } catch (final DatabaseKeyStoreException e) {
            final ErrorResponse er = new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "An error occurred during expired uuid retrieval.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(er).build();
        }
    }
}
