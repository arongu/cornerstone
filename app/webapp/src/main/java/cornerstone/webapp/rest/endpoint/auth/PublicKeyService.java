package cornerstone.webapp.rest.endpoint.auth;

import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreException;
import cornerstone.webapp.services.rsa.store.db.PublicKeyStoreInterface;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStoreInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.UUID;

@Singleton
@Path("/auth")
public class PublicKeyService {
    private static final Logger logger = LoggerFactory.getLogger(PublicKeyService.class);

    private final LocalKeyStoreInterface localKeyStore;
    private final PublicKeyStoreInterface publicKeyStore;

    @Inject
    public PublicKeyService(final LocalKeyStoreInterface localKeyStore, final PublicKeyStoreInterface publicKeyStore){
        this.localKeyStore = localKeyStore;
        this.publicKeyStore = publicKeyStore;
    }

    @GET
    @Path("pubkey/{uuid}")
    public String publicKey(@PathParam("uuid") String uuid) throws PublicKeyStoreException, NoSuchElementException {
        logger.info("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        try {
            logger.info("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
            final String b64 = Base64.getEncoder().encodeToString(localKeyStore.getPublicKey(UUID.fromString(uuid)).getEncoded());
            logger.info("b64 " + b64);
            return b64;
        } catch (final NoSuchElementException ignored){}

        logger.info("ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc");
        return publicKeyStore.getKey(UUID.fromString(uuid)).getBase64Key();
    }
}
