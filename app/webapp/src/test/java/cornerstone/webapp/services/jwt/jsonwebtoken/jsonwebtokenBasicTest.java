package cornerstone.webapp.services.jwt.jsonwebtoken;

import cornerstone.webapp.config.ConfigLoader;
import cornerstone.webapp.config.enums.APP_ENUM;
import cornerstone.webapp.services.jwt.JWTService;
import cornerstone.webapp.services.jwt.JWTServiceImpl;
import cornerstone.webapp.services.keys.rotation.KeyPairWithUUID;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStoreImpl;
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
            localKeyStore.setupSigning(keyPairWithUUID.uuid, keyPairWithUUID.keyPair.getPrivate(), keyPairWithUUID.keyPair.getPublic());

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getSubject_shouldReturnTheEmailAddress() throws Exception {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final String email          = "hellomoto@xmal.com";
        final String jws            = jwtService.createJws(email); // a signed jwt token is called 'jws'
        final Key publicKey         = localKeyStore.getSigningKeySetup().publicKey;

        assertEquals(email, Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody().getSubject());
    }

    @Test
    public void parseClaimsJws_shouldNotThrowAnyException_whenAllIsSetProperly() throws Exception {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final String email          = "hellomoto@xmal.com";
        final String jws            = jwtService.createJws(email);
        final Key publicKey         = localKeyStore.getSigningKeySetup().publicKey;

        assertDoesNotThrow(() -> Jwts.parserBuilder().setSigningKey(publicKey).requireSubject(email).build().parseClaimsJws(jws));
    }

    @Test
    public void parseClaimsJws_shouldThrowSignatureException_whenDifferentKeyIsUsed() throws Exception {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final String email          = "hellomoto@xmal.com";
        final String jws            = jwtService.createJws(email);
        final Key differentKey      = new KeyPairWithUUID().keyPair.getPublic();

        assertThrows(SignatureException.class, () -> Jwts.parserBuilder().setSigningKey(differentKey).requireSubject(email).build().parseClaimsJws(jws));
    }

    @Test
    public void parseClaimsJws_shouldThrowIncorrectClaimException_whenRequiredSubjectIsNotThere() throws Exception {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final String email          = "hellomoto@xmal.com";
        final String jws            = jwtService.createJws(email);
        final Key publicKey         = localKeyStore.getSigningKeySetup().publicKey;

        assertThrows(IncorrectClaimException.class, () -> Jwts.parserBuilder().setSigningKey(publicKey).requireSubject("moooooooooooo").build().parseClaimsJws(jws));
    }

    @Test
    public void getSubject_shouldReturnEmailAsSubject() throws Exception {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final String email          = "hellomoto@xmal.com";
        final String jws            = jwtService.createJws(email); // a signed jwt token is called 'jws'
        final Key publicKey         = localKeyStore.getSigningKeySetup().publicKey;

        assertEquals(email, Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody().getSubject());
    }

    @Test
    public void createJwsWithParseClaims_shouldReturnClaimsFromMap() throws Exception {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final Key publicKey         = localKeyStore.getSigningKeySetup().publicKey;
        final String email          = "hellomoto@xmal.com";
        final Map<String,Object> m  = new HashMap<>();
        m.put("claimOne", "one");
        m.put("claimTwo", 2);
        m.put("claimThree", 3.2);


        final String jws        = jwtService.createJws(email, m);
        final Claims claims     = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody();
        final String claimOne   = (String)  claims.get("claimOne");
        final Integer claimTwo  = (Integer) claims.get("claimTwo");
        final Double claimThree = (Double)  claims.get("claimThree");


        assertEquals("one", claimOne);
        assertEquals(2, claimTwo);
        assertEquals(3.2, claimThree);
    }

    @Test
    public void expMinusIatShouldBeEqualToJwtTTL() throws Exception {
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final PublicKey publicKey   = localKeyStore.getSigningKeySetup().publicKey;
        final Integer jwtTTL        = Integer.valueOf((String) configLoader.getAppProperties().get(APP_ENUM.APP_JWT_TTL.key));
        final String email          = "hellomoto@xmal.com";


        final String jws           = jwtService.createJws(email);
        final Claims claims        = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody();
        final Integer iat          = (Integer) claims.get("iat");
        final Integer exp          = (Integer) claims.get("exp");


        assertEquals(jwtTTL, exp - iat);
    }
}
