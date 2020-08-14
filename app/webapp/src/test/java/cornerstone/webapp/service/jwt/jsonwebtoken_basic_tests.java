package cornerstone.webapp.service.jwt;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.service.rsa.rotation.KeyPairWithUUID;
import cornerstone.webapp.service.rsa.store.local.LocalKeyStore;
import cornerstone.webapp.service.rsa.store.local.LocalKeyStoreImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class jsonwebtoken_basic_tests {
    private static ConfigLoader configLoader;
    private static LocalKeyStore localKeyStore;

    @BeforeAll
    public static void init() {
        final String test_files_dir = "../../_test_config/";
        final String keyFile        = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile       = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            configLoader = new ConfigLoader(keyFile, confFile);
            localKeyStore            = new LocalKeyStoreImpl();
            final KeyPairWithUUID kp = new KeyPairWithUUID();
            localKeyStore.setLiveKeyData(kp.uuid, kp.keyPair.getPrivate(), kp.keyPair.getPublic());

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getSubjectTests() {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final String email          = "hellomoto@xmal.com";
        final String jws            = jwtService.issueJWT(email); // a signed jwt token is called 'jws'

        assertEquals(email, Jwts.parserBuilder().setSigningKey(localKeyStore.getLiveKeys().publicKey).build().parseClaimsJws(jws).getBody().getSubject());
        assertNotEquals(Jwts.parserBuilder().setSigningKey(localKeyStore.getLiveKeys().publicKey).build().parseClaimsJws(jws).getBody().getSubject(), "nono");
    }

    @Test
    public void keyAndClaimTests() {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final String email          = "hellomoto@xmal.com";
        final String jws            = jwtService.issueJWT(email); // a signed jwt token is called 'jws'

        // should not throw any exceptions when
        // key is valid and
        // claim is present
        assertDoesNotThrow(() -> {
            final PublicKey validKey = localKeyStore.getLiveKeys().publicKey;
            Jwts.parserBuilder().setSigningKey(validKey).requireSubject(email).build().parseClaimsJws(jws);
        });

        // should throw SignatureException
        // when key is invalid but
        // claim is present
        assertThrows(SignatureException.class, () -> {
            final PublicKey invalidKey = new KeyPairWithUUID().keyPair.getPublic();
            Jwts.parserBuilder().setSigningKey(invalidKey).requireSubject(email).build().parseClaimsJws(jws);
        });

        // should throw IncorrectClaimException
        // when key is valid but
        // claim is not present
        assertThrows(IncorrectClaimException.class, () -> {
            final PublicKey validKey = localKeyStore.getLiveKeys().publicKey;
            Jwts.parserBuilder().setSigningKey(validKey).requireSubject("moooooooooooo").build().parseClaimsJws(jws);
        });
    }

    @Test
    public void requireIssuerTest() {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final String email = "hellomoto@xmal.com";
        final String jws   = jwtService.issueJWT(email); // a signed jwt token is called 'jws'

        assertEquals(email, Jwts.parserBuilder().setSigningKey(localKeyStore.getLiveKeys().publicKey).build().parseClaimsJws(jws).getBody().getSubject());
        assertThrows(SignatureException.class, () -> {
            final KeyPairWithUUID differentKeys = new KeyPairWithUUID();
            Jwts.parserBuilder().setSigningKey(differentKeys.keyPair.getPublic()).build().parseClaimsJws(jws);
        });
    }

    @Test
    public void claimTests() {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final String email = "hellomoto@xmal.com";
        final Map<String,Object> m = new HashMap<>();
        m.put("claimOne", "one");
        m.put("claimTwo", 2);
        m.put("claimThree", 3.2);


        // should throw SignatureException
        // when claims are there but
        // key is invalid
        assertThrows(SignatureException.class, () -> {
            final PublicKey invalidKey = new KeyPairWithUUID().keyPair.getPublic();
            final String jws           = jwtService.issueJWT(email, m);
            Jwts.parserBuilder().setSigningKey(invalidKey).build().parseClaimsJws(jws);
        });


        // should not throw exception
        // when claims are there and
        // key is valid
        assertDoesNotThrow(() -> {
            final String jws         = jwtService.issueJWT(email, m);
            final PublicKey validKey = localKeyStore.getLiveKeys().publicKey;
            Jwts.parserBuilder().setSigningKey(validKey).build().parseClaimsJws(jws);
        });


        // check claims
        final String jws = jwtService.issueJWT(email, m);
        final PublicKey validKey = localKeyStore.getLiveKeys().publicKey;
        final Claims claims      = Jwts.parserBuilder().setSigningKey(validKey).build().parseClaimsJws(jws).getBody();
        assertEquals("one", claims.get("claimOne"));
        assertEquals(2, claims.get("claimTwo"));
        assertEquals(3.2, claims.get("claimThree"));
    }

    @Test
    public void expMinusIatShouldBeEqualToJwtTTL() {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final PublicKey validKey    = localKeyStore.getLiveKeys().publicKey;
        final Integer jwtTTL        = Integer.valueOf((String) configLoader.getAppProperties().get(APP_ENUM.APP_JWT_TTL.key));
        final String email          = "hellomoto@xmal.com";


        final String jws           = jwtService.issueJWT(email);
        final Claims claims        = Jwts.parserBuilder().setSigningKey(validKey).build().parseClaimsJws(jws).getBody();
        final Integer issuedEpoch  = (Integer) claims.get("iat");
        final Integer expiresEpoch = (Integer) claims.get("exp");


        assertEquals(jwtTTL, expiresEpoch - issuedEpoch);
    }

    @Test
    public void emailAkaSubjectTemperingTest() {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final PublicKey validKey    = localKeyStore.getLiveKeys().publicKey;
        final String email          = "hellomoto@xmal.com";
        final String jws            = jwtService.issueJWT(email);


        // original, valid jws
        final Base64.Decoder decoder = Base64.getDecoder();
        final String[] parsed        = jws.split("\\.");
        final String header          = new String(decoder.decode(parsed[0]));
        final String payload         = new String(decoder.decode(parsed[1]));
        final String signature       = parsed[2];


        // tempered account email
        final String temperedPayload = payload.replaceFirst(email, "haxor@mail.com");
        final Base64.Encoder encode = Base64.getEncoder();
        final String b64temperedPayload = new String(encode.encode(temperedPayload.getBytes()));
        final String temperedJWS = parsed[0] + "." + b64temperedPayload + "." + parsed[2];


        // display data
        System.out.println("original jws header   : " + header);
        System.out.println("tempered jws header   : " + header);
        System.out.println("original jws payload  : " + payload);
        System.out.println("tempered jws payload  : " + temperedPayload);
        System.out.println("original jws signature: " + signature);
        System.out.println("tempered jws signature: " + signature);
        System.out.println("original jws: " + jws);
        System.out.println("tempered jws: " + temperedJWS);



        // valid
        assertEquals(email, Jwts.parserBuilder().setSigningKey(validKey).build().parseClaimsJws(jws).getBody().getSubject());
        assertTrue(Jwts.parserBuilder().setSigningKey(validKey).build().isSigned(jws));
        // tempered
        assertThrows(SignatureException.class, () -> Jwts.parserBuilder().setSigningKey(validKey).build().parseClaimsJws(temperedJWS).getBody().getSubject());
        // !!!!! Signed but tempered
        assertTrue(Jwts.parserBuilder().setSigningKey(validKey).build().isSigned(temperedJWS));
    }
}

