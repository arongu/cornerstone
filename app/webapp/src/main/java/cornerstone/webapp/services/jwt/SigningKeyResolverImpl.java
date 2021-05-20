package cornerstone.webapp.services.jwt;

import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.security.Key;
import java.util.NoSuchElementException;
import java.util.UUID;

public class SigningKeyResolverImpl implements SigningKeyResolver {
    private static final Logger logger = LoggerFactory.getLogger(SigningKeyResolverImpl.class);
    private final LocalKeyStore localKeyStore;

    @Inject
    public SigningKeyResolverImpl(final LocalKeyStore localKeyStore) {
        this.localKeyStore = localKeyStore;
    }

    private Key getKey(final JwsHeader<?> jwsHeader) throws NoSuchElementException {
        final UUID keyId = UUID.fromString(jwsHeader.getKeyId());
        return localKeyStore.getPublicKey(keyId);
    }

    @Override
    public Key resolveSigningKey(final JwsHeader jwsHeader, final Claims claims) throws NoSuchElementException {
        return getKey(jwsHeader);
    }

    @Override
    public Key resolveSigningKey(final JwsHeader jwsHeader, final String s) throws NoSuchElementException {
        return getKey(jwsHeader);
    }
}
