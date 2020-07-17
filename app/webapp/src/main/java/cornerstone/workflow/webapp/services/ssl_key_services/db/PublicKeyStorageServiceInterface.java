package cornerstone.workflow.webapp.services.ssl_key_services.db;

import java.util.UUID;

public interface PublicKeyStorageServiceInterface {
    int savePublicKeyToDB(final UUID uuid, final String nodeName, final int ttl, final String base64key ) throws PublicKeyStorageServiceException;
    int deletePublicKeyFromDB(final UUID uuid) throws PublicKeyStorageServiceException;
//    int deletePublicKeysFromDB(final UUID[] uuids);
}
