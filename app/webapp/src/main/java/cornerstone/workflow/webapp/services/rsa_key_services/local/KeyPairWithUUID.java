package cornerstone.workflow.webapp.services.rsa_key_services.local;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.KeyPair;
import java.util.UUID;

public class KeyPairWithUUID {
    public final KeyPair keyPair;
    public final UUID uuid;

    public KeyPairWithUUID() {
        this.keyPair = Keys.keyPairFor(SignatureAlgorithm.RS512);
        this.uuid = java.util.UUID.randomUUID();
    }

    public KeyPairWithUUID(final KeyPair keyPair, final UUID uuid) {
        this.keyPair = keyPair;
        this.uuid = uuid;
    }
}
