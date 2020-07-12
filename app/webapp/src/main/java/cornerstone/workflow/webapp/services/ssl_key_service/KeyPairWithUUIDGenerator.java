package cornerstone.workflow.webapp.services.ssl_key_service;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.KeyPair;
import java.util.UUID;

public class KeyPairWithUUIDGenerator {
    public static KeyPairWithUUID generateKeyPairWithUUID() {
        final KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS512);
        final UUID uuid = java.util.UUID.randomUUID();

        return new KeyPairWithUUID(keyPair, uuid);
    }
}
