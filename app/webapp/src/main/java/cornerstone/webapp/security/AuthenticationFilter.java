package cornerstone.webapp.security;

import cornerstone.webapp.logmsg.CommonLogMessages;
import cornerstone.webapp.services.accounts.management.UserRole;
import cornerstone.webapp.services.jwt.JWT_SERVICE_CLAIMS;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String BEARER = "Bearer ";
    private static final String INVALID_USER_ROLE_FOR_SUBJECT = "Invalid user role detected in a signed JWT! subject: '%s', role: '%s' !";

    // TODO this is not a single string, this is a set !
    //  (for future, multiple roles will be available) // SERIOUS issue
    private static Set<UserRole> extractUserRoles(final Claims claims) {
        Set<UserRole> userRoles = null;
        String roleFromClaims   = "UNSET";

        try {
            roleFromClaims    = claims.get(JWT_SERVICE_CLAIMS.role.name()).toString();
            UserRole userRole = UserRole.valueOf(roleFromClaims);
            userRoles         = new HashSet<>();
            userRoles.add(userRole);

        } catch (final IllegalArgumentException | NullPointerException e) {
            final String em = CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + String.format(INVALID_USER_ROLE_FOR_SUBJECT, roleFromClaims, claims.getSubject());
            logger.error(em);
        }

        return userRoles;
    }

    @Context
    private HttpServletRequest httpServletRequest;

    private SigningKeyResolver signingKeyResolver;

    public AuthenticationFilter() {
    }

    @Inject
    public AuthenticationFilter(final SigningKeyResolver signingKeyResolver) {
        this.signingKeyResolver = signingKeyResolver;
    }

    //secure, principal, userRoles, claims
    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException, ForbiddenException {
        final String m = String.format(
                CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + "Filtering URI='%s', METHOD='%s', ADDR='%s'", httpServletRequest.getRequestURI(), httpServletRequest.getMethod(), httpServletRequest.getLocalAddr()
        );
        logger.info(m);

        if ( containerRequestContext == null) {
            final String m2 = CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + "containerRequestContext is null!";
            logger.error(m2);
            throw new ForbiddenException();
        }

        if ( containerRequestContext.getSecurityContext() == null) {
            final String m3 = CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + "containerRequestContext.getSecurityContext() returned null!";
            logger.error(m3);
            throw new ForbiddenException();
        }

        final String authorizationHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        boolean secure                   = containerRequestContext.getSecurityContext().isSecure();
        String subject                   = null;
        Set<UserRole> userRoles          = null;
        Claims claims                    = null;


        if ( authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            final String    jws       = authorizationHeader.substring(BEARER.length());
            final JwtParser jwtParser = Jwts.parserBuilder().setSigningKeyResolver(signingKeyResolver).build();

            Jws<Claims> claimsJws = null;
            try {
                claimsJws = jwtParser.parseClaimsJws(jws);

            } catch (final ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException | NoSuchElementException exception) {
                logger.info(CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + "JWT/JWS could not be validated! (Anonymous will be granted)");

            } catch (final NullPointerException nullPointerException) {
                logger.error(CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + "NullPointerException caught!");
                throw new ForbiddenException();
            }

            if ( claimsJws != null && claimsJws.getBody() != null) {
                claims    = claimsJws.getBody();
                userRoles = extractUserRoles(claims);
                subject   = claims.getSubject();
            }
        }

        final Principal principal = subject == null ? new PrincipalImpl("Anonymous") : new PrincipalImpl(subject);
        containerRequestContext.setSecurityContext(
                new JwtSecurityContext(secure, principal, userRoles, claims)
        );
    }
}
