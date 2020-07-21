package cornerstone.webapp.services.rsakey.storage.db;

import cornerstone.webapp.services.rsakey.common.PublicKeyData;

import java.util.NoSuchElementException;
import java.util.UUID;

public interface DatabasePublicKeyStoreInterface {
    int addPublicKey(final UUID uuid, final String node_name, final int ttl, final String base64_key ) throws DatabasePublicKeyStoreException;
    int removePublicKey(final UUID uuid) throws DatabasePublicKeyStoreException;
    PublicKeyData getPublicKey(final UUID uuid) throws DatabasePublicKeyStoreException, NoSuchElementException;
}
