package cornerstone.webapp.service.rsa.store.db;

import cornerstone.webapp.service.rsa.common.PublicKeyData;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public interface PublicKeyStore {
    int addKey(final UUID uuid, final String node_name, final int ttl, final String base64_key) throws PublicKeyStoreException;
    int deleteKey(final UUID uuid) throws PublicKeyStoreException;
    PublicKeyData getKey(final UUID uuid) throws PublicKeyStoreException, NoSuchElementException;
    List<PublicKeyData> getLiveKeys() throws PublicKeyStoreException;
    List<UUID> getLiveKeyUUIDs() throws PublicKeyStoreException;
    List<UUID> getExpiredKeyUUIDs() throws PublicKeyStoreException;
    int deleteExpiredKeys() throws PublicKeyStoreException;
}
