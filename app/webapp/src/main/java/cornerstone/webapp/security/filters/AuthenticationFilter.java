package cornerstone.webapp.security.filters;

import cornerstone.webapp.services.jwt.SigningKeyResolverImpl;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import io.jsonwebtoken.*;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;

class AuthenticationFilter implements ContainerRequestFilter {
    private static final String BEARER  = "Bearer ";
    private final SigningKeyResolver signingKeyResolver;

    @Inject
    public AuthenticationFilter(final SigningKeyResolver signingKeyResolver) {
        this.signingKeyResolver = signingKeyResolver;
    }

    private void setSecurityContextBasedOnToken(final String jws) {
        final JwtParser jwtParser = Jwts.parserBuilder().setSigningKeyResolver(signingKeyResolver).build();
        final Claims claims       = jwtParser.parseClaimsJwt(jws).getBody();
        final boolean isSigned    = jwtParser.isSigned(jws);
    }

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws IOException {
        final String authorizationHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if ( authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            final String jwt = authorizationHeader.substring(BEARER.length());
//            setSecurityContextBasedOnToken();
        }
    }
}
