package cornerstone.workflow.webapp.services.ssl_key_services.db;

import cornerstone.workflow.webapp.services.ssl_key_services.PublicKeyData;

import java.util.UUID;

public interface PublicKeyStorageServiceInterface {
    int addPublicKey(final UUID uuid, final String node_name, final int ttl, final String base64_key ) throws PublicKeyStorageServiceException;
    int removePublicKey(final UUID uuid) throws PublicKeyStorageServiceException;
    PublicKeyData getPublicKey(final UUID uuid) throws PublicKeyStorageServiceException;
}
