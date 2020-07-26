package cornerstone.webapp.services.rsa.store.local;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

public interface LocalKeyStoreInterface {
    Set<UUID> getUUIDs();
    PublicKey getPublicKey(final UUID uuid) throws NoSuchElementException;
    void addPublicKey(final UUID uuid, final PublicKey publicKey);
    void removePublicKey(final UUID uuid);
    void removePublicKeys(final List<UUID> uuidsToBeRemoved);
    void keepOnly(final List<UUID> toBeKept);

    void setKeysForSigning(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey);
    PrivateKeyWithUUID getPrivateKey() throws NoSuchElementException;
    void dropPrivateKey();
    void dropEverything();
}
