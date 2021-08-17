package cornerstone.webapp.services.keys.stores.local;

import cornerstone.webapp.services.keys.rotation.KeyPairWithUUID;
import org.junit.jupiter.api.Test;

import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class LocalKeyStoreImplTest {
    @Test
    void crudTest_add_get_delete_get_uuids() {
        final LocalKeyStore localKeyStore = new LocalKeyStoreImpl();
        final KeyPairWithUUID kp          = new KeyPairWithUUID();
        final UUID uuid                   = kp.uuid;
        final PublicKey publicKey         = kp.keyPair.getPublic();
        // results
        final Set<UUID> retrieved_uuids;
        final PublicKey retrieved_public_key;


        localKeyStore.addPublicKey(uuid, publicKey);
        retrieved_public_key = localKeyStore.getPublicKey(uuid);
        retrieved_uuids      = localKeyStore.getPublicKeyUUIDs();


        assertEquals(publicKey, retrieved_public_key);
        assertTrue(retrieved_uuids.contains(uuid));
        assertEquals(1 , retrieved_uuids.size());
        // delete
        localKeyStore.deletePublicKey(kp.uuid);
        assertThrows(NoSuchElementException.class, () -> localKeyStore.getPublicKey(uuid));
        assertEquals(0, retrieved_uuids.size());
    }

    @Test
    public void crudTest_addDeletePublicKeys_shouldDeleteAllKeys() throws LocalKeyStoreException {
        final int to_be_created = 5;
        int number_of_throws    = 0;
        final LocalKeyStore localKeyStore = new LocalKeyStoreImpl();
        final Set<UUID> created_uuids     = new HashSet<>();


        // add - get
        for (int i = 0; i < to_be_created; i++) {
            final KeyPairWithUUID kp = new KeyPairWithUUID();
            final UUID uuid          = kp.uuid;
            final PublicKey pubKey   = kp.keyPair.getPublic();

            localKeyStore.addPublicKey(uuid, pubKey);
            created_uuids.add(uuid);
        }
        assertEquals(created_uuids, localKeyStore.getPublicKeyUUIDs());


        // delete
        localKeyStore.deletePublicKey(new ArrayList<>(created_uuids));
        for (final UUID uuid : created_uuids) {
            try {
                localKeyStore.getPublicKey(uuid);
            } catch (final NoSuchElementException e){
                number_of_throws++;
            }
        }


        assertEquals(to_be_created, number_of_throws);
        System.out.printf("to_be_created, no_such_element_throws: %d, %d\n", to_be_created, number_of_throws);
    }

    @Test
    void addPublicKey_shouldWorkWithX509EncodedKeySpec_whenAddingKeyFromBase64String() throws Exception {
        final LocalKeyStore localKeyStore;
        final KeyPairWithUUID kp              = new KeyPairWithUUID();
        final UUID uuid                       = kp.uuid;
        final PublicKey public_key            = kp.keyPair.getPublic();
        final byte[] ba_public_key            = kp.keyPair.getPublic().getEncoded();
        final String str_b64_public_key       = Base64.getEncoder().encodeToString(ba_public_key);
        // public key from base64 String -- using X509EncodedKeySpec
        final PublicKey retrieved_public_key;
        final String    retrieved_str_b64_public_key;


        localKeyStore                       = new LocalKeyStoreImpl();
        localKeyStore.addPublicKey(uuid, str_b64_public_key);
        retrieved_public_key                = localKeyStore.getPublicKey(uuid);
        retrieved_str_b64_public_key        = Base64.getEncoder().encodeToString(retrieved_public_key.getEncoded());


        assertEquals(public_key, retrieved_public_key);
        assertEquals(str_b64_public_key, retrieved_str_b64_public_key);
    }

    @Test
    void addPublicKey_shouldThrowException_whenBase64KeyIsMalformed() {
        assertThrows(InvalidKeySpecException.class, () -> {
            LocalKeyStore localKeyStore = new LocalKeyStoreImpl();
            localKeyStore.addPublicKey(UUID.randomUUID(), "ff");
        });
    }

    @Test
    void addPublicKey_shouldThrowException_whenBase64KeyIsMalformedB() {
        assertThrows(InvalidKeySpecException.class, () -> {
            LocalKeyStore localKeyStore = new LocalKeyStoreImpl();
            localKeyStore.addPublicKey(UUID.randomUUID(), "csacsi");
        });
    }

    @Test
    void addPublicKey_shouldThrowException_whenUuidIsNull() {
        assertThrows(InvalidKeySpecException.class, () -> {
            LocalKeyStore localKeyStore = new LocalKeyStoreImpl();
            localKeyStore.addPublicKey(null, "ff");
        });
    }

    @Test
    public void sync_shouldKeepToBeeKeptAndDeleteTheRest() throws LocalKeyStoreException {
        final int to_be_created = 5;
        final List<UUID> uuids = new ArrayList<>();
        final Map<UUID,PublicKey> map = new HashMap<>();
        final LocalKeyStore localKeyStore = new LocalKeyStoreImpl();
        // create data
        for (int i = 0; i < to_be_created; i++) {
            final KeyPairWithUUID kp = new KeyPairWithUUID();
            final UUID uuid = kp.uuid;
            final PublicKey pubKey = kp.keyPair.getPublic();

            localKeyStore.addPublicKey(uuid, pubKey);
            uuids.add(uuid);
            map.put(uuid, pubKey);
        }


        final List<UUID> uuids_to_be_kept = new LinkedList<>();
        uuids_to_be_kept.add(uuids.get(0));
        uuids_to_be_kept.add(uuids.get(2));
        uuids_to_be_kept.add(uuids.get(4));
        localKeyStore.keepOnlyPublicKeys(uuids_to_be_kept);


        assertEquals(3, localKeyStore.getPublicKeyUUIDs().size());
        assertEquals(map.get(uuids.get(0)), localKeyStore.getPublicKey(uuids.get(0)));
        assertEquals(map.get(uuids.get(2)), localKeyStore.getPublicKey(uuids.get(2)));
        assertEquals(map.get(uuids.get(4)), localKeyStore.getPublicKey(uuids.get(4)));
        assertThrows(NoSuchElementException.class, () -> localKeyStore.getPublicKey(uuids.get(1)));
        assertThrows(NoSuchElementException.class, () -> localKeyStore.getPublicKey(uuids.get(3)));
    }

    @Test
    void setLiveKeys_getLiveKeys_dropEverything() throws Exception {
        // init
        final KeyPairWithUUID reference_key = new KeyPairWithUUID();
        final LocalKeyStore localKeyStore = new LocalKeyStoreImpl();


        // get without data -> should throw
        assertThrows(SigningKeysException.class, localKeyStore::getSigningKeys);
        localKeyStore.setSigningKeys(reference_key.uuid, reference_key.keyPair.getPrivate(), reference_key.keyPair.getPublic());


        // get key after set
        final SigningKeys stored_key = localKeyStore.getSigningKeys();
        assertEquals(reference_key.uuid, stored_key.uuid);
        assertEquals(reference_key.keyPair.getPrivate(), stored_key.privateKey);


        // drop
        localKeyStore.resetAll();
        assertThrows(SigningKeysException.class, localKeyStore::getSigningKeys);
        assertThrows(NoSuchElementException.class, () -> localKeyStore.getPublicKey(reference_key.uuid));
    }
}
