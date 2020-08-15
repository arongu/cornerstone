package cornerstone.webapp.service.jwt;

import cornerstone.webapp.config.ConfigLoader;
import cornerstone.webapp.service.rsa.rotation.KeyPairWithUUID;
import cornerstone.webapp.service.rsa.store.local.LocalKeyStore;
import cornerstone.webapp.service.rsa.store.local.LocalKeyStoreImpl;
import io.jsonwebtoken.Claims;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonwebtokenSecurityTest {
    private static ConfigLoader  configLoader;
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

    /*        x
        algo key issuer subject claims iat exp
        forging a jws with own key
    */
    @Test
    public void parseClaims_shouldThrowSignatureException_whenKeyIsTempered() {
        final String subject                          = "hellomoto@mail.com";
        final JWTService jwtService                   = new JWTServiceImpl(configLoader, localKeyStore);
        final Key privateKey                          = localKeyStore.getLiveKeys().privateKey;
        final Key publicKey                           = localKeyStore.getLiveKeys().publicKey;
        final String jws                              = jwtService.createJws(subject);
        final Claims extractClaims                    = jsonwebtokenTestHelper.extractClaims(jws, privateKey);
        final UUID keyId                              = UUID.fromString((String) extractClaims.get("keyId"));


        final KeyPair temperingKeyPair                = new KeyPairWithUUID().keyPair;
        final LocalKeyStore temperingLocalKeyStore    = new LocalKeyStoreImpl();
        temperingLocalKeyStore.setLiveKeys(keyId, temperingKeyPair.getPrivate(), temperingKeyPair.getPublic());
        final JWTService temperingJwtService          = new JWTServiceImpl(configLoader, temperingLocalKeyStore);
        final String temperedJws                      = temperingJwtService.createJws(subject);


        assertEquals(subject, Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody().getSubject());
        assertThrows(SignatureException.class, () -> Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(temperedJws).getBody().getSubject());
    }

    /*                     x
        algo key issuer subject claims iat exp
     */
    @Test
    public void parseClaims_shouldThrowSignatureException_whenSubjectIsTempered() {
        final String subject         = "hellomoto@xmal.com";
        final String temperedSubject = "haxor@mail.com";
        final JWTService jwtService  = new JWTServiceImpl(configLoader, localKeyStore);
        final PublicKey publicKey    = localKeyStore.getLiveKeys().publicKey;
        final String jws             = jwtService.createJws(subject);


        // Forging jws
        final Base64.Decoder decoder    = Base64.getDecoder();
        final String[] parsed           = jws.split("\\.");
        final String header             = parsed[0];
        final String payload            = new String(decoder.decode(parsed[1]));
        final String signature          = parsed[2];
        // tempered payload
        final String temperedPayload    = payload.replaceFirst(subject, temperedSubject);
        final String b64temperedPayload = new String(Base64.getEncoder().encode(temperedPayload.getBytes()));
        final String temperedJWS        = header + "." + b64temperedPayload + "." + signature;


        // valid, tempered
        assertEquals(subject, Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody().getSubject());
        assertThrows(SignatureException.class, () -> Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(temperedJWS).getBody().getSubject());
    }
}
