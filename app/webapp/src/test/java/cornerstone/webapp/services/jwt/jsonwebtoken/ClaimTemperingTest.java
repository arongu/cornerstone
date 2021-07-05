package cornerstone.webapp.services.jwt.jsonwebtoken;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.services.jwt.JWTService;
import cornerstone.webapp.services.jwt.JWTServiceImpl;
import cornerstone.webapp.services.keys.rotation.KeyPairWithUUID;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStoreImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ClaimTemperingTest {
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

    /*        x
        algo key issuer subject claims iat exp
        forging a jws with own key
    */
    @Test
    public void parseClaims_shouldThrowSignatureException_whenKeyIsTempered() throws Exception {
        final String subject        = "hellomoto@mail.com";
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final Key publicKey         = localKeyStore.getSigningKeys().publicKey;
        final String jws            = jwtService.createJws(subject);

        // forging jws
        final KeyPair forgedKeyPair                = new KeyPairWithUUID().keyPair;
        final LocalKeyStore forgedLocalKeyStore    = new LocalKeyStoreImpl();
        forgedLocalKeyStore.setSigningKeys(localKeyStore.getSigningKeys().uuid, forgedKeyPair.getPrivate(), forgedKeyPair.getPublic());
        // sign with the new forged key, using the original data, and uuid
        final JWTService forgedJwtService          = new JWTServiceImpl(configLoader, forgedLocalKeyStore);
        final String forgedJws                     = forgedJwtService.createJws(subject);


        assertEquals(subject,                        Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody().getSubject());
        assertThrows(SignatureException.class, () -> Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(forgedJws).getBody().getSubject());
    }

    /*             x
        algo key issuer subject claims iat exp
    */
    @Test
    public void parseClaims_shouldThrowSignatureException_whenIssuerIsTempered() throws Exception {
        final String subject        = "hellomoto@xmal.com";
        final JWTService jwtService = new JWTServiceImpl(configLoader, localKeyStore);
        final PublicKey publicKey   = localKeyStore.getSigningKeys().publicKey;
        final String jws            = jwtService.createJws(subject);
        final String issuer         = configLoader.getAppProperties().getProperty(APP_ENUM.APP_NODE_NAME.key);


        // forging jws
        final Base64.Decoder decoder    = Base64.getDecoder();
        final String[] parsed           = jws.split("\\.");
        final String header             = parsed[0];
        final String payload            = new String(decoder.decode(parsed[1]));
        final String signature          = parsed[2];
        final String temperedIssuer     = "haxor-node";
        // tempered payload
        final String temperedPayload    = payload.replaceFirst(issuer, temperedIssuer);
        final String b64temperedPayload = new String(Base64.getEncoder().encode(temperedPayload.getBytes()));
        final String temperedJWS        = header + "." + b64temperedPayload + "." + signature;


        // valid, tempered
        assertEquals(issuer, Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody().getIssuer());
        assertThrows(SignatureException.class, () -> Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(temperedJWS).getBody().getIssuer());
    }

    /*                     x
        algo key issuer subject claims iat exp
     */
    @Test
    public void parseClaims_shouldThrowSignatureException_whenSubjectIsTempered() throws Exception {
        final String subject         = "hellomoto@xmal.com";
        final String temperedSubject = "haxor@mail.com";
        final JWTService jwtService  = new JWTServiceImpl(configLoader, localKeyStore);
        final PublicKey publicKey    = localKeyStore.getSigningKeys().publicKey;
        final String jws             = jwtService.createJws(subject);


        // forging jws
        final Base64.Decoder decoder    = Base64.getDecoder();
        final String[] parsed           = jws.split("\\.");
        final String header             = parsed[0];
        final String payload            = new String(decoder.decode(parsed[1]));
        final String signature          = parsed[2];
        // tempered payload
        final String temperedPayload    = payload.replaceFirst(subject, temperedSubject);
        final String b64temperedPayload = new String(Base64.getEncoder().encode(temperedPayload.getBytes()));
        final String temperedJws        = header + "." + b64temperedPayload + "." + signature;


        // valid, tempered
        assertEquals(subject, Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody().getSubject());
        assertThrows(SignatureException.class, () -> Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(temperedJws).getBody().getSubject());
    }

    /*                             x
        algo key issuer subject claims iat exp
    */
    @Test
    public void parseClaims_shouldThrowSignatureException_whenClaimIsTempered() throws Exception {
        final String subject         = "hellomoto@xmal.com";
        final JWTService jwtService  = new JWTServiceImpl(configLoader, localKeyStore);
        final PublicKey publicKey    = localKeyStore.getSigningKeys().publicKey;
        final Map<String, Object> m  = new HashMap<>();
        m.put("myClaim", "Rome");
        final String jws             = jwtService.createJws(subject, m);


        // forging jws
        final Base64.Decoder decoder    = Base64.getDecoder();
        final String[] parsed           = jws.split("\\.");
        final String header             = parsed[0];
        final String payload            = new String(decoder.decode(parsed[1]));
        final String signature          = parsed[2];
        // tempered payload
        final String temperedPayload    = payload.replaceFirst("\"myClaim\":\"Rome\"", "\"myClaim\":\"London\"");
        final String b64temperedPayload = new String(Base64.getEncoder().encode(temperedPayload.getBytes()));
        final String temperedJWS        = header + "." + b64temperedPayload + "." + signature;


        // valid, tempered
        assertEquals("Rome", Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody().get("myClaim"));
        assertThrows(SignatureException.class, () -> Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(temperedJWS).getBody());
    }

    /*                                  x
        algo key issuer subject claims iat exp
    */
    @Test
    public void parseClaims_shouldThrowSignatureException_whenIatIsTempered() throws Exception {
        final String subject         = "hellomoto@xmal.com";
        final JWTService jwtService  = new JWTServiceImpl(configLoader, localKeyStore);
        final PublicKey publicKey    = localKeyStore.getSigningKeys().publicKey;
        final String jws             = jwtService.createJws(subject);


        // forging jws
        final Base64.Decoder decoder    = Base64.getDecoder();
        final String[] parsed           = jws.split("\\.");
        final String header             = parsed[0];
        final long iat                  = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody().getIssuedAt().getTime();
        final String iatStr             = String.valueOf(iat/1000);
        final long temperedIat          = iat + 5000;   // add 5 seconds to issued at
        final String temperedIatStr     = String.valueOf(temperedIat/1000);
        final String payload            = new String(decoder.decode(parsed[1]));
        final String signature          = parsed[2];
        // forged payload
        final String forgedPayload      = payload.replaceFirst(iatStr, temperedIatStr);
        System.out.println("payload      : " + payload);
        System.out.println("forgedPayload: " + forgedPayload);
        final String b64temperedPayload = new String(Base64.getEncoder().encode(forgedPayload.getBytes()));
        final String temperedJWS        = header + "." + b64temperedPayload + "." + signature;


        assertThrows(SignatureException.class, () -> Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(temperedJWS).getBody().getIssuedAt());
    }

    /*                                      x
        algo key issuer subject claims iat exp
    */
    @Test
    public void parseClaims_shouldThrowSignatureException_whenExpIsTempered() throws Exception {
        final String subject         = "hellomoto@xmal.com";
        final JWTService jwtService  = new JWTServiceImpl(configLoader, localKeyStore);
        final PublicKey publicKey    = localKeyStore.getSigningKeys().publicKey;
        final String jws             = jwtService.createJws(subject);


        // forging jws
        final Base64.Decoder decoder    = Base64.getDecoder();
        final String[] parsed           = jws.split("\\.");
        final String header             = parsed[0];
        final long exp                  = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody().getExpiration().getTime();
        final String expStr             = String.valueOf(exp/1000);
        final long temperedExp          = exp + 5000;   // add 5 seconds to exp
        final String temperedExpStr     = String.valueOf(temperedExp/1000);
        final String payload            = new String(decoder.decode(parsed[1]));
        final String signature          = parsed[2];
        // forged payload
        final String forgedPayload      = payload.replaceFirst(expStr, temperedExpStr);
        System.out.println("payload      : " + payload);
        System.out.println("forgedPayload: " + forgedPayload);
        final String b64temperedPayload = new String(Base64.getEncoder().encode(forgedPayload.getBytes()));
        final String temperedJWS        = header + "." + b64temperedPayload + "." + signature;


        assertThrows(SignatureException.class, () -> Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(temperedJWS).getBody().getExpiration());
    }
}
