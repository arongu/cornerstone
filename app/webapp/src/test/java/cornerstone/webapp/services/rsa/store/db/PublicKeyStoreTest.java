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
    public void t00_addKey_shouldCreate10Keys_whenCalled10Times() throws PublicKeyStoreException {
        long startTime;
        double endTime;
        int keys_to_be_added = 10;
        int keys_added = 0;

        for (int i = 0; i < keys_to_be_added; i++) {
            startTime = System.currentTimeMillis();
            final KeyPairWithUUID keyPairWithUUID = new KeyPairWithUUID();
            final String base64pubkey = Base64.getEncoder().encodeToString(keyPairWithUUID.keyPair.getPublic().getEncoded());

            keys_added += publicKeyStore.addKey(keyPairWithUUID.uuid, getClass().getName(), 111_222_333, base64pubkey);
            endTime = (double)(System.currentTimeMillis() - startTime) / 1000;

            System.out.println(
                    String.format("[ OK ] %03d/%03d -- elapsed (%.03fs) -- uuid: '%s', pubkey: '%s'",
                            i+1,
                            keys_to_be_added,
                            endTime,
                            keyPairWithUUID.uuid.toString(), base64pubkey
                    )
            );
        }

        assertEquals(keys_to_be_added, keys_added);
    }

    @Order(1)
    @Test
    public void t01_getThePreviouslyCreatedLiveAndExpiredKeysThenDeleteThemOneByOne_shouldDeleteAllKeys() throws PublicKeyStoreException {
        final List<UUID> expiredUUIDS;
        final List<UUID> liveUUIDs;
        int number_of_uuids;
        int deletes = 0;


        expiredUUIDS = publicKeyStore.getExpiredKeyUUIDs();
        liveUUIDs = publicKeyStore.getLiveKeyUUIDs();
        number_of_uuids = expiredUUIDS.size() + liveUUIDs.size();

        for (final UUID uuid : expiredUUIDS){
            publicKeyStore.deleteKey(uuid);
            deletes++;
        }

        for (final UUID uuid : liveUUIDs){
            publicKeyStore.deleteKey(uuid);
            deletes++;
        }

        System.out.println(String.format("number_of_uuids, deletes: %d, %d", number_of_uuids, deletes));
        assertEquals(number_of_uuids, deletes);
    }

    @Order(2)
    @Test
    public void t02_deleteExpiredKeys_shouldDeleteAllExpiredKey_whenCalled() throws PublicKeyStoreException {
        int expired_keys_to_be_created = 10;
        int expired_keys_created = 0;

        int expired_keys_deleted;
        int got_expired_keys_first;
        int got_expired_keys_second;

        for (int i = 0; i < expired_keys_to_be_created; i++){
            final KeyPairWithUUID keyPairWithUUID = new KeyPairWithUUID();
            final String base64pubkey = Base64.getEncoder().encodeToString(keyPairWithUUID.keyPair.getPublic().getEncoded());
            expired_keys_created += publicKeyStore.addKey(keyPairWithUUID.uuid, getClass().getName(), 0, base64pubkey);
        }


        got_expired_keys_first = publicKeyStore.getExpiredKeyUUIDs().size();
        expired_keys_deleted = publicKeyStore.deleteExpiredKeys();
        got_expired_keys_second = publicKeyStore.getExpiredKeyUUIDs().size();


        assertEquals(expired_keys_to_be_created, expired_keys_created);
        assertEquals(expired_keys_to_be_created, got_expired_keys_first);
        assertEquals(expired_keys_to_be_created, expired_keys_deleted);
        assertEquals(0, got_expired_keys_second);
        System.out.println(String.format("creates, deletes: %d, :%d", expired_keys_created, expired_keys_deleted));
    }

    @Order(3)
    @Test
    public void t03_add_get_delete_verify_CRUD_test() throws PublicKeyStoreException {
        int number_of_cruds = 20;
        int added = 0;
        int got = 0;

        int deleted = 0;
        int got_after_delete = 0;

        int verified_pubkey_matches = 0;


        Map<UUID, String> generated_data = new HashMap<>();
        Map<UUID, PublicKeyData> received_data = new HashMap<>();

        // ADD
        for (int i = 0; i < number_of_cruds; i++){
            final KeyPairWithUUID keyPairWithUUID = new KeyPairWithUUID();
            final String base64pubkey = Base64.getEncoder().encodeToString(keyPairWithUUID.keyPair.getPublic().getEncoded());

            generated_data.put(keyPairWithUUID.uuid, base64pubkey);
            added += publicKeyStore.addKey(keyPairWithUUID.uuid, getClass().getName(), 1_000_000, base64pubkey);
        }

        // GET
        for(Map.Entry<UUID, String> entry : generated_data.entrySet()){
            final UUID uuid = entry.getKey();
            received_data.put(uuid, publicKeyStore.getKey(uuid));
            got++;
        }

        // DELETE & GET
        for(Map.Entry<UUID, String> entry : generated_data.entrySet()){
            final UUID uuid = entry.getKey();
            deleted += publicKeyStore.deleteKey(uuid);
            try {
                publicKeyStore.getKey(uuid);
                got_after_delete++;
            } catch (final NoSuchElementException ignored){
            }
        }

        // Verify Data
        for(Map.Entry<UUID, String> entry : generated_data.entrySet()){
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
