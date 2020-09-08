package cornerstone.webapp.services.jwt;

import cornerstone.webapp.common.CommonLogMessages;
import cornerstone.webapp.config.ConfigLoader;
import cornerstone.webapp.config.enums.APP_ENUM;
import cornerstone.webapp.services.rsa.store.local.LiveKeys;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStore;
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

    @Override
    public String createJws(final String subject) {
        final LiveKeys liveKeys     = localKeyStore.getLiveKeys();
        final Properties properties = configLoader.getAppProperties();
        final long jwtTTL           = Long.parseLong(properties.getProperty(APP_ENUM.APP_JWT_TTL.key));
        final long now              = Instant.now().getEpochSecond();

        final Map<String,Object> claimsMap  = new HashMap<>();
        claimsMap.put("keyId", liveKeys.uuid);

        return Jwts.builder()
                .setClaims     (claimsMap)
                .setIssuer     (properties.getProperty(APP_ENUM.APP_NODE_NAME.key))
                .setSubject    (subject)
                .setIssuedAt   (Date.from(Instant.ofEpochSecond(now)))
                .setExpiration (Date.from(Instant.ofEpochSecond(now + jwtTTL)))
                .signWith      (liveKeys.privateKey)
                .compact();
    }

    @Override
    public String createJws(final String subject, final Map<String,Object> claimsMap) {
        final LiveKeys liveKeys     = localKeyStore.getLiveKeys();
        final Properties properties = configLoader.getAppProperties();
        final long jwtTTL           = Long.parseLong(properties.getProperty(APP_ENUM.APP_JWT_TTL.key));
        final long now              = Instant.now().getEpochSecond();

        if (claimsMap != null) {
            final Map<String, Object> m = new HashMap<>(claimsMap);
            m.put("keyId", liveKeys.uuid);

            return Jwts.builder()
                    .setClaims     (m)
                    .setIssuer     (properties.getProperty(APP_ENUM.APP_NODE_NAME.key))
                    .setSubject    (subject)
                    .setIssuedAt   (Date.from(Instant.ofEpochSecond(now)))
                    .setExpiration (Date.from(Instant.ofEpochSecond(now + jwtTTL)))
                    .signWith      (liveKeys.privateKey)
                    .compact();
        } else {
            throw new NullPointerException("claimsMap must not be null!");
        }
    }
}
