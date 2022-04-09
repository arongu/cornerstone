package cornerstone.webapp.security;

import cornerstone.webapp.common.logmsg.CommonLogMessages;
import cornerstone.webapp.services.accounts.management.enums.SYSTEM_ROLE_ENUM;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import java.util.Set;

// TODO - FIX IT, filters disabled until code is back and running
//@Provider
//@PreMatching
//@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String BEARER = "Bearer ";
    private static final String INVALID_USER_ROLE_FOR_SUBJECT = "Invalid user role detected in a signed JWT! subject: '%s', role: '%s' !";

    // TODO this is not a single string, this is a set !
    //  (for future, multiple roles will be available) // SERIOUS issue
    private static Set<SYSTEM_ROLE_ENUM> extractUserRoles(final Claims claims) {
        Set<SYSTEM_ROLE_ENUM> roles = null;
        String roleFromClaims   = "UNSET";

//        try {
//            roleFromClaims    = claims.get(JWT_SERVICE_CLAIMS.role.name()).toString();
//            SYSTEM_ROLE_ENUM roles = SYSTEM_ROLE_ENUM.valueOf(roleFromClaims);
//            roles = new HashSet<>();
//            roles.add(roles);
//
//        } catch (final IllegalArgumentException | NullPointerException e) {
//            final String em = CommonLogMessages.PREFIX_SECURITY + CommonLogMessages.PREFIX_FILTER + String.format(INVALID_USER_ROLE_FOR_SUBJECT, roleFromClaims, claims.getSubject());
//            logger.error(em);
//        }

        return roles;
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
    public void filter(final ContainerRequestContext containerRequestContext) throws ForbiddenException {
//        final String m = String.format(
//                CommonLogMessages.PREFIX_SECURITY + CommonLogMessages.PREFIX_FILTER + "Filtering URI='%s', METHOD='%s', ADDR='%s'", httpServletRequest.getRequestURI(), httpServletRequest.getMethod(), httpServletRequest.getLocalAddr()
//        );
//        logger.info(m);
//
//        if ( containerRequestContext == null) {
//            final String m2 = CommonLogMessages.PREFIX_SECURITY + CommonLogMessages.PREFIX_FILTER + "containerRequestContext is null!";
//            logger.error(m2);
//            throw new ForbiddenException();
//        }
//
//        if ( containerRequestContext.getSecurityContext() == null) {
//            final String m3 = CommonLogMessages.PREFIX_SECURITY + CommonLogMessages.PREFIX_FILTER + "containerRequestContext.getSecurityContext() returned null!";
//            logger.error(m3);
//            throw new ForbiddenException();
//        }
//
//        final String authorizationHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
//        boolean       secure    = containerRequestContext.getSecurityContext().isSecure();
//        String        subject   = null;
//        Set<SYSTEM_ROLE_ENUM> roles = null;
//        Claims        claims    = null;
//
//        // if authorization Header is set
//        if ( authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
//            final String    jws       = authorizationHeader.substring(BEARER.length());
//            final JwtParser jwtParser = Jwts.parserBuilder().setSigningKeyResolver(signingKeyResolver).build();
//
//            // try to parse claims from JWS
//            Jws<Claims> claimsJws = null;
//            try {
//                claimsJws = jwtParser.parseClaimsJws(jws);
//
//            } catch (final ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException | NoSuchElementException exception) {
//                logger.info(CommonLogMessages.PREFIX_SECURITY + CommonLogMessages.PREFIX_FILTER + "JWT/JWS could not be validated! (Anonymous will be granted)");
//            }
//
//            // set claims, userRoles, subject based on Jws<Claims>
//            if ( claimsJws != null && claimsJws.getBody() != null) {
//                claims    = claimsJws.getBody();
//                roles = extractUserRoles(claims);
//                subject   = claims.getSubject();
//
//            // if Jws<Claims> is null set "NO_ROLE" for Jersey, otherwise it will forbid any resource which is not annotated
//            } else {
//                roles = new HashSet<>();
//                roles.add(SYSTEM_ROLE_ENUM.NO_ROLE);
//            }
//
//        // if no authorization header is set, set role to NO_ROLE
//        } else {
//            roles = new HashSet<>();
//            roles.add(SYSTEM_ROLE_ENUM.NO_ROLE);
//        }
//
//        final Principal principal = subject == null ? new PrincipalImpl("Anonymous") : new PrincipalImpl(subject);
//        final JwtSecurityContext jsc = new JwtSecurityContext(secure, principal, roles, claims);
//        containerRequestContext.setSecurityContext(jsc);
    }
}
