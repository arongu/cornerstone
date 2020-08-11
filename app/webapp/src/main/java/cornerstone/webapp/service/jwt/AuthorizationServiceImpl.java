package cornerstone.webapp.service.jwt;

import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.service.rsa.store.local.LocalKeyStore;
import cornerstone.webapp.service.rsa.store.local.PrivateKeyWithUUID;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Properties;

public class AuthorizationServiceImpl implements AuthorizationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationServiceImpl.class);

    private final ConfigurationLoader configurationLoader;
    private final LocalKeyStore localKeyStore;

    @Inject
    public AuthorizationServiceImpl(final ConfigurationLoader configurationLoader, final LocalKeyStore localKeyStore){
        this.configurationLoader = configurationLoader;
        this.localKeyStore = localKeyStore;
        logger.info(String.format(DefaultLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    @Override
    public String issueJWT(final String emailAddress) throws AuthorizationServiceException {
        try {
            final PrivateKeyWithUUID privateKeyWithUUID = localKeyStore.getPrivateKey();
            final Properties properties = configurationLoader.getAppProperties();
            final long jwtTTL = Long.parseLong(properties.getProperty(APP_ENUM.APP_JWT_TTL.key));

            return Jwts.builder()
                    .setIssuer(this.getClass().getName())
                    .setSubject(emailAddress)
                    .claim("scope", "user")
                    .setIssuedAt  (Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond())))
                    .setExpiration(Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond() + jwtTTL)))
                    .signWith(privateKeyWithUUID.privateKey)
                    .compact();

        } catch (final NoSuchElementException e){
            logger.error("key is null");
            throw new AuthorizationServiceException();
        }
    }
}
