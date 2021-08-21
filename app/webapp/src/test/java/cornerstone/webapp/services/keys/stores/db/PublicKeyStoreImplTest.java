package cornerstone.webapp.services.keys.stores.db;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.services.keys.common.PublicKeyData;
import cornerstone.webapp.services.keys.rotation.KeyPairWithUUID;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PublicKeyStoreImplTest {
    private static DatabaseKeyStore publicKeyStore;

    @BeforeAll
    public static void setup() {
        final String test_config_dir = "../../_test_config/";
        final String key_file        = Paths.get(test_config_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String conf_file       = Paths.get(test_config_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            final ConfigLoader configLoader = new ConfigLoader(key_file, conf_file);
            publicKeyStore  = new DatabaseKeyStoreImpl(new WorkDB(configLoader));

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Order(0)
    @Test
    public void t00_addKey_shouldAddNKeys_whenCalledNTimes() throws Exception {
        int desired_count = 5;
        int count_added   = 0;
        int count_fetched = 0;
        final Map<UUID, String> keys_to_be_added = new HashMap<>();
        // create keys
        final Base64.Encoder encoder = Base64.getEncoder();
        for (int i = 0; i < desired_count; i++) {
            final KeyPairWithUUID kp = new KeyPairWithUUID();
            final String b64_pubkey = encoder.encodeToString(kp.keyPair.getPublic().getEncoded());

            keys_to_be_added.put(kp.uuid, b64_pubkey);
        }


        // ADD
        for (final Map.Entry<UUID,String> e : keys_to_be_added.entrySet()) {
            count_added += publicKeyStore.addPublicKey(e.getKey(), getClass().getSimpleName(), 111_222_333, e.getValue());
        }
        // GET
        List<PublicKeyData> liveKeys = publicKeyStore.getLivePublicKeys();


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
    public void t01_getThePreviouslyCreatedLiveAndExpiredKeysThenDeleteThemOneByOne_shouldDeleteAllKeys() throws DatabaseKeyStoreException {
        final List<UUID> uuid_list_expired;
        final List<UUID> uuid_list_live;
        int number_of_uuids;
        int number_of_deletes = 0;


        uuid_list_expired = publicKeyStore.getExpiredPublicKeyUUIDs();
        uuid_list_live    = publicKeyStore.getLivePublicKeyUUIDs();
        number_of_uuids   = uuid_list_expired.size() + uuid_list_live.size();

        for (final UUID uuid : uuid_list_expired) {
            publicKeyStore.deletePublicKey(uuid);
            number_of_deletes++;
        }

        for (final UUID uuid : uuid_list_live) {
            publicKeyStore.deletePublicKey(uuid);
            number_of_deletes++;
        }

        System.out.printf("uuids, deletes: %d, %d\n", number_of_uuids, number_of_deletes);
        assertEquals(number_of_uuids, number_of_deletes);
    }

    @Order(2)
    @Test
    public void t02_deleteExpiredKeys_shouldDeleteAllExpiredKey() throws DatabaseKeyStoreException {
        int wanted_expired_keys        = 5;
        int expired_keys_created       = 0;
        // results
        int expired_keys_deleted;
        int expired_keys_received_first_time;
        int expired_keys_received_second_time;
        // setup, create expired keys
        final Base64.Encoder enc = Base64.getEncoder();
        for (int i = 0; i < wanted_expired_keys; i++) {
            final KeyPairWithUUID kp = new KeyPairWithUUID();
            final String base64pubkey = enc.encodeToString(kp.keyPair.getPublic().getEncoded());
            expired_keys_created += publicKeyStore.addPublicKey(kp.uuid, getClass().getSimpleName(), 0, base64pubkey);
        }


        expired_keys_received_first_time  = publicKeyStore.getExpiredPublicKeyUUIDs().size();
        expired_keys_deleted              = publicKeyStore.deleteExpiredPublicKeys();
        expired_keys_received_second_time = publicKeyStore.getExpiredPublicKeyUUIDs().size();


        assertEquals(wanted_expired_keys, expired_keys_created);
        assertEquals(wanted_expired_keys, expired_keys_received_first_time);
        assertEquals(wanted_expired_keys, expired_keys_deleted);
        assertEquals(0, expired_keys_received_second_time);
        System.out.printf("created, deleted: %d, %d\n", expired_keys_created, expired_keys_deleted);
    }

    @Order(3)
    @Test
    public void t03_add_get_delete_verify_CRUD_test() throws DatabaseKeyStoreException {
        int wanted_number_of_cruds = 5;
        // results
        int number_of_keys_added            = 0;
        int number_of_keys_got              = 0;
        int number_of_keys_deleted          = 0;
        int number_of_keys_got_after_delete = 0;
        int number_of_pubkey_matches        = 0;
        // data
        final Map<UUID, String> keys_to_be_added            = new HashMap<>();
        final Map<UUID, PublicKeyData> received_pubkey_data = new HashMap<>();

        // ADD
        final Base64.Encoder enc = Base64.getEncoder();
        for (int i = 0; i < wanted_number_of_cruds; i++) {
            final KeyPairWithUUID kp = new KeyPairWithUUID();
            final String base64pubkey = enc.encodeToString(kp.keyPair.getPublic().getEncoded());

            keys_to_be_added.put(kp.uuid, base64pubkey);
            number_of_keys_added += publicKeyStore.addPublicKey(kp.uuid, getClass().getSimpleName(), 1_000_000, base64pubkey);
        }

        // GET
        for (Map.Entry<UUID, String> entry : keys_to_be_added.entrySet()) {
            final UUID uuid = entry.getKey();
            received_pubkey_data.put(uuid, publicKeyStore.getPublicKey(uuid));
            number_of_keys_got++;
        }

        // DELETE & GET
        for (Map.Entry<UUID, String> entry : keys_to_be_added.entrySet()) {
            final UUID uuid = entry.getKey();
            number_of_keys_deleted += publicKeyStore.deletePublicKey(uuid);
            try {
                publicKeyStore.getPublicKey(uuid);
                number_of_keys_got_after_delete++;
            } catch (final NoSuchElementException ignored){

            }
        }

        // Verify Data
        for (Map.Entry<UUID, String> entry : keys_to_be_added.entrySet()) {
            final String generated_key = entry.getValue();
            final PublicKeyData data = received_pubkey_data.get(entry.getKey());

            if (generated_key.equals(data.getBase64Key())){
                number_of_pubkey_matches++;
            }
        }


        assertEquals(wanted_number_of_cruds, number_of_keys_added);
        assertEquals(wanted_number_of_cruds, number_of_keys_got);
        assertEquals(wanted_number_of_cruds, number_of_keys_deleted);
        assertEquals(0, number_of_keys_got_after_delete);
        assertEquals(wanted_number_of_cruds, number_of_pubkey_matches);

        System.out.printf("added, got, deleted, got_after_delete, verified_pubkey_matches: %d, %d, %d, %d, %d\n", number_of_keys_added, number_of_keys_got, number_of_keys_deleted, number_of_keys_got_after_delete, number_of_pubkey_matches);
    }
}
