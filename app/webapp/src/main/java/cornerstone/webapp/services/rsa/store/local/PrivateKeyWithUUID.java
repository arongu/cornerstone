package cornerstone.webapp.services.rsa.store.local;

import java.security.PrivateKey;
import java.util.UUID;

public class PrivateKeyWithUUID {
    public final UUID uuid;
    public final PrivateKey privateKey;

    public PrivateKeyWithUUID(UUID uuid, PrivateKey privateKey) {
        this.uuid = uuid;
        this.privateKey = privateKey;
    }
}
