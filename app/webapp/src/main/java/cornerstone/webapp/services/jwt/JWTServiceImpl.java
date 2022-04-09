package cornerstone.webapp.services.jwt;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.common.logmsg.CommonLogMessages;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.SigningKeys;
import cornerstone.webapp.services.keys.stores.local.SigningKeysException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/*
    NOTE setClaims first!!!
    https://github.com/jwtk/jjwt/issues/179

    The setClaims method sets (or resets) the entire payload for the JWT. It replaces any previously set claims with the map passed in.
    If you called setClaims first and then called setSubject, your example would work as you intended.
    You're getting a null pointer because there is no sub claim set in the resultant JWT.
    I'm going to close this issue as the library is working as intended.
*/

/**
 * Provides JWS(signed jwt tokens).
 */

public class JWTServiceImpl implements JWTService {
    private static final Logger logger = LoggerFactory.getLogger(JWTServiceImpl.class);

    private final ConfigLoader  configLoader;
    private final LocalKeyStore localKeyStore;

    @Inject
    public JWTServiceImpl(final ConfigLoader configLoader, final LocalKeyStore localKeyStore) {
        this.configLoader  = configLoader;
        this.localKeyStore = localKeyStore;
        logger.info(String.format(CommonLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    /**
     * Creates JWS for the subject.
     * @param subject Subject of the JWS.
     */
    @Override
    public String createJws(final String subject) throws SigningKeysException {
        return createJws(subject, null);
    }

    /**
     * Creates JWS for the subject with the given Map as claims.
     * @param subject Subject of the JWS.
     * @param claimsMap A map which will be added to the JWS as claims.
     * @return JWS as string.
     * @throws SigningKeysException Throws it if the JWT cannot be signed, with the private key.
     */
    @Override
    public String createJws(final String subject, final Map<String,Object> claimsMap) throws SigningKeysException {
        final SigningKeys signingKeySetup     = localKeyStore.getSigningKeys();
        final Properties properties           = configLoader.getAppProperties();
        final long jwtTTL                     = Long.parseLong(properties.getProperty(APP_ENUM.APP_JWT_TTL.key));
        final long now                        = Instant.now().getEpochSecond();

        final Map<String, Object> claims = claimsMap != null ? new HashMap<>(claimsMap) : new HashMap<>();
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setHeaderParam("kid", signingKeySetup.uuid.toString())
                .setClaims     (claims)
                .setIssuer     (properties.getProperty(APP_ENUM.APP_NODE_NAME.key))
                .setSubject    (subject)
                .setIssuedAt   (Date.from(Instant.ofEpochSecond(now)))
                .setExpiration (Date.from(Instant.ofEpochSecond(now + jwtTTL)))
                .signWith      (signingKeySetup.privateKey)
                .compact();
    }
}
