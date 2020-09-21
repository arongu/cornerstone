package cornerstone.webapp.services.rsa.store.local;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

public interface LocalKeyStore {
    Set<UUID> getPublicKeyUUIDs();
    PublicKey getPublicKey(final UUID uuid) throws NoSuchElementException;

    void addPublicKey(final UUID uuid, final PublicKey publicKey);
    void addPublicKey(final UUID uuid, final String base64KeyString) throws NoSuchAlgorithmException, InvalidKeySpecException;
    void deletePublicKey(final UUID uuid);
    void deletePublicKeys(final List<UUID> uuidsToBeRemoved);
    void sync(final List<UUID> toBeKept);

    void setupSigning(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey);
    SigningKeySetup getSigningKeySetup() throws SigningKeySetupException;
    void resetAll();
}
