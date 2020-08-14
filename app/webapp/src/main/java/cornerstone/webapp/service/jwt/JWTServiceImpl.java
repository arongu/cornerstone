package cornerstone.webapp.service.jwt;

import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.service.rsa.store.local.LiveKeys;
import cornerstone.webapp.service.rsa.store.local.LocalKeyStore;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class JWTServiceImpl implements JWTService {
    private static final Logger logger = LoggerFactory.getLogger(JWTServiceImpl.class);

    private final ConfigLoader  configLoader;
    private final LocalKeyStore localKeyStore;

    @Inject
    public JWTServiceImpl(final ConfigLoader configLoader, final LocalKeyStore localKeyStore) {
        this.configLoader  = configLoader;
        this.localKeyStore = localKeyStore;
        logger.info(String.format(DefaultLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    @Override
    public String createJws(String email) {
        final LiveKeys liveKeys       = localKeyStore.getLiveKeys();
        final Properties properties   = configLoader.getAppProperties();
        final long jwtTTL             = Long.parseLong(properties.getProperty(APP_ENUM.APP_JWT_TTL.key));
        final long now                = Instant.now().getEpochSecond();

        return Jwts.builder()
                .setIssuer     (properties.getProperty(APP_ENUM.APP_NODE_NAME.key))
                .setSubject    (email)
                .setIssuedAt   (Date.from(Instant.ofEpochSecond(now)))
                .setExpiration (Date.from(Instant.ofEpochSecond(now + jwtTTL)))
                .setId         (liveKeys.uuid.toString())
                .signWith      (liveKeys.privateKey)
                .compact();
    }

    @Override
    public String createJws(final String email, final Map<String,Object> claims) {
        final LiveKeys liveKeys     = localKeyStore.getLiveKeys();
        final Properties properties = configLoader.getAppProperties();
        final long jwtTTL           = Long.parseLong(properties.getProperty(APP_ENUM.APP_JWT_TTL.key));
        final long now              = Instant.now().getEpochSecond();

        return Jwts.builder()
                .setIssuer     (properties.getProperty(APP_ENUM.APP_NODE_NAME.key))
                .setSubject    (email)
                .setClaims     (claims)
                .setIssuedAt   (Date.from(Instant.ofEpochSecond(now)))
                .setExpiration (Date.from(Instant.ofEpochSecond(now + jwtTTL)))
                .setId         (liveKeys.uuid.toString())
                .signWith      (liveKeys.privateKey)
                .compact();
    }
}
