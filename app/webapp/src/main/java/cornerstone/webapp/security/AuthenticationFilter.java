package cornerstone.webapp.security;

import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.SigningKeysException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.PublicKey;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    public static final String STR_BEARER = "Bearer ";

    // Messages
    // Service
    public static final String MSG_LOCAL_KEYSTORE_SERVICE_IS_NULL              = "LocalKeystore service is null!";
    // Annotation

    // Header
    public static final String MSG_AUTHORIZATION_HEADER_IS_NOT_SET             = "HTTP Header " + HttpHeaders.AUTHORIZATION + " is not set!";
    public static final String MSG_AUTHORIZATION_STRING_MUST_START_WITH_BEARER = "HTTP Header " + HttpHeaders.AUTHORIZATION + " must start with '" + STR_BEARER + "' received: '%s'!";

    /**
     * Preliminary check for HTTP Authorization header
     * @param authorizationHeader the value of the Authorization Header
     * @throws IOException Throws IOException when header string is null or does not start with STR_BEARER ('Bearer ')
     */
    private static void checkAuthorizationHeader(final String authorizationHeader) throws IOException {
        if ( authorizationHeader == null) {
            throw new IOException(MSG_AUTHORIZATION_HEADER_IS_NOT_SET);
        }

        if ( authorizationHeader.startsWith(STR_BEARER)) {
            throw new IOException(String.format(MSG_AUTHORIZATION_STRING_MUST_START_WITH_BEARER, HTTP_HEADER_AUTHORIZATION_STRING));
        }
    }

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    private LocalKeyStore localKeyStore;

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws IOException {
        final Method method = resourceInfo.getResourceMethod();
        if (method.isAnnotationPresent(DenyAll.class)) {
            throw new IOException("DenyAll annotation present!");
        }

        if (method.isAnnotationPresent(PermitAll.class)) {
            if (method.isAnnotationPresent(RolesAllowed.class)) {

            }
        }

        switch (method) {
            case
        }
    }

/*
        final String HTTP_HEADER_AUTHORIZATION_STRING = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        checkAuthorizationHeader(HTTP_HEADER_AUTHORIZATION_STRING);



        if ( localKeyStore == null ) {
            throw new IOException(MSG_LOCAL_KEYSTORE_SERVICE_IS_NULL);
        }

        try {
            final PublicKey publicKey = localKeyStore.getSigningKeys().publicKey;
            final JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(publicKey).build();
            jwtParser.parse(HTTP_HEADER_AUTHORIZATION_STRING.substring(STR_BEARER.length()));


        } catch (final SigningKeysException signingKeysException) {
            throw new IOException(signingKeysException.getMessage());
        }
    }*/
}
