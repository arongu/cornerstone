package cornerstone.webapp.services.authorization_service;

import cornerstone.webapp.configuration.ConfigurationLoader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

public class AuthorizationService implements AuthorizationServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);
    private Key key;

    public void loadKey(final ConfigurationLoader configurationLoader) {

        final String base64key = "alma";
                //(String) configReader.getAppProperties().get(ApplicationConfigFields.APP_JWS_KEY.key);

        if ( base64key != null &&
           ! base64key.isEmpty() ) {

            key = Keys.hmacShaKeyFor(
                    Base64.getDecoder().decode(base64key)
            );

        } else {
            logger.error("JWS key for JWT token generation is set to null or empty, the app will not work correctly!");
        }
    }

    @Inject
    public AuthorizationService(final ConfigurationLoader cp) {
        loadKey(cp);
    }

    @Override
    public String issueJWT(final String emailAddress) throws AuthorizationServiceException {
        if ( null != key ) {

            return Jwts.builder()
                    .setIssuer(this.getClass().getName())
                    .setSubject(emailAddress)
                    .claim("scope", "user")
                    .setIssuedAt(
                            Date.from(
                                    Instant.ofEpochSecond(
                                            Instant.now().getEpochSecond()
                                    )
                            )
                    )
                    .setExpiration(
                            Date.from(
                                    Instant.ofEpochSecond(
                                            Instant.now().getEpochSecond() + 86400L // valid for 24h
                                    )
                            )
                    )
                    .signWith(key)
                    .compact();
        } else {
            // TODO add proper DTO, error message, JSON... etc
            logger.error("key is null");
            throw new AuthorizationServiceException();
        }
    }
}
