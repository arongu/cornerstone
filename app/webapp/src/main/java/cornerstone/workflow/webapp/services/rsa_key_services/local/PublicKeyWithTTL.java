package cornerstone.workflow.webapp.services.rsa_key_services.local;

import java.security.PublicKey;

public class PublicKeyWithTTL {
    public final PublicKey publicKey;
    public final int ttl;

    public PublicKeyWithTTL(final PublicKey publicKey, final int ttl) {
        this.ttl = ttl;
        this.publicKey = publicKey;
    }
}
