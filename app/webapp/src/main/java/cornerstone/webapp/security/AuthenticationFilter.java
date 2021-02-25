package cornerstone.webapp.security;

import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import io.jsonwebtoken.Claims;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    public static final String STR_BEARER = "Bearer ";

    public static final String MSG_LOCAL_KEYSTORE_SERVICE_IS_NULL = "LocalKeystore service is null!";
    public static final String MSG_LOCAL_KEYSTORE_ERROR = "LocalKeystore error during signing key retrieval: '%s'";
    public static final String MSG_AUTHORIZATION_HEADER_IS_NOT_SET = "HTTP Header " + HttpHeaders.AUTHORIZATION + " is not set!";
    public static final String MSG_AUTHORIZATION_STRING_MUST_START_WITH_BEARER = "HTTP Header " + HttpHeaders.AUTHORIZATION + " must start with '" + STR_BEARER + "' received: '%s'!";

    public AuthenticationFilter() {
    }

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    private LocalKeyStore localKeyStore;

    private static String getJwsFromRequestContext(final ContainerRequestContext containerRequestContext) throws FilterException {
        final String authorizationHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null) {
            throw new FilterException(MSG_AUTHORIZATION_HEADER_IS_NOT_SET);
        }

        if (! authorizationHeader.startsWith(STR_BEARER)) {
            throw new FilterException(String.format(MSG_AUTHORIZATION_STRING_MUST_START_WITH_BEARER, authorizationHeader));
        }

        return authorizationHeader.substring(STR_BEARER.length());
    }

    private static UUID getKeyIdFromJWS(final String jws) throws FilterException {
        final Claims claims = Jwts.parserBuilder().build().parseClaimsJws(jws).getBody();
        return UUID.fromString(String.valueOf(claims.get("keyId")));
    }

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws IOException {
        final Method method = resourceInfo.getResourceMethod();
        if ( method != null) {
            logger.info("noooooooooooooooooooot noooooooooooooooooooooooooooooot");
        }
        //@DenyAll -> deny
        if (method.isAnnotationPresent(DenyAll.class)) {
            throw new FilterException("DENIED -- @DenyAll");
        }

        if (method.isAnnotationPresent(PermitAll.class)){
            logger.info("pppppppppppppppppppppppppppppreeeeeeeeeeeeeeeeeeeeeesent");
        }
        //@PermitAll -> chek if @RolesAllowed is not set -> allow
        if (method.isAnnotationPresent(PermitAll.class) && !method.isAnnotationPresent(RolesAllowed.class)) {
            return;
        }

        //@RolesAllowed
        if (method.isAnnotationPresent(RolesAllowed.class)) {
            final String jws = getJwsFromRequestContext(containerRequestContext);
            final UUID keyId = getKeyIdFromJWS(jws);

            if (localKeyStore == null) {
                throw new FilterException(MSG_LOCAL_KEYSTORE_SERVICE_IS_NULL);
            }

            PublicKey publicKey = localKeyStore.getPublicKey(keyId);
            final JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(publicKey).build();
            final Claims claims = jwtParser.parseClaimsJws(jws).getBody();

            final RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
            Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAnnotation.value()));
            if(rolesSet.contains(String.valueOf(claims.get("role")))){
                return;
            }
        }

        throw new FilterException("DENIED -- ALL RULES EXHAUSTED");
    }
}
