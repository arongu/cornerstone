package cornerstone.webapp.services.jwt;

import cornerstone.webapp.logmsg.CommonLogMessages;
import cornerstone.webapp.services.keys.stores.logging.KeyRelatedMessageElements;
import cornerstone.webapp.services.keys.stores.manager.KeyManager;
import cornerstone.webapp.services.keys.stores.manager.KeyManagerException;
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

    private final KeyManager keyManager;

    @Inject
    public SigningKeyResolverImpl(final KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    private Key getKey(final JwsHeader<?> jwsHeader) throws NoSuchElementException, KeyManagerException {
        final UUID   uuid = UUID.fromString(jwsHeader.getKeyId());
        final String m    = CommonLogMessages.GENRE_SECURITY + KeyRelatedMessageElements.PREFIX_RESOLVER + String.format(KeyRelatedMessageElements.RESOLVING, uuid);
        logger.info(m);

        return keyManager.getPublicKey(uuid);
    }

    @Override
    public Key resolveSigningKey(final JwsHeader jwsHeader, final Claims claims) throws NoSuchElementException {
        try {
            return getKey(jwsHeader);

        } catch (final KeyManagerException e) {
            final String em = CommonLogMessages.GENRE_SECURITY + KeyRelatedMessageElements.PREFIX_RESOLVER + KeyRelatedMessageElements.DATABASE_KEYSTORE_ERROR;
            logger.error(em);
            throw new NoSuchElementException();
        }
    }

    @Override
    public Key resolveSigningKey(final JwsHeader jwsHeader, final String s) throws NoSuchElementException {
        try {
            return getKey(jwsHeader);
        } catch (final KeyManagerException e) {
            throw new NoSuchElementException();
        }
    }
}
