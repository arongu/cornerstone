package cornerstone.webapp.services.rsa.store.local;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

public interface LocalKeyStoreInterface {
    PublicKey getPublicKey(final UUID uuid) throws NoSuchElementException;
    void addPublicKey(final UUID uuid, final PublicKey publicKey);
    void removePublicKey(final UUID uuid);
    void removePublicKeys(final List<UUID> uuidsToBeRemoved);
    Set<UUID> getUUIDs();

    void setSigningKey(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey);
    PrivateKeyWithUUID getSigningKey() throws NoSuchElementException;
    void unsetSigningKey();
    void resetAll();
}
