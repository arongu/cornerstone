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
    public String publicKey(@PathParam("uuid") String uuidString) {
        final UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);

        } catch (final IllegalArgumentException illegalArgumentException) {
            return "asd";
        }


        PublicKey publicKey = null;
        try {
            publicKey = localKeyStore.getPublicKey(uuid);

        } catch (final NoSuchElementException noSuchElementException) {
            try {
                final String base64Key = publicKeyStore.getKey(uuid).getBase64Key();
                localKeyStore.addPublicKey(uuid, base64Key);

            } catch (final NoSuchElementException noSuchElementException1) {

            } catch (final PublicKeyStoreException publicKeyStoreException) {
                //
                return null;
            } catch (final NoSuchAlgorithmException e) {
                // log ?
                return null;
            } catch (final InvalidKeySpecException e) {
                // e.printStackTrace();
                return null;
            }
        }

        if (publicKey != null) {
            final String s = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            return Response.status(Response.Status.OK)
                    .entity(s)
                    .build();
        } else {
            return null;
        }
    }

    @GET
    @Path("uuid/live")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UUID> getLivePublicKeys() throws PublicKeyStoreException {
        return publicKeyStore.getLiveKeyUUIDs();
    }

    @GET
    @Path("uuid/expired")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UUID> getExpiredKeys() throws PublicKeyStoreException {
        return publicKeyStore.getExpiredKeyUUIDs();
    }
}
