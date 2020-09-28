package cornerstone.webapp.rest.endpoints.pubkeys;

import cornerstone.webapp.rest.error_responses.SingleErrorResponse;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStore;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreException;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@Singleton
@Path("/pubkeys")
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
            final SingleErrorResponse singleErrorResponse = new SingleErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), illegalArgumentException.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(singleErrorResponse).build();
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
                logger.error(String.format("An error occurred during local caching a public key, exception class: '%s', exception message: '%s'",
                        e.getClass().getCanonicalName(), e.getMessage())
                );
            }
        }

        // Send response
        if (base64Key == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new SingleErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), "No such key."))
                    .build();
        } else {
            return Response.status(Response.Status.OK).entity(base64Key).build();
        }
    }

    @GET
    @Path("uuid/live")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLivePublicKeys() {
        try {
            return Response.status(Response.Status.OK).entity(publicKeyStore.getLiveKeyUUIDs()).build();

        } catch (final PublicKeyStoreException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("uuid/expired")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExpiredKeys() {
        try {
            final List<UUID> uuidList = publicKeyStore.getExpiredKeyUUIDs();
            if (uuidList.size() > 0) {
                return Response.status(Response.Status.OK).entity(uuidList).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).build();
            }

        } catch (final PublicKeyStoreException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
