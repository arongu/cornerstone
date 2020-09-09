package cornerstone.webapp.rest.endpoints.pubkey;

import cornerstone.webapp.services.rsa.store.db.PublicKeyStore;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreException;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStore;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.base64.Base64Encoder;

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


/*
TODO return response
 */
@Singleton
@Path("/pubkeys")
public class PublicKeyRestService {
    private final LocalKeyStore localKeyStore;
    private final PublicKeyStore publicKeyStore;

    @Inject
    public PublicKeyRestService(final LocalKeyStore localKeyStore, final PublicKeyStore publicKeyStore){
        this.localKeyStore = localKeyStore;
        this.publicKeyStore = publicKeyStore;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("uuid/{uuid}")
    public Response publicKey(@PathParam("uuid") String uuidString) {
        final UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (final IllegalArgumentException illegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST).entity(uuidString).build();
        }

        try {
            final PublicKey publicKey = localKeyStore.getPublicKey(uuid);
            final String b64key = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            return Response.status(Response.Status.OK).entity(b64key).build();

        } catch (final NoSuchElementException nse_a) {
            try {
                final String base64Key = publicKeyStore.getKey(uuid).getBase64Key();
                localKeyStore.addPublicKey(uuid, base64Key);
                return Response.status(Response.Status.OK).entity(base64Key).build();

            } catch (final NoSuchElementException nse_b) {
                return Response.status(Response.Status.NOT_FOUND).entity(uuidString).build();

            } catch (final InvalidKeySpecException | NoSuchAlgorithmException | PublicKeyStoreException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

            }
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
    public Response getExpiredKeys() throws PublicKeyStoreException {
        try {
            return Response.status(Response.Status.OK).entity(publicKeyStore.getExpiredKeyUUIDs()).build();

        } catch (final PublicKeyStoreException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
