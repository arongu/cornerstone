package cornerstone.workflow.webapp.services.ssl_key_service;

import java.util.UUID;

public interface SSLKeyServiceInterface {
    int savePublicKeyToDB(final UUID uuid, final String nodeName, final int ttl, final String base64key ) throws SSLKeyServiceException;
}
