package cornerstone.webapp.services.keys.stores.manager;

import cornerstone.webapp.services.keys.stores.local.SigningKeys;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

public interface KeyManager {
    void          addPublicKey(final UUID uuid, final String base64_key) throws KeyManagerException;
    void          addPublicKey(final UUID uuid, final PublicKey publicKey) throws KeyManagerException;
    void          deletePublicKey(final UUID uuid) throws KeyManagerException;
    PublicKey     getPublicKey(final UUID uuid) throws KeyManagerException;
    SigningKeys   getSigningKeys() throws KeyManagerException;
    void          setSigningKeys() throws KeyManagerException;
    void          sync(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey) throws KeyManagerException;
}
