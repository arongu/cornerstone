package cornerstone.webapp.services.rsa.rotation;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.KeyPair;
import java.util.UUID;

public class KeyPairWithUUID {
    public final KeyPair keyPair;
    public final UUID uuid;

    public KeyPairWithUUID() {
        keyPair = Keys.keyPairFor(SignatureAlgorithm.RS512);
        uuid = java.util.UUID.randomUUID();
    }

    public KeyPairWithUUID(final java.security.KeyPair keyPair, final UUID uuid) {
        this.keyPair = keyPair;
        this.uuid = uuid;
    }
}
