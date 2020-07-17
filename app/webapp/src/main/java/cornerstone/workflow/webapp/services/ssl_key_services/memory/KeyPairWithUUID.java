package cornerstone.workflow.webapp.services.ssl_key_services.memory;

import java.security.KeyPair;
import java.util.UUID;

public class KeyPairWithUUID {
    public final KeyPair keyPair;
    public final UUID uuid;

    public KeyPairWithUUID(final KeyPair keyPair, final UUID uuid) {
        this.keyPair = keyPair;
        this.uuid = uuid;
    }
}
