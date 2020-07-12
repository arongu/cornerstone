package cornerstone.workflow.webapp.services.ssl_key_service;

import java.security.KeyPair;
import java.util.UUID;

public interface SSLKeyServiceInterface {
    int savePublicKeyToDB(final String base64pubkey, final String created_by, final UUID uuid) throws Exception;
}
