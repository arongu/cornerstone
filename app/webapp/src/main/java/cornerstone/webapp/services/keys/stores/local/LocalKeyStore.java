package cornerstone.webapp.services.keys.stores.local;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

public interface LocalKeyStore {
    /**
     * @return Returns UUIDs of the cached public keys.
     */
    Set<UUID> getPublicKeyUUIDs();

    /**
     * Returns a public key based on UUID.
     * @param uuid UUID of the key.
     * @return Returns the PublicKey object based on the UUID.
     * @throws NoSuchElementException Thrown when the key could not be found based on the UUID.
     */
    PublicKey getPublicKey(final UUID uuid) throws NoSuchElementException;

    /**
     * Adds a public key to the store.
     * @param uuid UUID of the key.
     * @param publicKey The public key of the key.
     */
    void addPublicKey(final UUID uuid, final PublicKey publicKey);

    /**
     * Adds a public key to the store.
     * @param uuid UUID of the key.
     * @param base64KeyString Public key in base64 format (useful when data is coming from DB).
     * @throws NoSuchAlgorithmException Thrown when KeyFactory cannot provide "RSA" instance. (Never should occur, unless underlying Java changes.)
     * @throws InvalidKeySpecException Thrown when keySpec is invalid. (Never should occur, unless underlying Java changes.)
     */
    void addPublicKey(final UUID uuid, final String base64KeyString) throws NoSuchAlgorithmException, InvalidKeySpecException;

    /**
     * Deletes a public key from the local store based on UUID.
     * @param uuid UUID of the key.
     */
    void deletePublicKey(final UUID uuid);

    /**
     * Deletes a list of public keys based on the passed UUIDs.
     * @param uuidsToBeRemoved Takes a list of UUIDs and deletes any matching keys from the store.
     */
    void deletePublicKeys(final List<UUID> uuidsToBeRemoved);

    /**
     * Keeps only the keys with listed UUIDs, deletes the rest of it.
     * Removes any key from the store which is not present in the passed UUIDs.
     * @param toBeKept List of the key UUIDs that needs to be kept, the rest will be deleted.
     */
    void keepOnly(final List<UUID> toBeKept);

    /**
     * Sets the keys used for JWT/JWS signing.
     * @param uuid The UUID of the key.
     * @param privateKey Private key.
     * @param publicKey Public key.
     */
    void setSigningKeys(final UUID uuid, final PrivateKey privateKey, final PublicKey publicKey);

    /**
     * Returns the keys and the corresponding UUID used for signing JWT/JWS.
     * @return Returns public key, private key, UUID.
     * @throws SigningKeysException Thrown the keys are not set properly.
     */
    SigningKeys getSigningKeys() throws SigningKeysException;
    void resetAll();
}
