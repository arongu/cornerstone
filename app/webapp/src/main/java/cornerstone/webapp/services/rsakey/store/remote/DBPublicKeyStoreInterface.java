package cornerstone.webapp.services.rsakey.store.remote;

import cornerstone.webapp.services.rsakey.common.PublicKeyData;

import java.util.NoSuchElementException;
import java.util.UUID;

public interface DBPublicKeyStoreInterface {
    int addPublicKey(final UUID uuid, final String node_name, final int ttl, final String base64_key ) throws DBPublicKeyStoreException;
    int removePublicKey(final UUID uuid) throws DBPublicKeyStoreException;
    PublicKeyData getPublicKey(final UUID uuid) throws DBPublicKeyStoreException, NoSuchElementException;
}
