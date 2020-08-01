package cornerstone.webapp.services.rsa.store.db;

import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.services.rsa.common.PublicKeyData;
import cornerstone.webapp.services.rsa.rotation.KeyPairWithUUID;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PublicKeyStoreTest {
    private static PublicKeyStore publicKeyStore;

    @BeforeAll
    public static void setup() {
        final String test_files_dir = "../../_test_config/";
        final String keyFile  = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            ConfigurationLoader cl = new ConfigurationLoader(keyFile, confFile);
            publicKeyStore  = new PublicKeyStoreImpl(new WorkDB(cl));

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Order(0)
    @Test
    public void t00_addKey_shouldAddNKeys_whenCalledNTimes() throws PublicKeyStoreException {
        int desired_count = 5;
        int count_added = 0;
        int count_fetched = 0;
        final Map<UUID, String> keys_to_be_added = new HashMap<>();

        final Base64.Encoder encoder = Base64.getEncoder();
        for (int i = 0; i < desired_count; i++) {
            final KeyPairWithUUID kp = new KeyPairWithUUID();
            final String b64_pubkey = encoder.encodeToString(kp.keyPair.getPublic().getEncoded());

            keys_to_be_added.put(kp.uuid, b64_pubkey);
        }

        // ADD
        for (final Map.Entry<UUID,String> e : keys_to_be_added.entrySet()) {
            count_added += publicKeyStore.addKey(e.getKey(), getClass().getName(), 111_222_333, e.getValue());
        }

        // GET
        List<PublicKeyData> liveKeys = publicKeyStore.getLiveKeys();


        for (final Map.Entry<UUID,String> e : keys_to_be_added.entrySet()) {
            for (final PublicKeyData liveKey : liveKeys) {
                if (e.getKey().equals(liveKey.getUUID())) {
                    count_fetched++;
                    assertEquals(keys_to_be_added.get(liveKey.getUUID()), liveKey.getBase64Key());
                }
            }
        }

        assertEquals(desired_count, count_added);
        assertEquals(desired_count, count_fetched);
        assertTrue(liveKeys.size() >= desired_count);
    }

    @Order(1)
    @Test
    public void t01_getThePreviouslyCreatedLiveAndExpiredKeysThenDeleteThemOneByOne_shouldDeleteAllKeys() throws PublicKeyStoreException {
        final List<UUID> uuid_list_expired;
        final List<UUID> uuid_list_live;
        int uuids;
        int deletes = 0;


        uuid_list_expired = publicKeyStore.getExpiredKeyUUIDs();
        uuid_list_live    = publicKeyStore.getLiveKeyUUIDs();
        uuids = uuid_list_expired.size() + uuid_list_live.size();

        for (final UUID uuid : uuid_list_expired){
            publicKeyStore.deleteKey(uuid);
            deletes++;
        }

        for (final UUID uuid : uuid_list_live){
            publicKeyStore.deleteKey(uuid);
            deletes++;
        }

        System.out.println(String.format("uuids, deletes: %d, %d", uuids, deletes));
        assertEquals(uuids, deletes);
    }

    @Order(2)
    @Test
    public void t02_deleteExpiredKeys_shouldDeleteAllExpiredKey_whenCalled() throws PublicKeyStoreException {
        int expired_keys_to_be_created = 5;
        int expired_keys_created = 0;

        int expired_keys_deleted;
        int got_expired_keys_first;
        int got_expired_keys_second;

        final Base64.Encoder enc = Base64.getEncoder();
        for (int i = 0; i < expired_keys_to_be_created; i++) {
            final KeyPairWithUUID kp = new KeyPairWithUUID();
            final String base64pubkey = enc.encodeToString(kp.keyPair.getPublic().getEncoded());
            expired_keys_created += publicKeyStore.addKey(kp.uuid, getClass().getName(), 0, base64pubkey);
        }


        got_expired_keys_first = publicKeyStore.getExpiredKeyUUIDs().size();
        expired_keys_deleted = publicKeyStore.deleteExpiredKeys();
        got_expired_keys_second = publicKeyStore.getExpiredKeyUUIDs().size();


        assertEquals(expired_keys_to_be_created, expired_keys_created);
        assertEquals(expired_keys_to_be_created, got_expired_keys_first);
        assertEquals(expired_keys_to_be_created, expired_keys_deleted);
        assertEquals(0, got_expired_keys_second);
        System.out.println(String.format("created, deleted: %d, %d", expired_keys_created, expired_keys_deleted));
    }

    @Order(3)
    @Test
    public void t03_add_get_delete_verify_CRUD_test() throws PublicKeyStoreException {
        int number_of_cruds = 5;
        int added = 0;
        int got = 0;

        int deleted = 0;
        int got_after_delete = 0;

        int verified_pubkey_matches = 0;


        Map<UUID, String> keys_to_be_added = new HashMap<>();
        Map<UUID, PublicKeyData> received_data = new HashMap<>();

        // ADD
        final Base64.Encoder enc = Base64.getEncoder();
        for (int i = 0; i < number_of_cruds; i++) {
            final KeyPairWithUUID kp = new KeyPairWithUUID();
            final String base64pubkey = enc.encodeToString(kp.keyPair.getPublic().getEncoded());

            keys_to_be_added.put(kp.uuid, base64pubkey);
            added += publicKeyStore.addKey(kp.uuid, getClass().getName(), 1_000_000, base64pubkey);
        }

        // GET
        for (Map.Entry<UUID, String> entry : keys_to_be_added.entrySet()) {
            final UUID uuid = entry.getKey();
            received_data.put(uuid, publicKeyStore.getKey(uuid));
            got++;
        }

        // DELETE & GET
        for (Map.Entry<UUID, String> entry : keys_to_be_added.entrySet()) {
            final UUID uuid = entry.getKey();
            deleted += publicKeyStore.deleteKey(uuid);
            try {
                publicKeyStore.getKey(uuid);
                got_after_delete++;
            } catch (final NoSuchElementException ignored){

            }
        }

        // Verify Data
        for (Map.Entry<UUID, String> entry : keys_to_be_added.entrySet()) {
            final String generated_key = entry.getValue();
            final PublicKeyData data = received_data.get(entry.getKey());

            if (generated_key.equals(data.getBase64Key())){
                verified_pubkey_matches++;
            };
        }


        assertEquals(number_of_cruds, added);
        assertEquals(number_of_cruds, got);
        assertEquals(number_of_cruds, deleted);
        assertEquals(0, got_after_delete);
        assertEquals(number_of_cruds, verified_pubkey_matches);

        System.out.println(String.format("added, got, deleted, got_after_delete, verified_pubkey_matches: %d, %d, %d, %d, %d", added, got, deleted, got_after_delete, verified_pubkey_matches));
    }
}
