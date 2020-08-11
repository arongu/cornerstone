package cornerstone.webapp.service.jwt;

import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.service.rsa.store.local.LiveKeyData;
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

    private final ConfigurationLoader configurationLoader;
    private final LocalKeyStore localKeyStore;

    @Inject
    public JWTServiceImpl(final ConfigurationLoader configurationLoader, final LocalKeyStore localKeyStore){
        this.configurationLoader = configurationLoader;
        this.localKeyStore = localKeyStore;
        logger.info(String.format(DefaultLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    @Override
    public String issueJWT(String email) {
        final LiveKeyData liveKeyData = localKeyStore.getLiveKeyData();
        final Properties properties = configurationLoader.getAppProperties();
        final long jwtTTL = Long.parseLong(properties.getProperty(APP_ENUM.APP_JWT_TTL.key));

        return Jwts.builder()
                .setIssuer(properties.getProperty(APP_ENUM.APP_NODE_NAME.key))
                .setSubject(email)
                .setIssuedAt(Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond())))
                .setExpiration(Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond() + jwtTTL)))
                .setId(liveKeyData.uuid.toString())
                .signWith(liveKeyData.privateKey)
                .compact();
    }

    @Override
    public String issueJWT(final String email, final Map<String,Object> claims) {
        final LiveKeyData liveKeyData = localKeyStore.getLiveKeyData();
        final Properties properties = configurationLoader.getAppProperties();
        final long jwtTTL = Long.parseLong(properties.getProperty(APP_ENUM.APP_JWT_TTL.key));

        return Jwts.builder()
                .setIssuer(properties.getProperty(APP_ENUM.APP_NODE_NAME.key))
                .setSubject(email)
                .setClaims(claims)
                .setIssuedAt(Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond())))
                .setExpiration(Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond() + jwtTTL)))
                .setId(liveKeyData.uuid.toString())
                .signWith(liveKeyData.privateKey)
                .compact();
    }
}
