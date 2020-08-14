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
import java.security.Key;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class jsonwebtokenBasicTest {
    private static ConfigLoader configLoader;
    private static LocalKeyStore localKeyStore;

    @BeforeAll
    public static void init() {
        final String test_config_dir = "../../_test_config/";
        final String key_file        = Paths.get(test_config_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String conf_file       = Paths.get(test_config_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            configLoader                          = new ConfigLoader(key_file, conf_file);
            localKeyStore                         = new LocalKeyStoreImpl();
            final KeyPairWithUUID keyPairWithUUID = new KeyPairWithUUID();
            localKeyStore.setLiveKeys(keyPairWithUUID.uuid, keyPairWithUUID.keyPair.getPrivate(), keyPairWithUUID.keyPair.getPublic());

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getSubject_shouldReturnTheEmailAddress() {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final String email          = "hellomoto@xmal.com";
        final String jws            = jwtService.createJws(email); // a signed jwt token is called 'jws'
        final Key publicKey         = localKeyStore.getLiveKeys().publicKey;

        assertEquals(email, Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody().getSubject());
    }

    @Test
    public void parseClaimsJws_shouldNotThrowAnyException_whenAllIsSetProperly() {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final String email          = "hellomoto@xmal.com";
        final String jws            = jwtService.createJws(email);
        final Key publicKey         = localKeyStore.getLiveKeys().publicKey;

        assertDoesNotThrow(() -> Jwts.parserBuilder().setSigningKey(publicKey).requireSubject(email).build().parseClaimsJws(jws));
    }

    @Test
    public void parseClaimsJws_shouldThrowSignatureException_whenDifferentKeyIsUsed() {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final String email          = "hellomoto@xmal.com";
        final String jws            = jwtService.createJws(email);
        final Key differentKey      = new KeyPairWithUUID().keyPair.getPublic();

        assertThrows(SignatureException.class, () -> Jwts.parserBuilder().setSigningKey(differentKey).requireSubject(email).build().parseClaimsJws(jws));
    }

    @Test
    public void parseClaimsJws_shouldThrowIncorrectClaimException_whenRequiredSubjectIsNotThere() {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final String email          = "hellomoto@xmal.com";
        final String jws            = jwtService.createJws(email);
        final Key publicKey         = localKeyStore.getLiveKeys().publicKey;

        assertThrows(IncorrectClaimException.class, () -> Jwts.parserBuilder().setSigningKey(publicKey).requireSubject("moooooooooooo").build().parseClaimsJws(jws));
    }

    @Test
    public void getSubject_shouldReturnEmailAsSubject() {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final String email          = "hellomoto@xmal.com";
        final String jws            = jwtService.createJws(email); // a signed jwt token is called 'jws'
        final Key publicKey         = localKeyStore.getLiveKeys().publicKey;

        assertEquals(email, Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody().getSubject());
    }

    @Test
    public void createJwsWithParseClaims_shouldReturnClaimsFromMap() {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final Key publicKey         = localKeyStore.getLiveKeys().publicKey;
        final String email          = "hellomoto@xmal.com";
        final Map<String,Object> m  = new HashMap<>();
        m.put("claimOne", "one");
        m.put("claimTwo", 2);
        m.put("claimThree", 3.2);


        final String jws    = jwtService.createJws(email, m);
        final Claims claims = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody();
        final String  claimOne  = (String)  claims.get("claimOne");
        final Integer claimTwo  = (Integer) claims.get("claimTwo");
        final Double claimThree = (Double)  claims.get("claimThree");


        assertEquals("one", claimOne);
        assertEquals(2, claimTwo);
        assertEquals(3.2, claimThree);
    }

    @Test
    public void expMinusIatShouldBeEqualToJwtTTL() {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final PublicKey publicKey   = localKeyStore.getLiveKeys().publicKey;
        final Integer jwtTTL        = Integer.valueOf((String) configLoader.getAppProperties().get(APP_ENUM.APP_JWT_TTL.key));
        final String email          = "hellomoto@xmal.com";


        final String jws           = jwtService.createJws(email);
        final Claims claims        = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody();
        final Integer issuedEpoch  = (Integer) claims.get("iat");
        final Integer expiresEpoch = (Integer) claims.get("exp");


        assertEquals(jwtTTL, expiresEpoch - issuedEpoch);
    }

    @Test
    public void emailAkaSubjectTemperingTest() {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final PublicKey validKey    = localKeyStore.getLiveKeys().publicKey;
        final String email          = "hellomoto@xmal.com";
        final String jws            = jwtService.createJws(email);


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

