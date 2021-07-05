package cornerstone.webapp.services.jwt.jsonwebtoken;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.services.jwt.JWTService;
import cornerstone.webapp.services.jwt.JWTServiceImpl;
import cornerstone.webapp.services.keys.rotation.KeyPairWithUUID;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStoreImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.Key;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AlgMismatchTest {
    private static ConfigLoader  configLoader;
    private static LocalKeyStore localKeyStore;

    @BeforeAll
    public static void beforeAll() {
        final String test_config_dir = "../../_test_config/";
        final String key_file        = Paths.get(test_config_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String conf_file       = Paths.get(test_config_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            configLoader                          = new ConfigLoader(key_file, conf_file);
            localKeyStore                         = new LocalKeyStoreImpl();
            final KeyPairWithUUID keyPairWithUUID = new KeyPairWithUUID();
            localKeyStore.setSigningKeys(keyPairWithUUID.uuid, keyPairWithUUID.keyPair.getPrivate(), keyPairWithUUID.keyPair.getPublic());

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /*   x
        algo key issuer subject claims iat exp
    */
    @Test
    public void parseClaims_shouldThrowException_whenAlgIsMismatched() throws Exception {
        final String subject        = "hellomoto@xmal.com";
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final Key publicKey         = localKeyStore.getSigningKeys().publicKey;
        final String keyId          = localKeyStore.getSigningKeys().uuid.toString();
        final String jws            = jwtService.createJws(subject);
        final String[] parsed       = jws.split("\\.");
        final String payload        = parsed[1];
        final String signature      = parsed[2];

        // HS
        final String strHS256           = "{\"typ\":\"JWT\",\"alg\":\"HS256\"}";
        final String strHS384           = "{\"typ\":\"JWT\",\"alg\":\"HS384\"}";
        final String strHS512           = "{\"typ\":\"JWT\",\"alg\":\"HS512\"}";
        // ES
        final String strES256           = "{\"typ\":\"JWT\",\"alg\":\"ES256\"}";
        final String strES384           = "{\"typ\":\"JWT\",\"alg\":\"ES384\"}";
        final String strES512           = "{\"typ\":\"JWT\",\"alg\":\"ES512\"}";
        // RS
        final String strRS256           = "{\"typ\":\"JWT\",\"alg\":\"RS256\"}";
        final String strRS384           = "{\"typ\":\"JWT\",\"alg\":\"RS384\"}";
        //final String strRS512         = "{\"typ\":\"JWT\",\"alg\":\"RS512\"}"; // proof even headers cannot be tempered, keyId - kid is added from JWTServiceImpl
        final String strRS512           = "{\"typ\":\"JWT\",\"kid\":\"" + keyId + "\",\"alg\":\"RS512\"}";
        // PS
        final String strPS256           = "{\"typ\":\"JWT\",\"alg\":\"PS256\"}";
        final String strPS384           = "{\"typ\":\"JWT\",\"alg\":\"PS384\"}";
        final String strPS512           = "{\"typ\":\"JWT\",\"alg\":\"PS512\"}";
        // custom strings
        final String headerAlgs         = "{\"typ\":\"JWT\",\"algs\":\"PS256\"}";
        final String headerAlgXXX       = "{\"typ\":\"JWT\",\"alg\":\"XXX\"}";
        final String headerAlgEmpty     = "{\"typ\":\"JWT\",\"alg\":\"\"}";


        final Base64.Encoder encoder = Base64.getEncoder();
        // algs instead of alg
        assertThrows(MalformedJwtException.class, () -> {
            final String forgedJws = new String(encoder.encode(headerAlgs.getBytes())) + "." + payload + "." + signature;
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject();
        });

        // xxx alg
        assertThrows(SignatureException.class, () -> {
            final String forgedJws = new String(encoder.encode(headerAlgXXX.getBytes())) + "." + payload + "." + signature;
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject();
        });

        // empty alg
        assertThrows(MalformedJwtException.class, () -> {
            final String forgedJws = new String(encoder.encode(headerAlgEmpty.getBytes())) + "." + payload + "." + signature;
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject();
        });

        // forging jws
        // RS256
        assertThrows(SignatureException.class, () -> {
            final String forgedJws = new String(encoder.encode(strRS256.getBytes())) + "." + payload + "." + signature;
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject();
        });

        // RS384
        assertThrows(SignatureException.class, () -> {
            final String forgedJws = new String(encoder.encode(strRS384.getBytes())) + "." + payload + "." + signature;
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject();
        });

        // RS512
        assertDoesNotThrow(() -> {
            final String forgedJws = new String(encoder.encode(strRS512.getBytes())) + "." + payload + "." + signature;
            System.out.println(jws);
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject();
        });

        // HS256
        assertThrows(UnsupportedJwtException.class, () -> {
            final String forgedJws = new String(encoder.encode(strHS256.getBytes())) + "." + payload + "." + signature;
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject();
        });

        // HS384
        assertThrows(UnsupportedJwtException.class, () -> {
            final String forgedJws = new String(encoder.encode(strHS384.getBytes())) + "." + payload + "." + signature;
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject();
        });

        // HS512
        assertThrows(UnsupportedJwtException.class, () -> {
            final String forgedJws = new String(encoder.encode(strHS512.getBytes())) + "." + payload + "." + signature;
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject();
        });

        // ES256
        assertThrows(UnsupportedJwtException.class, () -> {
            final String forgedJws = new String(encoder.encode(strES256.getBytes())) + "." + payload + "." + signature;
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject();
        });

        // ES384
        assertThrows(UnsupportedJwtException.class, () -> {
            final String forgedJws = new String(encoder.encode(strES384.getBytes())) + "." + payload + "." + signature;
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject();
        });

        // ES512
        assertThrows(UnsupportedJwtException.class, () -> {
            final String forgedJws = new String(encoder.encode(strES512.getBytes())) + "." + payload + "." + signature;
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject();
        });

        // PS256
        assertThrows(SignatureException.class, () -> {
            final String forgedJws = new String(encoder.encode(strPS256.getBytes())) + "." + payload + "." + signature;
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject();
        });

        // PS384
        assertThrows(SignatureException.class, () -> {
            final String forgedJws = new String(encoder.encode(strPS384.getBytes())) + "." + payload + "." + signature;
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject();
        });

        // PS512
        assertThrows(SignatureException.class, () -> {
            final String forgedJws = new String(encoder.encode(strPS512.getBytes())) + "." + payload + "." + signature;
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject();
        });
    }
}
