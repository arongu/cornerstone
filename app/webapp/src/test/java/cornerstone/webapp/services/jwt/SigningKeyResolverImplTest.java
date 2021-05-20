package cornerstone.webapp.services.jwt;

import cornerstone.webapp.services.keys.rotation.KeyPairWithUUID;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStoreImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SigningKeyResolverImplTest {
    private static LocalKeyStore localKeyStore;

    private static KeyPairWithUUID keyA;
    private static KeyPairWithUUID keyB;

    @BeforeAll
    public static void setup() {
        keyA = new KeyPairWithUUID();
        keyB = new KeyPairWithUUID();

        localKeyStore = new LocalKeyStoreImpl();
        localKeyStore.addPublicKey(keyA.uuid, keyA.keyPair.getPublic());
        localKeyStore.addPublicKey(keyB.uuid, keyB.keyPair.getPublic());
    }

    @Test
    public void resolveSigningKey_shouldReturnKey_whenUUIDMatches() {
        final SigningKeyResolver signingKeyResolver = new SigningKeyResolverImpl(localKeyStore);
        final JwsHeader<?> jwsHeader                = Jwts.jwsHeader().setKeyId(keyA.uuid.toString());

        final Key key = signingKeyResolver.resolveSigningKey(jwsHeader, (Claims) null);

        assertEquals(key, keyA.keyPair.getPublic());
    }

    @Test
    public void resolveSigningKey_shouldThrowNoSuchElementException_whenUUIDdoesNotExist() {
        final SigningKeyResolver signingKeyResolver = new SigningKeyResolverImpl(localKeyStore);
        final JwsHeader<?> jwsHeader                = Jwts.jwsHeader().setKeyId(UUID.randomUUID().toString());

        assertThrows(NoSuchElementException.class, () -> signingKeyResolver.resolveSigningKey(jwsHeader, (Claims) null));
    }
}
