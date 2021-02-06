package cornerstone.webapp.services.keys.stores.local;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

public class SigningKeys {
    public final UUID uuid;
    public final PrivateKey privateKey;
    public final PublicKey publicKey;

    public SigningKeys(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey) {
        this.uuid = uuid;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }
}
