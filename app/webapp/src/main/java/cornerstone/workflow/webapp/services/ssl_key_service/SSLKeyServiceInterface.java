package cornerstone.workflow.webapp.services.ssl_key_service;

import java.security.KeyPair;
import java.util.UUID;

public interface SSLKeyServiceInterface {
    int savePublicKeyToDB(final UUID uuid, final String nodeName, final String base64key, final int ttl) throws SSLKeyServiceException;
}
