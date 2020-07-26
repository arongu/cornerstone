package cornerstone.webapp.services.rsa.store.db;

import cornerstone.webapp.services.rsa.common.PublicKeyData;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public interface DbPublicKeyStoreInterface {
    int addPublicKey(final UUID uuid, final String node_name, final int ttl, final String base64_key) throws DbPublicKeyStoreException;
    int deletePublicKey(final UUID uuid) throws DbPublicKeyStoreException;
    PublicKeyData getPublicKey(final UUID uuid) throws DbPublicKeyStoreException, NoSuchElementException;
    List<PublicKeyData> getActivePublicKeys() throws DbPublicKeyStoreException;
    List<UUID> getActiveKeyUUIDs() throws DbPublicKeyStoreException;
    List<UUID> getExpiredKeyUUIDs() throws DbPublicKeyStoreException;
    int deleteExpiredPublicKeys() throws DbPublicKeyStoreException;
}
