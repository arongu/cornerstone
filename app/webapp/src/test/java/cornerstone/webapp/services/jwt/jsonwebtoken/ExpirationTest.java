package cornerstone.webapp.services.jwt.jsonwebtoken;

import cornerstone.webapp.services.keys.rotation.KeyPairWithUUID;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExpirationTest {
    private static KeyPairWithUUID keyPairWithUUID;

    @BeforeAll
    public static void init() {
        keyPairWithUUID = new KeyPairWithUUID();
    }

    @Test
    public void parseClaims_shouldNotThrowException_whenJWSisSignedAndNotExpired() {
        final Map<String,Object> claims = new HashMap<>();
        claims.put("test_claim", "test");
        final String issuer = "testIssuer";
        final String subject = "TestUser";
        final Instant now = Instant.now();
        final Date issuedAt = Date.from(now);
        final Date expiration = Date.from(Instant.ofEpochSecond(now.getEpochSecond() + 3600));


        final String jws = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(keyPairWithUUID.keyPair.getPrivate())
                .compact();


        final JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(keyPairWithUUID.keyPair.getPublic()).build();
        assertTrue(jwtParser.isSigned(jws));
        assertDoesNotThrow(() -> jwtParser.parseClaimsJws(jws));
    }

    @Test
    public void parseClaims_shouldThrowException_whenJWSisSignedButExpired() {
        final Map<String,Object> claims = new HashMap<>();
        claims.put("test_claim", "test");
        final String issuer = "testIssuer";
        final String subject = "TestUser";
        final Instant now = Instant.now();
        final Date issuedAt = Date.from(now);
        final Date expiration = Date.from(Instant.ofEpochSecond(now.getEpochSecond() - 86400));


        final String jws = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(keyPairWithUUID.keyPair.getPrivate())
                .compact();


        final JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(keyPairWithUUID.keyPair.getPublic()).build();
        assertTrue(jwtParser.isSigned(jws));
        assertThrows(ExpiredJwtException.class, () -> {
            jwtParser.parseClaimsJws(jws);
        });
    }
}
