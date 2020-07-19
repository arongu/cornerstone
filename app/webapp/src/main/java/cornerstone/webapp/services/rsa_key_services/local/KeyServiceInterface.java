package cornerstone.webapp.services.rsa_key_services.local;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.NoSuchElementException;
import java.util.UUID;

public interface KeyServiceInterface {
    void setSigningKeyTTL(final int ttl);
    void resetSigningKey();
    void storePublicKey(final UUID uuid, final String base64_key);
    void storePublicKey(final UUID uuid, final PublicKey publicKey);
    PrivateKey getSigningKey();
    PublicKey getPublicKey(final UUID uuid) throws NoSuchElementException;
}
