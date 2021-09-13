package cornerstone.webapp.services.keys.stores.manager;

import cornerstone.webapp.services.keys.stores.db.DatabaseKeyStoreException;
import cornerstone.webapp.services.keys.stores.local.SigningKeys;
import cornerstone.webapp.services.keys.stores.local.SigningKeysException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public interface KeyManager {
    void          addPublicKey            (final UUID uuid, final String base64_key)                                throws KeyManagerException;
    void          addPublicKey            (final UUID uuid, final PublicKey publicKey)                              throws KeyManagerException;
    void          deletePublicKey         (final UUID uuid)                                                         throws KeyManagerException;
    List<UUID>    getExpiredPublicKeyUUIDs()                                                                        throws DatabaseKeyStoreException;
    List<UUID>    getLivePublicKeyUUIDs   ()                                                                        throws DatabaseKeyStoreException;
    PublicKey     getPublicKey            (final UUID uuid)                                                         throws KeyManagerException, NoSuchElementException;
    SigningKeys   getSigningKeys          ()                                                                        throws SigningKeysException;
    int           removeExpiredKeys       ()                                                                        throws DatabaseKeyStoreException;
    void          setSigningKeys          (final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey) throws KeyManagerException;
    void          syncLiveKeys            ()                                                                        throws DatabaseKeyStoreException;
}
