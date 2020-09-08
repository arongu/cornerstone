package cornerstone.webapp.services.jwt.jsonwebtoken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.security.Key;

public final class jsonwebtokenTestHelper {
    private jsonwebtokenTestHelper(){}

    public static Claims extractClaims(final String jws, final Key key) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jws).getBody();
    }
}
