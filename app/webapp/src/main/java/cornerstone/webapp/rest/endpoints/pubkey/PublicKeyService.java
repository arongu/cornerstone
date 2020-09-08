package cornerstone.webapp.rest.endpoints.pubkey;

import cornerstone.webapp.services.rsa.common.PublicKeyData;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStore;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreException;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStore;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Singleton
@Path("/pubkeys")
public class PublicKeyService {
    private final LocalKeyStore localKeyStore;
    private final PublicKeyStore publicKeyStore;

    @Inject
    public PublicKeyService(final LocalKeyStore localKeyStore, final PublicKeyStore publicKeyStore){
        this.localKeyStore = localKeyStore;
        this.publicKeyStore = publicKeyStore;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/uuid/{uuid}")
    public String publicKey(@PathParam("uuid") String uuid) throws PublicKeyStoreException, NoSuchElementException {
        try {
            return Base64.getEncoder().encodeToString(localKeyStore.getPublicKey(UUID.fromString(uuid)).getEncoded());
        } catch (final NoSuchElementException ignored){

        }

        return publicKeyStore.getKey(UUID.fromString(uuid)).getBase64Key();
    }

    @GET
    @Path("/live")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PublicKeyData> getLivePublicKeys() throws PublicKeyStoreException {
        try {
            return publicKeyStore.getLiveKeys();
        } catch (NoSuchElementException e){
            return null;
        }
    }

    @GET
    @Path("/expired")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UUID> getExpiredKeys() throws PublicKeyStoreException {
        return publicKeyStore.getExpiredKeyUUIDs();
    }
}
