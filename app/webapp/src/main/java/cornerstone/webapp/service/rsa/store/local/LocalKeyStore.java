package cornerstone.webapp.service.rsa.store.local;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

public interface LocalKeyStore {
    Set<UUID> getPublicKeyUUIDs();
    PublicKey getPublicKey(final UUID uuid) throws NoSuchElementException;

    void addPublicKey(final UUID uuid, final PublicKey publicKey);
    void deletePublicKey(final UUID uuid);
    void deletePublicKeys(final List<UUID> uuidsToBeRemoved);
    void sync(final List<UUID> toBeKept);

    void setLiveKeyData(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey);
    LiveKeyData getLiveKeyData() throws NoSuchElementException;
    void dropEverything();
}
