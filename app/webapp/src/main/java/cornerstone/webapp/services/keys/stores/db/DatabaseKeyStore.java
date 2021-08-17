package cornerstone.webapp.services.keys.stores.db;

import cornerstone.webapp.services.keys.common.PublicKeyData;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public interface DatabaseKeyStore {
    /**
     * Adds a new public key to the database.
     * @param uuid The UUID of the public key.
     * @param node_name Name of the node.
     * @param ttl TTL of the public key (DB will calculate it NOW() + TTL.
     * @return Returns the number of the deleted keys, this should be always 1 or 0.
     * @throws DatabaseKeyStoreException When SQL exception occurs.
     */
    int addPublicKey(final UUID uuid, final String node_name, final int ttl, final String base64_key) throws DatabaseKeyStoreException;

    /**
     * Deletes a public key from the database based on the given UUID.
     * @return Returns the number of the deleted keys, this should be always 1 or 0.
     * @throws DatabaseKeyStoreException When SQL exception occurs.
     */
    int deletePublicKey(final UUID uuid) throws DatabaseKeyStoreException;

    /**
     * Fetches the database for a public key.
     * @param uuid The UUID of the desired public key.
     * @return Matching public key with UUID as a compound object. (node_name, ttl, creation_ts, expire_ts, base64_key)
     * @throws DatabaseKeyStoreException When SQL exception occurs.
     * @throws NoSuchElementException When the key does not exist.
     */
    PublicKeyData getPublicKey(final UUID uuid) throws DatabaseKeyStoreException, NoSuchElementException;

    /**
     * Fetches the database for the live public keys.
     * @return Live public keys as a list.
     * @throws DatabaseKeyStoreException When SQL exception occurs.
     */
    List<PublicKeyData> getLivePublicKeys() throws DatabaseKeyStoreException;

    /**
     * Fetches the database for the live public key UUIDs.
     * @return List<UUID> Live key UUIDs as a list.
     * @throws DatabaseKeyStoreException When SQL exception occurs.
     */
    List<UUID> getLivePublicKeyUUIDs() throws DatabaseKeyStoreException;

    /**
     * Fetches the database for the expired keys UUIDs.
     * @return List<UUID> List of the expired key UUIDs.
     * @throws DatabaseKeyStoreException When SQL exception occurs.
     */
    List<UUID> getExpiredPublicKeyUUIDs() throws DatabaseKeyStoreException;

    /**
     * Executes an SQL query to remove all the expired key. (expired_ts < NOW() (NOW() is at postgres, not calculated here)
     * @return The number of deleted expired keys.
     * @throws DatabaseKeyStoreException When SQL exception occurs.
     */
    int deleteExpiredPublicKeys() throws DatabaseKeyStoreException;
}
