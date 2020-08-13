package cornerstone.webapp.service.jwt;

import cornerstone.webapp.configuration.ConfigurationLoader;
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
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JsonwebtokenLibTests {
    private static ConfigurationLoader configurationLoader;
    private static LocalKeyStore localKeyStore;

    @BeforeAll
    public static void init() {
        final String test_files_dir = "../../_test_config/";
        final String keyFile  = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            configurationLoader = new ConfigurationLoader(keyFile, confFile);

            localKeyStore = new LocalKeyStoreImpl();
            final KeyPairWithUUID kp = new KeyPairWithUUID();
            localKeyStore.setLiveKeyData(kp.uuid, kp.keyPair.getPrivate(), kp.keyPair.getPublic());

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getSubjectTests() {
        final JWTService jwtService = new JWTServiceImpl(configurationLoader, localKeyStore);
        final String email = "hellomoto@xmal.com";
        final String jws = jwtService.issueJWT(email); // a signed jwt token is called 'jws'

        assertEquals(email, Jwts.parserBuilder().setSigningKey(localKeyStore.getLiveKeyData().publicKey).build().parseClaimsJws(jws).getBody().getSubject());
        assertNotEquals(Jwts.parserBuilder().setSigningKey(localKeyStore.getLiveKeyData().publicKey).build().parseClaimsJws(jws).getBody().getSubject(), "nono");
    }

    @Test
    public void keyAndClaimTests() {
        final JWTService jwtService = new JWTServiceImpl(configurationLoader, localKeyStore);
        final String email = "hellomoto@xmal.com";
        final String jws = jwtService.issueJWT(email); // a signed jwt token is called 'jws'

        // should not throw any exceptions when
        // key is valid and
        // claim is present
        assertDoesNotThrow(() -> {
            final PublicKey validKey = localKeyStore.getLiveKeyData().publicKey;
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
            final PublicKey validKey = localKeyStore.getLiveKeyData().publicKey;
            Jwts.parserBuilder().setSigningKey(validKey).requireSubject("moooooooooooo").build().parseClaimsJws(jws);
        });
    }

    @Test
    public void requireIssuerTest() {
        final JWTService jwtService = new JWTServiceImpl(configurationLoader, localKeyStore);
        final String email = "hellomoto@xmal.com";
        final String jws = jwtService.issueJWT(email); // a signed jwt token is called 'jws'

        assertEquals(email, Jwts.parserBuilder().setSigningKey(localKeyStore.getLiveKeyData().publicKey).build().parseClaimsJws(jws).getBody().getSubject());
        assertThrows(SignatureException.class, () -> {
            final KeyPairWithUUID differentKeys = new KeyPairWithUUID();
            Jwts.parserBuilder().setSigningKey(differentKeys.keyPair.getPublic()).build().parseClaimsJws(jws);
        });
    }

    @Test
    public void claimTests() {
        final JWTService jwtService = new JWTServiceImpl(configurationLoader, localKeyStore);
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
            final String jws = jwtService.issueJWT(email, m);
            Jwts.parserBuilder().setSigningKey(invalidKey).build().parseClaimsJws(jws);
        });


        // should not throw exception
        // when claims are there and
        // key is valid
        assertDoesNotThrow(() -> {
            final String jws = jwtService.issueJWT(email, m);
            final PublicKey validKey = localKeyStore.getLiveKeyData().publicKey;
            Jwts.parserBuilder().setSigningKey(validKey).build().parseClaimsJws(jws);
        });


        // check claims
        final String jws = jwtService.issueJWT(email, m);
        final PublicKey validKey = localKeyStore.getLiveKeyData().publicKey;
        final Claims claims = Jwts.parserBuilder().setSigningKey(validKey).build().parseClaimsJws(jws).getBody();
        assertEquals("one", claims.get("claimOne"));
        assertEquals(2, claims.get("claimTwo"));
        assertEquals(3.2, claims.get("claimThree"));
    }

    @Test
    void x(){
        final JWTService jwtService = new JWTServiceImpl(configurationLoader, localKeyStore);
        final String email = "hellomoto@xmal.com";
        final Map<String,Object> m = new HashMap<>();
        m.put("claimOne", "one");
        m.put("claimTwo", 2);
        m.put("claimThree", 3.2);


        final String jws = jwtService.issueJWT(email, m);
        final PublicKey validKey = localKeyStore.getLiveKeyData().publicKey;
        final Claims claims = Jwts.parserBuilder().setSigningKey(validKey).build().parseClaimsJws(jws).getBody();
        System.out.println(claims);
    }
}

// iat - exp match
// exp -
