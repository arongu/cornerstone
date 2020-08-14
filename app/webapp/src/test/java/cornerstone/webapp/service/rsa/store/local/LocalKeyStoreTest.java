package cornerstone.webapp.service.rsa.store.local;

import cornerstone.webapp.service.rsa.rotation.KeyPairWithUUID;
import org.junit.jupiter.api.Test;

import java.security.PublicKey;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class LocalKeyStoreTest {
    @Test
    void add_get_delete_get_uuids_CRUD_test() {
        final LocalKeyStore localKeyStore = new LocalKeyStoreImpl();
        final KeyPairWithUUID kp = new KeyPairWithUUID();

        // add
        localKeyStore.addPublicKey(kp.uuid, kp.keyPair.getPublic());
        // get
        assertEquals(kp.keyPair.getPublic(), localKeyStore.getPublicKey(kp.uuid));
        assertEquals(1 , localKeyStore.getPublicKeyUUIDs().size());
        assertTrue(localKeyStore.getPublicKeyUUIDs().contains(kp.uuid));
        // delete
        localKeyStore.deletePublicKey(kp.uuid);
        assertThrows(NoSuchElementException.class, () -> localKeyStore.getPublicKey(kp.uuid));
        assertEquals(0, localKeyStore.getPublicKeyUUIDs().size());
    }

    @Test
    public void deletePublicKeys_shouldDeleteAllKeys() {
        final int to_be_created = 5;
        int no_such_element_throws = 0;
        final LocalKeyStore localKeyStore = new LocalKeyStoreImpl();
        final Set<UUID> created_uuids = new HashSet<>();


        // add - get
        for (int i = 0; i < to_be_created; i++){
            KeyPairWithUUID kp = new KeyPairWithUUID();
            UUID uuid = kp.uuid;
            PublicKey pubKey = kp.keyPair.getPublic();

            localKeyStore.addPublicKey(uuid, pubKey);
            created_uuids.add(uuid);
        }
        assertEquals(created_uuids, localKeyStore.getPublicKeyUUIDs());


        // delete
        localKeyStore.deletePublicKeys(new ArrayList<>(created_uuids));
        for (final UUID uuid : created_uuids){
            try {
                localKeyStore.getPublicKey(uuid);
            } catch (final NoSuchElementException e){
                no_such_element_throws++;
            }
        }


        assertEquals(to_be_created, no_such_element_throws);
        System.out.println(String.format("to_be_created, no_such_element_throws: %d, %d", to_be_created, no_such_element_throws));
    }

    @Test
    public void sync_shouldSyncKeys_shouldKeepToBeeKeptAndDeleteTheRest() {
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
        localKeyStore.sync(uuids_to_be_kept);


        assertEquals(3, localKeyStore.getPublicKeyUUIDs().size());
        assertEquals(map.get(uuids.get(0)), localKeyStore.getPublicKey(uuids.get(0)));
        assertEquals(map.get(uuids.get(2)), localKeyStore.getPublicKey(uuids.get(2)));
        assertEquals(map.get(uuids.get(4)), localKeyStore.getPublicKey(uuids.get(4)));
        assertThrows(NoSuchElementException.class, () -> localKeyStore.getPublicKey(uuids.get(1)));
        assertThrows(NoSuchElementException.class, () -> localKeyStore.getPublicKey(uuids.get(3)));
    }

    @Test
    void set_get_drop_PublicAndPrivateKeys(){
        // init
        final KeyPairWithUUID reference_key = new KeyPairWithUUID();
        final LocalKeyStore localKeyStore = new LocalKeyStoreImpl();


        // get without set -> throw
        assertThrows(NoSuchElementException.class, localKeyStore::getLiveKeys);
        localKeyStore.setLiveKeyData(reference_key.uuid, reference_key.keyPair.getPrivate(), reference_key.keyPair.getPublic());


        // get key after set
        final LiveKeys stored_key = localKeyStore.getLiveKeys();
        assertEquals(reference_key.uuid, stored_key.uuid);
        assertEquals(reference_key.keyPair.getPrivate(), stored_key.privateKey);


        // drop
        localKeyStore.dropEverything();
        assertThrows(NoSuchElementException.class, localKeyStore::getLiveKeys);
        assertThrows(NoSuchElementException.class, () -> localKeyStore.getPublicKey(reference_key.uuid));
    }
}
