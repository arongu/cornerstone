package cornerstone.webapp.rest.endpoints.pubkeys;

import cornerstone.webapp.rest.error_responses.ErrorResponse;
import cornerstone.webapp.services.keys.stores.db.PublicKeyStore;
import cornerstone.webapp.services.keys.stores.db.PublicKeyStoreException;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
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

    private final LocalKeyStore localKeyStore;
    private final PublicKeyStore publicKeyStore;

    @Inject
    public PublicKeyRestService(final LocalKeyStore localKeyStore, final PublicKeyStore publicKeyStore){
        this.localKeyStore = localKeyStore;
        this.publicKeyStore = publicKeyStore;
    }

    @GET
    @Path("uuid/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublicKey(@PathParam("uuid") String uuidString) {
        // Send bad request when uuid is malformed
        final UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);

        } catch (final IllegalArgumentException illegalArgumentException) {
            final ErrorResponse er = new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), illegalArgumentException.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(er).build();
        }

        // Try to get key from local keystore
        String base64Key = null;
        try {
            final PublicKey publicKey = localKeyStore.getPublicKey(uuid);
            base64Key = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        } catch (final NoSuchElementException ignored) {}

        // Try to get key from database and cache it locally
        if (base64Key == null) {
            try {
                base64Key = publicKeyStore.getKey(uuid).getBase64Key();
                localKeyStore.addPublicKey(uuid, base64Key);

            } catch (final NoSuchElementException ignored) {

            } catch (final PublicKeyStoreException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                logger.error(String.format("An error occurred during public key retrieval/local caching, exception class: '%s', exception message: '%s'", e.getClass().getCanonicalName(), e.getMessage()));
                final ErrorResponse er = new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "An error occurred during public key retrieval/local caching.");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(er).build();
            }
        }

        // Send response
        if (base64Key != null) {
            return Response.status(Response.Status.OK).entity(new PublicKeyDTO(base64Key)).build();
        } else {
            final ErrorResponse errorResponse = new ErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), "No such key.");
            return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
        }
    }

    @GET
    @Path("uuid/live")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLiveKeyUUIDs() {
        try {
            final List<UUID> uuidList = publicKeyStore.getLiveKeyUUIDs();
            return Response.status(Response.Status.OK).entity(uuidList).build();

        } catch (final PublicKeyStoreException e) {
            final ErrorResponse er = new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "An error occurred during live key retrieval.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(er).build();
        }
    }

    @GET
    @Path("uuid/expired")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExpiredKeyUUIDs() {
        try {
            final List<UUID> uuidList = publicKeyStore.getExpiredKeyUUIDs();
            return Response.status(Response.Status.OK).entity(uuidList).build();

        } catch (final PublicKeyStoreException e) {
            final ErrorResponse er = new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "An error occurred during expired key retrieval.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(er).build();
        }
    }
}
