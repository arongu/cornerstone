package cornerstone.webapp.service.rsa.store.local;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

public class LiveKeyData {
    public final UUID uuid;
    public final PrivateKey privateKey;
    public final PublicKey publicKey;

    public LiveKeyData(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey) {
        this.uuid = uuid;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }
}
